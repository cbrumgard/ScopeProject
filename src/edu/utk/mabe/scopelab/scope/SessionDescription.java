package edu.utk.mabe.scopelab.scope;

public class SessionDescription 
{
	/* Instance variables */
	final protected String sessionID;
	final protected String joinQueueName;
	
	
	public SessionDescription(String sessionID, String joinQueueName) 
	{
		this.sessionID     = sessionID;
		this.joinQueueName = joinQueueName;
	}
	
	public String getSessionID() 
	{
		return sessionID;
	}

	public String getJoinQueueName() 
	{
		return joinQueueName;
	}
}
