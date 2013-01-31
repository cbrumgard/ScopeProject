package edu.utk.mabe.scopelab.scope.admin.service.messenging;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import net.sf.json.JSONObject;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.ProducerBrokerExchange;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.region.DestinationFilter;
import org.apache.activemq.broker.region.DestinationInterceptor;
import org.apache.activemq.broker.region.virtual.CompositeDestinationFilter;
import org.apache.activemq.broker.region.virtual.VirtualTopic;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;

public class MessengingService 
{
	/* Constants */
	final static protected String NEW_SESSION_ANNONCEMENT_TOPIC = "scope.new_sessions";
	final static protected String SESSION_JOIN_TOPIC_FORMAT = "scope.new_sessions.%s";
	
	/* Instance variables */
	final protected URI webSocketAddress;
	final protected BrokerService broker;
	final protected ActiveMQConnectionFactory connectionFactory;
	final protected TransportConnector webSocketConnector;
	
	protected Connection connection = null;
	protected Session session = null;
	
	public MessengingService(String webSocketHostname, int webSocketPort) 
			throws Exception 
	{
		/* Forms the webSocketAddress */
		webSocketAddress = URI.create(
				String.format("ws://%s:%d", webSocketHostname, webSocketPort));
		
		/* Creates the embedded broker */
		//broker = BrokerFactory.createBroker(brokerURI, false);
		broker = new BrokerService();
		
		
		/* Sets the destination interceptor */
		/*broker.setDestinationInterceptors(
				new DestinationInterceptor[] 
					{ 
						new CustomDestinationInterceptor(compositeDestinations) 
					});*/
		
		/* Configures the broker */
		//broker.setBrokerName(BROKER_NAME);
		//broker.setUseShutdownHook(false);
		
		//NetworkConnector connector = broker.addNetworkConnector("static://"+address);
		//NetworkConnector connector = broker.addNetworkConnector(String.format("static://tcp://%s:%d", hostname, port));
		//connector.setDuplex(true);
		
		/* Creates the local vm connector for clients */
		//broker.addConnector("tcp://localhost:61617");
		
		//SslContext sslContext = SslContext.getCurrentSslContext();
		//broker.setSslContext(sslContext);
		
		
		/* Creates the websocket connector for the clients */
		webSocketConnector = broker.addConnector(webSocketAddress);
		webSocketConnector.setName("websocket");
		

		/* Makes a ActiveMQConnectionFactory */
		connectionFactory = new ActiveMQConnectionFactory(broker.getVmConnectorURI());
	}
	
	public void start() throws Exception
	{
		/* Start the broker and wait for it to start before returning */
		broker.start();
		broker.waitUntilStarted();
		
		System.out.println("Broker started");
		
		/* Creates a new connection */
		connection = connectionFactory.createConnection();
		connection.start();
		
		session = connection.createSession(false, 
								javax.jms.Session.AUTO_ACKNOWLEDGE);
	}

	public String getWebSocketURI() throws Exception
	{
		return String.format("%s/stomp", 
				webSocketConnector.getPublishableConnectString());
	}
	
	public String getNewSessionAnnoncementTopicName()
	{
		return NEW_SESSION_ANNONCEMENT_TOPIC;
	}
	

//	public void sendMessageToQueue(String queueName, String message) 
//			throws JMSException
//	{
//		MessageProducer producer = null;
//		
//		try
//		{
//			/* Gets the receive queue */
//			ActiveMQDestination receiveQueue =				
//					ActiveMQDestination.createDestination(queueName, 
//							ActiveMQDestination.QUEUE_TYPE);
//			
//			producer = session.createProducer(receiveQueue);
//
//			producer.send(session.createTextMessage(message));
//
//		}finally
//		{
//			if(producer != null)
//			{
//				producer.close();
//			}
//		}
//	}
//	
//	public void sendMessageToTopic(String topic, String message) 
//			throws JMSException
//	{
//		MessageProducer producer = null;
//	
//		try
//		{
//			Topic destination = session.createTopic(topic);
//
//			producer = session.createProducer(destination);
//			
//			
//			producer.setDeliveryMode(DeliveryMode.PERSISTENT);
//
//			producer.send(session.createTextMessage(message));
//			
//		}finally
//		{
//			if(producer != null)
//			{
//				producer.close();
//			}
//		}
//	}
	
	public void sendMessage(MessageDestination destination, String message) 
			throws MessengingException
	{
		MessageProducer producer = null;
		
		try 
		{
			producer = session.createProducer(
					((MessageDestinationImpl)destination).getDestination());
		
			producer.send(session.createTextMessage(message));
			
		}catch(JMSException e) 
		{
			throw new MessengingException(e);
			
		}finally
		{
			if(producer != null)
			{
				try 
				{
					producer.close();
				
				}catch (JMSException e) 
				{
					throw new MessengingException(e);
				}
			}
		}
	}
	
	
	
	
	public String getSessionJoinDestination(String sessionID)
	{
		return String.format(SESSION_JOIN_TOPIC_FORMAT, sessionID);
	}
	
	/*public MessageProducer createQueue(String queueName, 
			MessageListener messageListener) throws JMSException
	{
		Queue destination = session.createQueue(queueName);
		
		
		session.createConsumer(destination).setMessageListener(messageListener);
		return session.createProducer(destination);
	}*/
	
	public MessageDestination createQueue(String queueName) 
			throws MessengingException
	{
		try 
		{
			Session session = connection.createSession(false, 
					javax.jms.Session.AUTO_ACKNOWLEDGE);
			
			return new MessageDestinationImpl(connection, session, session.createQueue(queueName));
		}catch(JMSException e) 
		{
			if(session != null)
			{
				try 
				{
					session.close();
				}catch(JMSException e1) 
				{
					e1.printStackTrace();
				}
			}
			
			throw new MessengingException(e);
		}
	}
	
	public MessageDestination createQueue() throws MessengingException
	{
		return createQueue(UUID.randomUUID().toString());
	}
	
	
	public MessageDestination createTopic(String name) 
			throws MessengingException
	{
		try 
		{
			return new MessageDestinationImpl(connection, session, 
					session.createTopic(name));
			
		}catch(JMSException e) 
		{
			throw new MessengingException(e);
		}
	}
	
	public MessageDestination createTopic() 
			throws MessengingException
	{
		return createTopic(UUID.randomUUID().toString());
	}
	
	public MessageDestination createComposite(
			Collection<MessageDestination> destinations)
	{
		StringBuilder compositeURL = new StringBuilder();
		
		for(MessageDestination destination : destinations)
		{
			Destination dest = ((MessageDestinationImpl)destination).getDestination();
			
			compositeURL.append(destination.toString().substring(8));
			compositeURL.append(',');
		}
		
		compositeURL.deleteCharAt(compositeURL.length()-1);
		
		return new CompositeMessageDestinationImpl(compositeURL.toString(), 
				destinations.toArray(
						new MessageDestinationImpl[destinations.size()]));
	}
}
