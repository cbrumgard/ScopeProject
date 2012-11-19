package edu.utk.mabe.scopelab.scope.admin.service;

import java.net.URI;
import java.util.UUID;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;

import javax.jms.Destination;
import javax.jms.JMSException;
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
import org.apache.activemq.broker.TransportConnector;
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
	

	public void sendMessageToQueue(String queueName, String message) 
			throws JMSException
	{
		MessageProducer producer = null;
		
		try
		{
			/* Gets the receive queue */
			ActiveMQDestination receiveQueue =				
					ActiveMQDestination.createDestination(queueName, 
							ActiveMQDestination.QUEUE_TYPE);
			
			producer = session.createProducer(receiveQueue);

			producer.send(session.createTextMessage(message));

		}finally
		{
			if(producer != null)
			{
				producer.close();
			}
		}
	}
	
	public void sendMessageToTopic(String topic, String message) 
			throws JMSException
	{
		MessageProducer producer = null;
	
		try
		{
			Topic destination = session.createTopic(topic);

			producer = session.createProducer(destination);
			
			
			producer.setDeliveryMode(DeliveryMode.PERSISTENT);

			producer.send(session.createTextMessage(message));
			
		}finally
		{
			if(producer != null)
			{
				producer.close();
			}
		}
	}
	
	public void sendNewSessionMessage(String sessionID, String joinDestination) 
			throws JMSException
	{
		/* Sends the json message */
		sendMessageToTopic(NEW_SESSION_ANNONCEMENT_TOPIC, 
				new JSONObject().element("sessionID", sessionID)
				                .element("joinDestination", joinDestination)
				                .toString());
	}
	
	public String getSessionJoinDestination(String sessionID)
	{
		return String.format(SESSION_JOIN_TOPIC_FORMAT, sessionID);
	}
	
	public MessageProducer createQueue(String queueName, 
			MessageListener messageListener) throws JMSException
	{
		Queue destination = session.createQueue(queueName);
		
		
		session.createConsumer(destination).setMessageListener(messageListener);
		return session.createProducer(destination);
	}
	
	public Destination createQueue() throws JMSException
	{
		//return session.createTemporaryQueue();
		
		return session.createQueue(UUID.randomUUID().toString());
	}
	
	
	
	public class VirtualTopic
	{
		/* Constants */
		static final protected String VIRTUAL_TOPIC_PRODUCER_TOPIC_PATTERN="VirtualTopic.scope.%s";
		static final protected String VIRTUAL_TOPIC_CONSUMER_QUEUE_PATTERN="Consumer.%s.VirtualTopic.scope.%s";
		
		final protected String topicURL;
		final protected String topicName;
		
		public VirtualTopic(String topicName) 
		{
			this.topicName = topicName;
			this.topicURL = String.format(VIRTUAL_TOPIC_PRODUCER_TOPIC_PATTERN, topicName);
		}
		
		public String getTopicURL()
		{
			return this.topicURL;
		}
		
		public String getConsumerURL()
		{
			return String.format(VIRTUAL_TOPIC_CONSUMER_QUEUE_PATTERN, 
						UUID.randomUUID().toString(), this.topicName);
		}
	}
	
	public VirtualTopic createVirtualTopic(String name) throws JMSException
	{
		
		VirtualTopic virtualTopic = new VirtualTopic(name);
		
		
		/* Creates the topic */
		Topic topic = session.createTopic(virtualTopic.getTopicURL());
		
		
		
	
		//VirtualTopic topic = new VirtualTopic();
		//topic.setPrefix("prefix");
		//topic.setPostfix("postfix");
		//topic.setName("name");
		
		
		System.out.println(topic.getTopicName());
		//topic.intercept(destination);
		
		
		//VirtualTopic.create(this.broker.getBroker(), this.broker.getAdminConnectionContext(), new ActiveMQQueue());
	
		return virtualTopic;
	}
}
