package edu.utk.mabe.scopelab.scope.participant.action;

import javax.servlet.ServletContext;

import net.sf.json.JSONObject;

import org.apache.struts2.util.ServletContextAware;

import edu.utk.mabe.scopelab.scope.BaseScopeAction;
import edu.utk.mabe.scopelab.scope.ScopeServer;
import edu.utk.mabe.scopelab.scope.SessionDescription;

public class GetActiveSessionListAction extends BaseScopeAction 
	implements ServletContextAware
{
	/* Serialization */
	private static final long serialVersionUID = -4686774060537507722L;
	
	/* Instance variables */
	protected ServletContext servletContext = null;
	

	public void setServletContext(ServletContext servletContext) 
	{
		this.servletContext = servletContext;
	}
	
	public ServletContext getServletContext() 
	{
		return servletContext;
	}

	@Override
	public String execute() throws Exception 
	{
		/* Gets the scope server */
		ScopeServer scopeServer = (ScopeServer)servletContext.getAttribute(
				"edu.utk.mabe.scopelab.scope.ScopeServer");

		/* No server so not running */
		if(scopeServer == null)
		{
			return setErrorMessage("Server is not running");
		}
		
		/* JSON object for holding the results */
		JSONObject response = new JSONObject();
		
		
		/* Gets a list of the sessions */
		final Iterable<SessionDescription> sessions = 
				scopeServer.getActiveSessions();
	
		for(SessionDescription sessionDescription : sessions)
		{
			response.element(sessionDescription.getSessionID(), 
				new JSONObject().element("sessionID", sessionDescription.getSessionID())
								.element("joinDestination", sessionDescription.getJoinQueueName()));
		}
		
		/* Send back sessions as a json object */
		return setDataMessage(response);
	}
}
