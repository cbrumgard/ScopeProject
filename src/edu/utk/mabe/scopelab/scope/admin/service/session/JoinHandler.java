package edu.utk.mabe.scopelab.scope.admin.service.session;


import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import edu.utk.mabe.scopelab.scope.admin.service.GraphService.Node;
import edu.utk.mabe.scopelab.scope.admin.service.messenging.MessageDestination;
import edu.utk.mabe.scopelab.scope.admin.service.messenging.MessageListener;
import edu.utk.mabe.scopelab.scope.admin.service.messenging.MessengingException;
import edu.utk.mabe.scopelab.scope.admin.service.session.Session.ParticipantEntry;

class JoinHandler implements MessageListener
{
	protected final static String NEW_SESSION_ANNONCEMENT_TOPIC = "scope.new_sessions";
	protected final static String SESSION_JOIN_TOPIC_FORMAT = "scope.new_sessions.%s";
	
	/* Instance variables */
	protected final Session session;
	protected final int requiredNumberOfParticipants;
	protected int numParticipants = 0;
	protected final CountDownLatch participantCountDownLatch;
	protected MessageDestination[] fromClientdestinations;
	protected MessageDestination   newsfeedDestination;
	protected boolean        started;
	
	
	protected JoinHandler(Session session, int requiredNumberOfParticipants)
	{
		this.session = session;
		this.requiredNumberOfParticipants = requiredNumberOfParticipants;
	
		this.participantCountDownLatch = new CountDownLatch(requiredNumberOfParticipants);
		
		this.fromClientdestinations = new MessageDestination[requiredNumberOfParticipants];				
	}
	
	protected int getNumParticipants()
	{
		return this.numParticipants;
	}
	
	protected boolean hasStarted()
	{
		return this.started;
	}
	
	protected void waitForParticipantsToJoin() throws InterruptedException
	{
		this.participantCountDownLatch.await();
	}
	
	void start() throws MessengingException
	{
		for(int i=0; i<requiredNumberOfParticipants; i++)
		{
			/* Creates a queue for each client queue */
			this.fromClientdestinations[i] = this.session.messengingService.createQueue(
					String.format("%s.%s", this.session.getSessionID(), 
							UUID.randomUUID().toString()));
		}
		
		
		
		/* Creates a queue for listening to joins */
		MessageDestination joinDestination = this.session.messengingService
				.createQueue(String.format(SESSION_JOIN_TOPIC_FORMAT, 
						this.session.getSessionID()));
		
		/* Adds this class as a listener */
		joinDestination.setMessageListener(this);
		
	
		/* Sends the announcement message */
		MessageDestination sessionAnnoncementDestination = 
				this.session.messengingService.createTopic(
						NEW_SESSION_ANNONCEMENT_TOPIC);
		
		this.session.messengingService.sendMessage(sessionAnnoncementDestination, 
				new JSONObject().element("sessionID", session.getSessionID())
		        	.element("joinDestination", joinDestination.getName())
		        	.toString());
		
		this.started = true;
	}
	
	@Override
	public void onError(Throwable e) 
	{
		e.printStackTrace();
		
		System.exit(-1);
		
		/* TODO Need to handle error and detach clients */
	}
	
	@Override
	public void onMessage(String message) 
	{
		MessageDestination toClientDestination = null;
	
		try
		{
			System.out.println("I have a join message");
			System.out.println(message);

			
			/* Parses the text as JSON */
			JSONObject jsonObject = JSONObject.fromObject(message);
			
			
			String participantID = jsonObject.getString("participantID");
			String queueName     = jsonObject.getString("queueName").replaceFirst("queue://", "");
			
			int participantIndex;
			
			toClientDestination = 
					this.session.messengingService.createQueue(queueName);
			
			synchronized(this) 
			{
				/* Number of participants reached */
				if(this.numParticipants >= this.requiredNumberOfParticipants)
				{
					/* Send Error message */
					this.session.messengingService.sendMessage(toClientDestination, 
							new JSONObject().element("msgType", "error")
									.element("data", "Session already has the meet the required number of participants.")
									.toString());
					
					return;
				}
				
				/* Participant is already a member of the session */
				if(this.session.participantIDToIndexMap.containsKey(participantID))
				{
					/* Send error message */
					
					JSONObject dataObject = new JSONObject();
					
					this.session.messengingService.sendMessage(toClientDestination, 
							new JSONObject().element("msgType", "error")
									.element("data", "You are already a participant in this session")
									.toString());
					
					return;
				}
		
				participantIndex = numParticipants;
							
				/* Creates the participant entry */
				this.session.participants[participantIndex] = 
						this.session.new ParticipantEntry(participantID, toClientDestination, fromClientdestinations[numParticipants]);
				this.session.participantIDToIndexMap.put(participantID, participantIndex);
				
				
				/* Increments the participant count and counts down the latch */
				numParticipants++;
				this.participantCountDownLatch.countDown();
			}
			
			/* Builds the response object with the publish topic and
			 * the queues to subscribe to */
			JSONObject dataObject = new JSONObject();
		
			dataObject.element("toServerDestination", fromClientdestinations[participantIndex].getName());
			
			
			
			
			System.out.printf("Sending accept message back to %s\n", toClientDestination.getName());
			
			/* Send back success message */
			this.session.messengingService.sendMessage(toClientDestination, 
					new JSONObject().element("msgType", "data")
							.element("data", dataObject)
							.toString());
			
			
		
		/* Send error message */
		}catch(MessengingException | JSONException e)
		{
			e.printStackTrace();
			
			if(toClientDestination != null)
			{
				try 
				{
					this.session.messengingService.sendMessage(toClientDestination, 
							new JSONObject().element("msgType", "error")
							.element("data", "An internal error has occurred with scope server")
							.toString());
				}catch(MessengingException e1) 
				{
					e1.printStackTrace();
				}
			}
		}
		
		
	}

	String getJoinQueueName() 
	{
		return String.format(SESSION_JOIN_TOPIC_FORMAT, 
				this.session.getSessionID());
	}
}