package edu.utk.mabe.scopelab.scope.admin.service.session;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.sf.json.JSONObject;
import edu.utk.mabe.scopelab.scope.ScopeError;
import edu.utk.mabe.scopelab.scope.admin.service.GraphService.Graph;
import edu.utk.mabe.scopelab.scope.admin.service.GraphService.Node;
import edu.utk.mabe.scopelab.scope.admin.service.ScriptService.ChoiceEvent;
import edu.utk.mabe.scopelab.scope.admin.service.ScriptService.Event;
import edu.utk.mabe.scopelab.scope.admin.service.ScriptService.NewsEvent;
import edu.utk.mabe.scopelab.scope.admin.service.ScriptService.Script;
import edu.utk.mabe.scopelab.scope.admin.service.StorageService;
import edu.utk.mabe.scopelab.scope.admin.service.messenging.MessageDestination;
import edu.utk.mabe.scopelab.scope.admin.service.messenging.MessengingException;
import edu.utk.mabe.scopelab.scope.admin.service.messenging.MessengingService;

public class Session
{
	protected class ParticipantEntry
	{
		/* Instance variables */
		protected final String participantID;
		protected final MessageDestination toClientDestination;
		protected final MessageDestination fromClientDestination;
		
		
		protected ParticipantEntry(String participantID, 
				MessageDestination toClientDestination, 
				MessageDestination fromClientDestination) 
		{
			this.participantID  	   = participantID;
			this.toClientDestination   = toClientDestination;
			this.fromClientDestination = fromClientDestination;
		}

		public String getParticipantID() 
		{
			return participantID;
		}
		
		MessageDestination getClientDestination()
		{
			return this.toClientDestination;
		}
		
		MessageDestination getServerDestination()
		{
			return this.fromClientDestination;
		}
	}
	
	
	/* Instance variables */
	private final String sessionID;
	final protected Graph  graph;
	final protected Script script;
	final protected int    requiredNumOfParticpants;
	
	protected MessengingService messengingService = null;
	protected JoinHandler joinHandler = null;
	
	protected StorageService storageService = null;
	
	
	protected final ParticipantEntry[] participants;
	protected final Map<String, Integer> participantIDToIndexMap;
	
	/* Status info */
	protected boolean activated = false;
	protected boolean started   = false;
	protected Timer timer = null;

	
	Session(String sessionID, Graph graph, 
			Script script, MessengingService messengingService, 
			StorageService storageService)
	{
		this.sessionID = sessionID;
		this.graph = graph;
		this.script = script;
		
		this.messengingService = messengingService;
		this.storageService    = storageService;
		
		this.requiredNumOfParticpants = graph.getNumNodes();
		
		joinHandler = new JoinHandler(this, this.requiredNumOfParticpants);
		
		this.participants = new ParticipantEntry[graph.getNumNodes()];
		this.participantIDToIndexMap = new ConcurrentHashMap<>(graph.getNumNodes());
	}
	
	Session(Graph graph, Script script, MessengingService messengingService, 
			StorageService storageService)
	{
		this(UUID.randomUUID().toString(), graph, script, messengingService, 
				storageService);
	}
	
	public void activate() throws SQLException, ScopeError, MessengingException 
	{
		/* Checks the session hasn't already been started */
		if(this.activated == true)
		{
			throw new ScopeError("Session has already started");
		}
		
		this.activated = true;
		
		/* Starts the join handler */
		this.joinHandler.start();
	
		/* Adds the session to the list of active sessions in the backend
		 * storage */
		storageService.storeActiveSession(this);
	}
	
	
	void recordParticipants() throws SQLException
	{
		this.storageService.storeParticipants(this.sessionID, participantIDToIndexMap);
	}
	
	
	void recordChoice(int interval, int participantIndex, String choice) 
			throws SQLException
	{
		System.out.printf("At interval %d, participant %d chose %s\n", 
				interval, participantIndex, choice);
		
		this.storageService.storeChoice(sessionID, interval, participantIndex, choice);
	}
	
