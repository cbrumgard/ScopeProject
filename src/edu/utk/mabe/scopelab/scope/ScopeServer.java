package edu.utk.mabe.scopelab.scope;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.jms.JMSException;
import javax.naming.NamingException;

import edu.utk.mabe.scopelab.scope.admin.service.GraphService.Graph;
import edu.utk.mabe.scopelab.scope.admin.service.ScriptService.Script;
import edu.utk.mabe.scopelab.scope.admin.service.StorageService;
import edu.utk.mabe.scopelab.scope.admin.service.messenging.MessengingException;
import edu.utk.mabe.scopelab.scope.admin.service.messenging.MessengingService;
import edu.utk.mabe.scopelab.scope.admin.service.session.Session;
import edu.utk.mabe.scopelab.scope.admin.service.session.SessionService;
import edu.utk.mabe.scopelab.scope.admin.service.websocket.WebSocketService;

public class ScopeServer
{
	/* Constants */
	final protected String BROKER_NAME = "Scope";

	/* Instance variables */
	final protected MessengingService messagingService;
	final protected StorageService storageService;
	final protected SessionService sessionService;
	final protected WebSocketService websocketService;
	
	final Map<String, Session> sessionMap = new HashMap<>();
	
	
	public ScopeServer(String webSocketHostname, int webSocketPort) 
			throws Exception 
	{
		/* Creates a messaging service */
		messagingService = new MessengingService(
									webSocketHostname, webSocketPort);
		
		/* Create the backend storage service */
		storageService = new StorageService();
		
		/* Session service */
		sessionService = new SessionService(storageService);
		
		/* WebSocket service */
		websocketService = new WebSocketService();
		
		System.out.println("Message broker setup");
	}
	
	public Session createSession(String sessionID, Graph graph, Script script) 
			throws SQLException, JMSException, ScopeError 
	{		
		/* Session already exists */
		if(storageService.doesSessionExist(sessionID) == true)
		{
			throw new ScopeError("Session already exists");
		}
		
		/* Create the session */
		Session session = sessionService.createSession(
				sessionID, graph, script, messagingService, storageService);
		
		/* Store the graph */
		storageService.storeGraph(graph);
		
		/* Store the script */
		storageService.storeScript(script);
		
		/* Store the session */
		storageService.storeSession(session, graph.getGraphID(), script.getScriptID());
		
		/* Puts the session into the session map */
		this.sessionMap.put(sessionID, session);
		
		/* Return the session */
		return session;
	}
	
	public String createMessageQueue() throws MessengingException
	{
		return messagingService.createQueue().getName();
	}
	
	public Session getSession(String sessionID) throws ScopeError
	{
		Session session = sessionMap.get(sessionID);
		
		if(session == null)
		{
			throw new ScopeError("No such session");
		}
		
		return session;
	}
	
	public void activateSession(String sessionID) 
		throws ScopeError, SQLException, MessengingException
	{
		Session session = sessionMap.get(sessionID);
		
		if(session == null)
		{
			throw new ScopeError("No such session");
		}
		
		session.activate();
	}
	
	public void startSession(String sessionID) 
		throws ScopeError, MessengingException, SQLException
	{
		Session session = sessionMap.get(sessionID);
		
		if(session == null)
		{
			throw new ScopeError("No such session");
		}
		
		session.start();
	}
	
	public Iterable<SessionDescription> getActiveSessions() throws SQLException
	{
		return storageService.getActiveSessions();
	}
	
	public MessengingService getMessengingService()
	{
		return messagingService;
	}
	
	public WebSocketService getWebsocketService()
	{
		return this.websocketService;
	}
	
	public void start() 
			throws JMSException, ClassNotFoundException, SQLException, 
			NamingException, Exception
	{
		/* Starts the messaging service */
		messagingService.start();
	
		
		/* Creates a local connection to the local broker */
//		new Timer().scheduleAtFixedRate(
//				new TimerTask()
//				{
//					javax.jms.Session session = null;
//					MessageProducer producer = null;
//					
//					
//					{
//						Connection connection = messagingService.getConnection();
//						
//
//						System.out.printf("Connection = %s\n", connection);
//
//						session = connection.createSession(
//								false, javax.jms.Session.AUTO_ACKNOWLEDGE);
//						
//						Topic destination = session.createTopic("test");
//						producer = session.createProducer(destination);
//						
//						producer.setDeliveryMode(DeliveryMode.PERSISTENT);
//					}
//
//					@Override
//					public void run() 
//					{
//						try 
//						{
//							TextMessage msg = session.createTextMessage("hello");
//							
//							producer.send(msg);
//							
//							
//						}catch(JMSException e) 
//						{
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						
//					}
//					
//				}, 0, 10*1000);
		
		
		/* Schedule repeated task to register server */
		new Timer().scheduleAtFixedRate(
				new TimerTask()
				{
					@Override
					public void run() 
					{
						try 
						{
							storageService.registerServer(
									messagingService.getWebSocketURI(), 2*30*1000);
						}catch(Exception e) 
						{
							e.printStackTrace();
						}
					}
					
				}, 0, 30*1000);
	}
}
