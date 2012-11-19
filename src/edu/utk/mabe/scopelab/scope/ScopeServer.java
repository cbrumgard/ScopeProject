package edu.utk.mabe.scopelab.scope;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.NamingException;

import org.apache.commons.collections.map.HashedMap;

import edu.utk.mabe.scopelab.scope.admin.service.BackendStorageService;
import edu.utk.mabe.scopelab.scope.admin.service.GraphService.Graph;
import edu.utk.mabe.scopelab.scope.admin.service.MessengingService;
import edu.utk.mabe.scopelab.scope.admin.service.SessionService;
import edu.utk.mabe.scopelab.scope.admin.service.SessionService.Session;

public class ScopeServer
{
	/* Constants */
	final protected String BROKER_NAME = "Scope";

	/* Instance variables */
	final protected MessengingService messagingService;
	final protected BackendStorageService storageService;
	final protected SessionService sessionService;
	final Map<String, Session> sessionMap = new HashMap<>();
	
	
	public ScopeServer(String webSocketHostname, int webSocketPort) 
			throws Exception 
	{
		/* Creates a messaging service */
		messagingService = new MessengingService(
									webSocketHostname, webSocketPort);
		
		/* Create the backend storage service */
		storageService = new BackendStorageService();
		
		/* Session service */
		sessionService = new SessionService(storageService);
		
		System.out.println("Message broker setup");
	}
	
	public Session createSession(String sessionID, Graph graph) 
			throws SQLException, JMSException, ScopeError 
	{		
		/* Session already exists */
		if(storageService.doesSessionExist(sessionID) == true)
		{
			System.out.println("Session exists!!!");
			throw new ScopeError("Session already exists");
		}
		
		/* Create the session */
		Session session = sessionService.createSession(
				sessionID, graph, messagingService, storageService);
		
		/* Store the session */
		storageService.storeSession(session);
		
		/* Store the graph */
		storageService.storeGraph(UUID.randomUUID(), graph);
		
		/* Puts the session into the session map */
		this.sessionMap.put(sessionID, session);
		
		/* Return the session */
		return session;
	}
	
	public String createMessageQueue() throws JMSException
	{
		return messagingService.createQueue().toString();
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
	
	public void startSession(String sessionID) 
			throws ScopeError, JMSException, SQLException
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