	public void start() throws ScopeError, SQLException, MessengingException
	{
		if(this.joinHandler.getNumParticipants() < this.requiredNumOfParticpants)
		{
			throw new ScopeError("Session can't be started until the required number of participants is meet");
		}
		
		synchronized(this) 
		{
			if(timer != null)
			{
				new ScopeError("Newsfeed already started");
			}
			
			timer = new Timer();
		}
		
		/*** Creates the composite  ***/  
		List<MessageDestination> allParticipantDestinations = new LinkedList<>();
		
		/* Creates the composite destination */
		for(ParticipantEntry participant : this.participants)
		{
			allParticipantDestinations.add(participant.toClientDestination);
		}
	
		final MessageDestination allParticipantsDestination = 
				this.messengingService.createComposite(allParticipantDestinations);
		
		
		/* Creates an array of composite destinations for the connected nodes
		 * of each node.  Array is indexed by the node id */
		final MessageDestination connectedDestinations[] = 
						new MessageDestination[this.graph.getNumNodes()];
		
		
		Map<Integer, List<MessageDestination>> destinationLists = new HashMap<>();
		
		for(Node node : this.graph.getNodes())
		{
			destinationLists.put(node.getID(), new LinkedList<MessageDestination>());
		}
		
		/*  */
		for(Node node : this.graph.getNodes())
		{
			/* Message that contains a list of neighbors */
			JSONObject neighborData = new JSONObject();
		
			neighborData.element("type", "neighbors");
			
			Map<String, JSONObject> neighbors = new HashMap<>();
			
			System.out.printf("Building composite queue for %d\n", node.getID());
			
			for(Node connectedNode: this.graph.getConnectedNodes(node))
			{
				List<MessageDestination> destinationList = 
						destinationLists.get(connectedNode.getID());

				destinationList.add(participants[node.getID()].toClientDestination);		
				
				
				/* Adds the neighbor to the message list */
				neighbors.put(Integer.toString(connectedNode.getID()), 
						new JSONObject()
								.element("id", connectedNode.getID())
								.element("choice", (String)null));
				
				System.out.printf("\tConnected to %d: %s\n", connectedNode.getID(), participants[connectedNode.getID()].toClientDestination.toString());
			}
			
			
			
			neighborData.element("neighbors", neighbors);
		
	
			
			/* Sends the list of neighbors to the client */
			participants[node.getID()].toClientDestination.sendMessage(
					new JSONObject().element("msgType", "data")
					                .element("data", neighborData)
					                .toString());
		}
		
		for(Entry<Integer, List<MessageDestination>> entry : destinationLists.entrySet())
		{
			connectedDestinations[entry.getKey()] = 
					this.messengingService.createComposite(entry.getValue());
			
		}
		
		/* Records the participants */
		this.recordParticipants();
		
		/* Creates the thread pool executor */
		ThreadPoolExecutor executor = new ThreadPoolExecutor(
				4, 20, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		
		List<FutureTask<?>> tasks = new LinkedList<>();
		
		System.out.printf("Highest iterations = %d\n", this.script.getHighestIteration());
		
		for(int i=0; i<=this.script.getHighestIteration(); i++)
		{
			for(Event event : this.script.getEventsForIteration(i))
			{
				/* News event */
				if(event instanceof NewsEvent)
				{
					NewsEvent newsEvent = (NewsEvent)event;
					
					tasks.add(
						new NewsFeedTask(
							allParticipantsDestination, 
							newsEvent.getMessage(), 
							newsEvent.getDuration()));
					
				}else if(event instanceof ChoiceEvent)
				{
					ChoiceEvent choiceEvent = (ChoiceEvent)event;
					
					List<ParticipantEntry> taskParticipants = 
							new LinkedList<>();
					
					
					for(int pIndex : choiceEvent.getParticipants())
					{
						taskParticipants.add(participants[pIndex]);
					}
					
					tasks.add(
						new ChoiceTask(
							taskParticipants, 
							choiceEvent.getMessage(), 
							choiceEvent.getChoices(),
							choiceEvent.getDuration()));
					
				}else
				{
					throw new ScopeError("Unknown script event type");
				}
			}	
			
			System.out.printf("Iteration = %d\n", i);
			
			/* Runs the tasks */
			for(FutureTask<?> task : tasks)
			{
				System.out.printf("Starting task %s\n", task);
				executor.execute(task);
			}
			
			
			/* Waits for completion */
			for(FutureTask<?> task : tasks)
			{
				try 
				{
					@SuppressWarnings("unchecked")
					Map<String, String> participantResults = 
												(Map<String, String>)task.get();
					
					if(participantResults != null)
					{
						for(Map.Entry<String, String> participantResult : participantResults.entrySet())
						{
							if(this.participantIDToIndexMap.containsKey(participantResult.getKey()))
							{
								int participantIndex = this.participantIDToIndexMap.get(participantResult.getKey());
							
								this.recordChoice(i, participantIndex, participantResult.getValue());
								
								System.out.printf("Node: %d sending to %s\n", 
										participantIndex, 
										connectedDestinations[participantIndex].getName());
								executor.execute(
									new NeighborUpdateTask(
										connectedDestinations[participantIndex], 
										participantIndex, 
										participantResult.getValue()));
							}
						}
					}
					
					
					
				}catch (InterruptedException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch (ExecutionException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		executor.shutdown();
	}
	
	public StorageService getStorageService() {
		return storageService;
	}

	public String getJoinQueueName()
	{
		return this.joinHandler.getJoinQueueName();
	}
	
	public boolean hasActivated()
	{
		return this.activated;
	}
	
	public boolean hasStarted()
	{
		return this.started;
	}
	
	public boolean isCollectingParticipants()
	{
		return this.joinHandler.hasStarted() && 
				this.joinHandler.getNumParticipants() < this.requiredNumOfParticpants;
	}
	
	public int getNumberofParticipants()
	{
		return this.participantIDToIndexMap.size();
	}
	
	public int getCurrentNumberOfParticipants()
	{
		return this.requiredNumOfParticpants;
	}

	public String getSessionID() 
	{
		return sessionID;
	}
}