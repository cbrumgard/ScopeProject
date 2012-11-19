package edu.utk.mabe.scopelab.scope.admin.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import edu.utk.mabe.scopelab.scope.ScopeError;
import edu.utk.mabe.scopelab.scope.admin.service.GraphService.Graph;
import edu.utk.mabe.scopelab.scope.admin.service.GraphService.Node;
import edu.utk.mabe.scopelab.scope.admin.service.MessengingService.VirtualTopic;

public class SessionService 
{
	public class Session
	{
		protected class JoinHandler implements MessageListener
		{
			/* Instance variables */
			protected final int requiredNumberOfParticipants;
			protected int numParticipants = 0;
			
			protected VirtualTopic[] virtualTopics;
			
			
			
			protected JoinHandler(int requiredNumberOfParticipants) 
					throws JMSException
			{
				this.requiredNumberOfParticipants = requiredNumberOfParticipants;
				
				this.virtualTopics = new VirtualTopic[requiredNumberOfParticipants];
				
				for(int i=0; i<requiredNumberOfParticipants; i++)
				{
					/* Creates a virtual topic for the broadcast 
					 * queue */
					this.virtualTopics[i] = 
							messengingService.createVirtualTopic(
									String.format("%s.%s", sessionID, Integer.toString(i)));
				}
				
			}
			
			@Override
			public void onMessage(Message message) 
			{
				String queueName = null;
				
				try
				{
					System.out.println("I have a join message");
					System.out.println(message.toString());

					/* Gets the text message */
					TextMessage txtMsg = (TextMessage)message;

					System.out.println(txtMsg.getText());
					
					/* Parses the text as JSON */
					JSONObject jsonObject = JSONObject.fromObject(txtMsg.getText());
					
					
					String participantID = jsonObject.getString("participantID");
					queueName     = jsonObject.getString("queueName");
					int participantIndex;
					
					synchronized(this) 
					{
						/* Number of participants reached */
						if(this.numParticipants >= this.requiredNumberOfParticipants)
						{
							/* Send Error message */
							messengingService.sendMessageToQueue(queueName, 
									new JSONObject().element("msgType", "error")
											.element("data", "Session already has the meet the required number of participants.")
											.toString());
							
							return;
						}
						
						/* Participant is already a member of the session */
						if(participantIDToIndexMap.containsKey(participantID))
						{
							/* Send error message */
							
							JSONObject dataObject = new JSONObject();
							
							messengingService.sendMessageToQueue(queueName, 
									new JSONObject().element("msgType", "error")
											.element("data", "You are already a participant in this session")
											.toString());
							
							return;
						}
				
						participantIndex = numParticipants;
									
						/* Creates the participant entry */
						participants[participantIndex] = new ParticipantEntry(participantID, queueName, virtualTopics[numParticipants]);
						participantIDToIndexMap.put(participantID, participantIndex);
						
						
						/* Increments the participant count */
						numParticipants++;
					}
					
					/* Builds the response object with the publish topic and
					 * the queues to subscribe to */
					JSONObject dataObject = new JSONObject();
					
					dataObject.element("PublishTopic", virtualTopics[participantIndex].getTopicURL());
					
					for(Node node: graph.getConnectedNodes(participantIndex))
					{			
						dataObject.accumulate("ListenQueues", 
								virtualTopics[node.getID()].getConsumerURL());
					}
					
					
					
					/* Send back success message */
					messengingService.sendMessageToQueue(queueName, 
							new JSONObject().element("msgType", "data")
									.element("data", dataObject)
									.toString());
					
					
				
				/* Send error message */
				}catch(JMSException | JSONException e)
				{
					e.printStackTrace();
					
					if(queueName != null)
					{
						try 
						{
							messengingService.sendMessageToQueue(queueName, 
									new JSONObject().element("msgType", "error")
									.element("data", "An internal error has occurred with scope server")
									.toString());
						}catch (JMSException e1) 
						{
							e1.printStackTrace();
						}
					}
				}
			}
			
		}
		
		protected class ParticipantEntry
		{
			/* Instance variables */
			protected final String participantID;
			protected final String queueName;
			protected final VirtualTopic broadcastQueue;
			
			
			protected ParticipantEntry(String participantID, 
					String queue, VirtualTopic broadcastQueue) 
			{
				this.participantID  = participantID;
				this.queueName   = queue;
				this.broadcastQueue = broadcastQueue;
			}

			public String getParticipantID() 
			{
				return participantID;
			}

			public String getNameQueue() 
			{
				return queueName;
			}
		}
		
		
		/* Instance variables */
		final String sessionID;
		final Graph  graph;
		
		protected MessengingService messengingService = null;
		protected JoinHandler joinHandler = null;
		protected MessageProducer joinProducer = null;
		protected BackendStorageService storageService = null;
		protected boolean started = false;
		
		protected final ParticipantEntry[] participants;
		protected final Map<String, Integer> participantIDToIndexMap;
		
		Session(String sessionID, Graph graph, 
				MessengingService messengingService, 
				BackendStorageService storageService) throws JMSException
		{
			this.sessionID = sessionID;
			this.graph = graph;
			
			this.messengingService = messengingService;
			this.storageService    = storageService;
			
			joinHandler = new JoinHandler(graph.getNumNodes());
			
			this.participants = new ParticipantEntry[graph.getNumNodes()];
			this.participantIDToIndexMap = new HashMap<>(graph.getNumNodes());
		}
		
		Session(Graph graph, MessengingService messengingService, 
				BackendStorageService storageService) throws JMSException
		{
			this(UUID.randomUUID().toString(), graph, messengingService, 
					storageService);
		}
		
		public void start() throws JMSException, SQLException, ScopeError 
		{
			/* Checks the session hasn't already been started */
			if(this.started == true)
			{
				throw new ScopeError("Session has already started");
			}
			
			this.started = true;
			
			/* Gets a name for the join destination queue */
			String joinDestination = messengingService.getSessionJoinDestination(sessionID);
			
			/* Creates a queue for listening to joins */
			joinProducer = messengingService.createQueue(joinDestination, joinHandler);
			
			/* Adds the session to the list of active sessions in the backend
			 * storage */
			storageService.storeActiveSession(this);
			
			/* Sends the announcement message */
			messengingService.sendNewSessionMessage(sessionID, joinDestination);
		}
		
		
		public String getJoinQueueName() throws JMSException
		{
			return this.joinProducer.getDestination().toString();
		}
	}
	
	/* Instance variables */
	final protected BackendStorageService storageService;
	
	
	public SessionService(BackendStorageService storageService)
	{
		this.storageService = storageService;
	}
	
	public Session createSession(String sessionID, Graph graph, 
				MessengingService messengingService, 
				BackendStorageService storageService) throws JMSException
	{
		/* Creates the session */
		Session session = new Session(sessionID, graph, messengingService, 
				storageService);
		
		return session;
	}
}
