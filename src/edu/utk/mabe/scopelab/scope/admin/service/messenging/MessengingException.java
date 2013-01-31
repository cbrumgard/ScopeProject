package edu.utk.mabe.scopelab.scope.admin.service.messenging;

public class MessengingException extends Exception 
{

	private static final long serialVersionUID = 1853529754924181079L;

	public MessengingException() 
	{
		
	}

	public MessengingException(String message) 
	{
		super(message);
	}

	public MessengingException(Throwable cause) 
	{
		super(cause);
	}

	public MessengingException(String message, Throwable cause) 
	{
		super(message, cause);
	}
}
