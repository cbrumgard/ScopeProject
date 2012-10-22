package edu.utk.mabe.scopelab.scope;

import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.network.NetworkConnector;

import edu.utk.mabe.scopelab.scope.admin.service.BackendStorageService;

public class ScopeServer
{
	/* Constants */
	final protected String BROKER_NAME = "Scope";
	final protected String address = "tcp://localhost:61616";
	
	/* Instance variables */
	final protected BrokerService broker;
	final protected ActiveMQConnectionFactory connectionFactory;
	
	
	public ScopeServer(String hostname, int port) throws Exception 
	{
		/* Creates the broker */
		broker = new BrokerService();
		
		broker.setBrokerName(BROKER_NAME);
		broker.setUseShutdownHook(true);
		
		//NetworkConnector connector = broker.addNetworkConnector("static://"+address);
		NetworkConnector connector = broker.addNetworkConnector(String.format("static://tcp://%s:%d", hostname, port));
		connector.setDuplex(true);
		
		broker.addConnector(address);
		
		connectionFactory = new ActiveMQConnectionFactory(
				String.format("peer://groupA/%s?persistent=false", BROKER_NAME));
		
		System.out.println("Message broker setup");
	}
	
	
	public void start() 
			throws JMSException, ClassNotFoundException, SQLException, 
			NamingException, Exception
	{
		/* Starts the broker */
		broker.start();
		
		/* Creates a local connection to the local broker */
		Connection connection = connectionFactory.createConnection();
		
		System.out.printf("Connection = %s\n", connection);
	
		connection.start();
		
		/* Access the backend store */
		final BackendStorageService backendStorageService = 
				new BackendStorageService("localhost", "9160"); //new BackendStorageService();
		

		/* Schedule repeated task to register server */
		new Timer().scheduleAtFixedRate(
				new TimerTask()
				{
					@Override
					public void run() 
					{
						try 
						{
							backendStorageService.registerServer(address, 2*30*1000);
						}catch (SQLException e) 
						{
							e.printStackTrace();
						}
					}
					
				}, 0, 30*1000);
	}
	
	public static void main(String[] args) throws NumberFormatException, Exception
	{
		ScopeServer scopeServer = new ScopeServer(args[0], Integer.parseInt(args[1]));
		scopeServer.start();
	}
}
