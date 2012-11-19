package edu.utk.mabe.scopelab.scope.admin.action;

import javax.servlet.ServletContext;
import org.apache.struts2.util.ServletContextAware;

import edu.utk.mabe.scopelab.scope.BaseScopeAction;
import edu.utk.mabe.scopelab.scope.JSONResponse;
import edu.utk.mabe.scopelab.scope.ScopeServer;


public class GetServerStatusPage extends BaseScopeAction 
	implements ServletContextAware
{
	private static final long serialVersionUID = 7412266205082531531L;
	
	/* Instance variables */
	protected ServletContext servletContext = null;
	protected ScopeServer scopeServer 		= null;
	

	@Override
	public void setServletContext(ServletContext servletContext) 
	{
		this.servletContext = servletContext;
	}
	
	public ServletContext getServletContext() 
	{
		return servletContext;
	}
	
	public ScopeServer getScopeServer()
	{
		return scopeServer;
	}

	@Override
	public String execute() throws Exception 
	{
		/* Gets the scope server */
		scopeServer = (ScopeServer)servletContext.getAttribute(
				"edu.utk.mabe.scopelab.scope.ScopeServer");

		/* No server so not running */
		if(scopeServer == null)
		{
			return setErrorMessage("Server is not running");
		}
		
		/* Successful return */
		return "serverStatusPage";
	}
}
