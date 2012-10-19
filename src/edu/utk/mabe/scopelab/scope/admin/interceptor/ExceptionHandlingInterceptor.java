package edu.utk.mabe.scopelab.scope.admin.interceptor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

import edu.utk.mabe.scopelab.scope.JSONResponse;

public class ExceptionHandlingInterceptor implements Interceptor 
{

	/* Serialization stuff */
	private static final long serialVersionUID = -8776435111645631273L;

	
	public ExceptionHandlingInterceptor() 
	{
		
	}

	@Override
	public void init() 
	{
		// Do nothing
	}
	
	@Override
	public void destroy() 
	{
		// Do nothing
	}


	@Override
	public String intercept(ActionInvocation actionInvocation) throws Exception 
	{
		try
		{
			/* Calls the action and returns the result */
			return actionInvocation.invoke();
			
		}catch(Throwable e)
		{
			/* Gets the stack trace */
			ByteArrayOutputStream stackTrace = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(stackTrace));
			
			/* Sets the response */
			actionInvocation.getInvocationContext().put("inputStream", 
					new ByteArrayInputStream(
							JSONResponse.createStackTraceResponse(
									stackTrace.toString("utf-8")).toBytes()));
			
			
			/* Stream output */
			return "streamOutput";
		}
	}
}
