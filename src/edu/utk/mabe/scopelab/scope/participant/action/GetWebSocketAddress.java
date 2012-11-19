package edu.utk.mabe.scopelab.scope.participant.action;

import javax.servlet.ServletContext;

import net.sf.json.JSONObject;

import org.apache.struts2.util.ServletContextAware;

import edu.utk.mabe.scopelab.scope.BaseScopeAction;
import edu.utk.mabe.scopelab.scope.ScopeServer;

public class GetWebSocketAddress extends BaseScopeAction 
	implements ServletContextAware
{
	/* Serialization stuff */
	private static final long serialVersionUID = -1001914882563007722L;

	/* Instance variables */
	protected ServletContext servletContext = null;
	protected ScopeServer scopeServer 		= null;
	
	
	public GetWebSocketAddress() 
	{
		
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
		
		/* Builds the response to the client */
		JSONObject response = new JSONObject();

		response.element("uri", scopeServer.getMessengingService().getWebSocketURI());
		
		/* Return the result as a stream */
		return setDataMessage(response);
	}
}
