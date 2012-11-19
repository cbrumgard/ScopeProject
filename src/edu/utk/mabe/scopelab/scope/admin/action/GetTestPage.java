package edu.utk.mabe.scopelab.scope.admin.action;

import java.io.InputStream;

import org.apache.kahadb.util.ByteArrayInputStream;

import edu.utk.mabe.scopelab.scope.BaseScopeAction;

public class GetTestPage extends BaseScopeAction 
{
	/* Serialization stuff */
	private static final long serialVersionUID = -250173586182711171L;
		
	
	private InputStream inputStream = null;

	public InputStream getInputStream() 
	{
		System.out.println("getting the inputstream");
		return inputStream;
	}

	@Override
	public String execute() throws Exception 
	{
		System.out.println("Inside of execute");
		
		inputStream = new ByteArrayInputStream("Hello World".getBytes());
		
		System.out.printf("Finished with execute: %s\n", SUCCESS);
		
		return SUCCESS;
	}
}
