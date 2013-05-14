package edu.utk.mabe.scopelab.scope.admin.service.messenging;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQQueue;

public class CompositeMessageDestinationImpl implements MessageDestination 
{
	/* Instance variables */
	protected final String			   name;
	protected final MessageDestination[] destinations;
	protected final ActiveMQQueue 	destination;
	protected final Session 		session;
	protected final Connection      connection;
	
	protected MessageProducer producer = null;
	protected MessageConsumer consumer = null;
	
	CompositeMessageDestinationImpl(String name, MessageDestination[] destinations) 
	{
		this.name = name;
		this.destinations = destinations;
		
		
		/* Creates the address for the composite queue */
		StringBuilder compositeAddress = new StringBuilder();
		
		for(MessageDestination destination : destinations)
		{
			compositeAddress.append(destination.getName().replaceFirst("queue://", ""));
			compositeAddress.append(',');
		}
		
		compositeAddress.deleteCharAt(compositeAddress.length()-1);
		
		/* Creates the composite queue */
		this.destination = new ActiveMQQueue(compositeAddress.toString());
		
		this.session    = ((MessageDestinationImpl)destinations[0]).getSession();
		this.connection = ((MessageDestinationImpl)destinations[0]).getConnection();
	}

	@Override
	public void sendMessage(String message) throws MessengingException 
	{
		try 
		{
			if(producer == null)
			{
				synchronized(this) 
				{
					if(producer == null)
					{
						producer = session.createProducer(destination);
					}
				}
			}
		
			producer.send(session.createTextMessage(message));
			
		}catch(JMSException e) 
		{
			throw new MessengingException(e);
		}
	}
	
	@Override
	public void setMessageListener(final MessageListener listener)
			throws MessengingException 
	{
		try 
		{
			if(consumer == null)
			{
				synchronized(this) 
				{
					if(consumer == null)
					{
						consumer = session.createConsumer(destination);
					}
				}
			}
			
			consumer.setMessageListener(
				new javax.jms.MessageListener()
				{
					@Override
					public void onMessage(Message message) 
					{
						try 
						{
							/* Gets the text message */
							TextMessage txtMsg = (TextMessage)message;
							
							listener.onMessage(txtMsg.getText());
						}catch(JMSException e) 
						{
							listener.onError(e);
						}
					}
				});
			
			this.connection.setExceptionListener(new javax.jms.ExceptionListener()
				{
					@Override
					public void onException(JMSException e) 
					{
						listener.onError(e);
					}		
				});
			
			
			
		}catch(JMSException e) 
		{
			throw new MessengingException(e);
		}
		/*(for(MessageDestination destination : getDestinations())
		{
			destination.setMessageListener(listener);
		}*/
	}

	@Override
	public void clearMessageListener() throws MessengingException 
	{
		if(this.consumer != null)
		{
			try 
			{
				this.consumer.setMessageListener(null);
			}catch (JMSException e) 
			{
				throw new MessengingException(e);
			}
		}
	}
	
	@Override
	public String receiveMessage() throws MessengingException 
	{
		return this.receiveMessage(-1);
	}

	@Override
	public String receiveMessage(long timeout) throws MessengingException 
	{	
		try 
		{
			if(consumer == null)
			{
				synchronized(this) 
				{
					if(consumer == null)
					{
						consumer = session.createConsumer(destination);
					}
				}
			}
		
			consumer.setMessageListener(null);
			
			Message message = null;
			
			/* Wait forever */
			if(timeout < 0)
			{
				message = consumer.receive();
				
			/* Wait for specified timeout */
			}else if(timeout > 0)
			{
				message = consumer.receive(timeout);
			
			/* Nonblocking receive */
			}else
			{
				message = consumer.receiveNoWait();
			}
			
			if(message == null)
			{
				return null;
			}
			
			return ((TextMessage)message).getText();
			
		}catch(JMSException e) 
		{
			throw new MessengingException(e);
			
		}
	}
	@Override
	public String getName() 
	{
		return this.name;
	}

	MessageDestination[] getDestinations() 
	{
		return destinations;
	}

	@Override
	public void close() throws MessengingException 
	{
		try
		{
			if(producer == null)
			{
				synchronized(this) 
				{
					if(producer == null)
					{
						producer.close();
					}
				}
			}

			if(consumer != null)
			{
				synchronized(this) 
				{
					if(consumer != null)
					{
						consumer.close();
					}
				}
			}

		}catch(JMSException e) 
		{
			throw new MessengingException(e);
		}
	}
}
