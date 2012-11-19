package edu.utk.mabe.scopelab.scope;

public class ScopeError extends Exception 
{
	/* Serialization stuff */
	private static final long serialVersionUID = -1915492789949802299L;

	public ScopeError() 
	{
		
	}

	public ScopeError(String message) 
	{
		super(message);
	}

	public ScopeError(Throwable cause) 
	{
		super(cause);
	}

	public ScopeError(String message, Throwable cause) 
	{
		super(message, cause);
	}

	public ScopeError(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) 
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
