package edu.utk.mabe.scopelab.scope.admin.action;

import javax.servlet.ServletContext;

import net.sf.json.JSONObject;

import org.apache.struts2.util.ServletContextAware;

import edu.utk.mabe.scopelab.scope.BaseScopeAction;
import edu.utk.mabe.scopelab.scope.ScopeServer;
import edu.utk.mabe.scopelab.scope.admin.service.session.Session;

public class GetSessionStatus extends BaseScopeAction 
	implements ServletContextAware
{
	/* Java serialization stuff */
	private static final long serialVersionUID = 1L;

	/* Instance variables */
	protected ServletContext servletContext = null;
	private String sessionID	= null;
	
	
	public GetSessionStatus() 
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public String execute() throws Exception 
	{
		/* Checks the input */
		if(getSessionID() == null)
		{
			return setErrorMessage("SessionID was not sent");
		}
		
		/* Gets the scope server */
		ScopeServer scopeServer = (ScopeServer)getServletContext().getAttribute(
				"edu.utk.mabe.scopelab.scope.ScopeServer");

		/* No server so not running */
		if(scopeServer == null)
		{
			return setErrorMessage("Server is not running");
		}
	
		/* Gets the sessionID */
		Session session = scopeServer.getSession(this.getSessionID());
		
		/* Returns data about the session as a json object */
		return setDataMessage(
				new JSONObject()
					.element("Active", session.hasActivated())
					.element("CollectingParticipants", session.isCollectingParticipants())
					.element("NumberParticipants", session.getNumberofParticipants())
					.element("MaxNumberOfParticipants", session.getCurrentNumberOfParticipants()));
		
	}

	public ServletContext getServletContext() 
	{
		return servletContext;
	}

	public void setServletContext(ServletContext servletContext) 
	{
		this.servletContext = servletContext;
	}

	public String getSessionID() 
	{
		return sessionID;
	}

	public void setSessionID(String sessionID) 
	{
		this.sessionID = sessionID;
	}
}
