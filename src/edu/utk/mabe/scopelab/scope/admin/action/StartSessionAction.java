package edu.utk.mabe.scopelab.scope.admin.action;

import javax.servlet.ServletContext;

import org.apache.struts2.util.ServletContextAware;

import edu.utk.mabe.scopelab.scope.BaseScopeAction;
import edu.utk.mabe.scopelab.scope.ScopeServer;


public class StartSessionAction extends BaseScopeAction 
	implements ServletContextAware
{
	/* Serialization stuff */ 
	private static final long serialVersionUID = 6034882521119840496L;
	
	
	/* Instance variables */
	protected ServletContext servletContext = null;
	private String		 sessionID	    = null;
	
	public StartSessionAction() 
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setServletContext(ServletContext servletContext) 
	{
		this.servletContext = servletContext;
	}

	public String getSessionID() 
	{
		return this.sessionID;
	}

	public void setSessionID(String sessionID) 
	{
		this.sessionID = sessionID;
	}
	
	@Override
	public String execute() throws Exception 
	{
		/* Checks that the sessionID is valid */
		if(getSessionID() == null)
		{
			return setErrorMessage("Must specify a valid sessionID");
		}
		
		/* Gets the scope server */
		ScopeServer scopeServer = (ScopeServer)servletContext.getAttribute(
				"edu.utk.mabe.scopelab.scope.ScopeServer");

		/* No server so not running */
		if(scopeServer == null)
		{
			return setErrorMessage("Server is not running");
		}
		
		
		
		
		return null;
	}

	
}
