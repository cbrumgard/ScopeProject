package edu.utk.mabe.scopelab.scope.admin.service.messenging;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

class MessageDestinationImpl implements MessageDestination 
{
	/* Instance variables */
	protected Connection connection;
	protected Session session;
	protected Destination destination;
	
    protected MessageProducer producer = null;
    protected MessageConsumer consumer = null;
	
	MessageDestinationImpl(Connection connection, Session session, 
			Destination destination) 
	{
		this.setConnection(connection);
		this.setSession(session);
		this.setDestination(destination);
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
						consumer = getSession().createConsumer(getDestination());
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
			
			getConnection().setExceptionListener(new javax.jms.ExceptionListener()
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
		System.out.printf("Receiving on %s\n", this.destination);
		
		try 
		{
			if(consumer == null)
			{
				synchronized(this) 
				{
					if(consumer == null)
					{
						consumer = getSession().createConsumer(getDestination());
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
	
	Connection getConnection() 
	{
		return connection;
	}

	void setConnection(Connection connection) 
	{
		this.connection = connection;
	}

	Session getSession() 
	{
		return session;
	}

	void setSession(Session session) 
	{
		this.session = session;
	}

	Destination getDestination() 
	{
		return destination;
	}

	void setDestination(Destination destination) 
	{
		this.destination = destination;
	}

	@Override
	public String getName() 
	{
		return this.destination.toString();
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

			if(session != null)
			{
				session.close();
			}
			
		}catch(JMSException e) 
		{
			throw new MessengingException(e);
		}
	}
}
