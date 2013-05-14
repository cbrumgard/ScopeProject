package edu.utk.mabe.scopelab.scope.admin.action;


import javax.servlet.ServletContext;

import net.sf.json.JSONObject;

import org.apache.struts2.util.ServletContextAware;

import edu.utk.mabe.scopelab.scope.BaseScopeAction;
import edu.utk.mabe.scopelab.scope.ScopeServer;

public class StartServerAction extends BaseScopeAction 
	implements ServletContextAware
{
	private static final long serialVersionUID = -6012192452709248669L;

	/* Instance variables */
	protected ServletContext servletContext = null;
	protected String hostname = null;
	protected int port        = 0;
	
	@Override
	public void setServletContext(ServletContext servletContext) 
	{
		this.servletContext = servletContext;
	}

	public String getHostname() 
	{
		return hostname;
	}

	public void setHostname(String hostname) 
	{
		this.hostname = hostname;
	}

	public int getPort() 
	{
		return port;
	}

	public void setPort(int port) 
	{
		this.port = port;
	}

	@Override
	public String execute() throws Exception 
	{
		System.out.println("Inside of Start Server Action");
		System.out.printf("Hostname = %s Port = %d\n", hostname, port);
	

		synchronized(servletContext) 
		{
			/* Gets the scope server */
			ScopeServer scopeServer = (ScopeServer)servletContext.getAttribute(
					"edu.utk.mabe.scopelab.scope.ScopeServer");

			/* Error since there is already a server running */
			if(scopeServer != null)
			{
				return setErrorMessage("Server is already running");
			}

			System.out.println("Inside of Start Server Action 1");
			
			/* Creates and starts the scope server */
			scopeServer = new ScopeServer(hostname, port);
			
			System.out.println("Inside of Start Server Action 2");
			scopeServer.start();
			
			System.out.println("Inside of Start Server Action 3");

			/* Stores the references to the scope server for later use */
			servletContext.setAttribute("edu.utk.mabe.scopelab.scope.ScopeServer", 
					scopeServer);
		}

		/* Send a success full result back */
		return setDataMessage(new JSONObject());
	}
}
