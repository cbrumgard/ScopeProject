package edu.utk.mabe.scopelab.scope.participant.action;

import javax.servlet.ServletContext;

import net.sf.json.JSONObject;

import org.apache.struts2.util.ServletContextAware;

import edu.utk.mabe.scopelab.scope.BaseScopeAction;
import edu.utk.mabe.scopelab.scope.ScopeServer;

public class GetMessageQueueAction extends BaseScopeAction 
	implements ServletContextAware
{
	/* Serialization stuff */
	private static final long serialVersionUID = 545740541514267543L;

	
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
	
	
	public GetMessageQueueAction() 
	{
		
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

		/* Creates the queue */
		String queueName = scopeServer.createMessageQueue();
		
		/* Returns a message with the name of the queue */
		return setDataMessage(new JSONObject().element("queueName", queueName));
	}
}
