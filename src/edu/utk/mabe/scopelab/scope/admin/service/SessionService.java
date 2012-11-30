package edu.utk.mabe.scopelab.scope.admin.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
import edu.utk.mabe.scopelab.scope.admin.service.ScriptService.Script;

public class SessionService 
{
	public class Session
	{
		protected class JoinHandler implements MessageListener
		{
			/* Instance variables */
			protected final int requiredNumberOfParticipants;
			protected int numParticipants = 0;
			
			protected MessageProducer joinProducer = null;
			protected VirtualTopic[] virtualTopics;
			protected VirtualTopic   newsfeed;
			protected boolean        started;
			
			
			protected JoinHandler(int requiredNumberOfParticipants)
			{
				this.requiredNumberOfParticipants = requiredNumberOfParticipants;
				
				/* News feed topic */
				//this.newsfeed = messengingService.createVirtualTopic(
				//		String.format("%s.%s", sessionID, newsfeed));
				
				
				this.virtualTopics = new VirtualTopic[requiredNumberOfParticipants];				
			}
				
			protected String getJoinQueueName() throws JMSException
			{
				return this.joinProducer.getDestination().toString();
			}
			
			protected boolean hasStarted()
			{
				return this.started;
			}
			
			void start() throws JMSException
			{
				for(int i=0; i<requiredNumberOfParticipants; i++)
				{
					/* Creates a virtual topic for the broadcast 
					 * queue */
					this.virtualTopics[i] = 
							messengingService.createVirtualTopic(
									String.format("%s.%s", sessionID, Integer.toString(i)));
				}
				
				/* Gets a name for the join destination queue */
				String joinDestination = messengingService.getSessionJoinDestination(sessionID);
				
				/* Creates a queue for listening to joins */
				joinProducer = messengingService.createQueue(joinDestination, joinHandler);
			
				/* Sends the announcement message */
				messengingService.sendNewSessionMessage(sessionID, joinDestination);
				
				this.started = true;
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
				
					dataObject.element("NewsFeedTopic", newsfeed.getTopicURL());
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
		final protected String sessionID;
		final protected Graph  graph;
		final protected Script script;
		final protected int    requiredNumOfParticpants;
		
		protected MessengingService messengingService = null;
		protected JoinHandler joinHandler = null;
		
		protected StorageService storageService = null;
		
		
		protected final ParticipantEntry[] participants;
		protected final Map<String, Integer> participantIDToIndexMap;
		
		/* Status info */
		protected boolean started = false;
	
		
		Session(String sessionID, Graph graph, 
				Script script, MessengingService messengingService, 
				StorageService storageService) throws JMSException
		{
			this.sessionID = sessionID;
			this.graph = graph;
			this.script = script;
			
			this.messengingService = messengingService;
			this.storageService    = storageService;
			
			this.requiredNumOfParticpants = graph.getNumNodes();
			
			joinHandler = new JoinHandler(this.requiredNumOfParticpants);
			
			this.participants = new ParticipantEntry[graph.getNumNodes()];
			this.participantIDToIndexMap = new ConcurrentHashMap<>(graph.getNumNodes());
		}
		
		Session(Graph graph, Script script, MessengingService messengingService, 
				StorageService storageService) throws JMSException
		{
			this(UUID.randomUUID().toString(), graph, script, messengingService, 
					storageService);
		}
		
		public void activate() throws JMSException, SQLException, ScopeError 
		{
			/* Checks the session hasn't already been started */
			if(this.started == true)
			{
				throw new ScopeError("Session has already started");
			}
			
			this.started = true;
			
			/* Starts the join handler */
			this.joinHandler.start();
		
			/* Adds the session to the list of active sessions in the backend
			 * storage */
			storageService.storeActiveSession(this);
		}
		
		public void start()
		{
			/* Send out the news feed */
		}
		
		public String getJoinQueueName() throws JMSException
		{
			return this.joinHandler.getJoinQueueName();
		}
		
		public boolean hasStarted()
		{
			return this.started;
		}
		
		public boolean isCollectingParticipants()
		{
			return this.joinHandler.hasStarted();
		}
		
		public int getNumberofParticipants()
		{
			return this.participantIDToIndexMap.size();
		}
		
		public int getCurrentNumberOfParticipants()
		{
			return this.requiredNumOfParticpants;
		}
	}
	
	/* Instance variables */
	final protected StorageService storageService;
	
	
	public SessionService(StorageService storageService)
	{
		this.storageService = storageService;
	}
	
	public Session createSession(String sessionID, Graph graph, 
				Script script, MessengingService messengingService, 
				StorageService storageService) throws JMSException
	{
		/* Creates the session */
		Session session = new Session(sessionID, graph, script, messengingService, 
				storageService);
		
		return session;
	}
}
