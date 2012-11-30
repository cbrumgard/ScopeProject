package edu.utk.mabe.scopelab.scope.admin.action;

import javax.servlet.ServletContext;

import net.sf.json.JSONObject;

import org.apache.struts2.util.ServletContextAware;

import edu.utk.mabe.scopelab.scope.BaseScopeAction;
import edu.utk.mabe.scopelab.scope.ScopeError;
import edu.utk.mabe.scopelab.scope.ScopeServer;

public class ActivateSessionAction extends BaseScopeAction 
	implements ServletContextAware
{
	/* Serialization stuff*/
	private static final long serialVersionUID = 5909295960574393150L;

	/* Instance variables */
	protected String sessionID;
	protected ServletContext servletContext = null;
	
	
	public ActivateSessionAction() 
	{
		
	}
	

	@Override
	public String execute() throws Exception 
	{
		/* Checks that the sessionID is valid */
		if(sessionID == null)
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
		
		try
		{
			/* Starts the session */
			scopeServer.startSession(sessionID);
		
		}catch(ScopeError e)
		{
			return setErrorMessage(e.getMessage());
		}
		
		/* Return null */
		return setDataMessage(new JSONObject().element("started", true));
	}

	public String getSessionID() 
	{
		return sessionID;
	}

	public void setSessionID(String sessionID) 
	{
		this.sessionID = sessionID;
	}

	@Override
	public void setServletContext(ServletContext servletContext) 
	{
		this.servletContext = servletContext;
	}
	
	public ServletContext getServletContext() 
	{
		return servletContext;
	}

}
