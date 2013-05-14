package edu.utk.mabe.scopelab.scope.admin.action;

import javax.servlet.ServletContext;

import net.sf.json.JSONObject;

import org.apache.struts2.util.ServletContextAware;

import edu.utk.mabe.scopelab.scope.BaseScopeAction;
import edu.utk.mabe.scopelab.scope.ScopeServer;
import edu.utk.mabe.scopelab.scope.admin.service.StorageService;

public class GetNamedScriptList extends BaseScopeAction 
	implements ServletContextAware 
{
	/* Serialization stuff */
	private static final long serialVersionUID = -6116957985686401460L;
	
	/* Instance variables */
	protected ServletContext servletContext = null;
	
	@Override
	public String execute() throws Exception 
	{
		synchronized(servletContext) 
		{
			/* Gets the scope server */
			ScopeServer scopeServer = (ScopeServer)servletContext.getAttribute(
					"edu.utk.mabe.scopelab.scope.ScopeServer");

			/* Error since there is already a server running */
			if(scopeServer == null)
			{
				return setErrorMessage("Server is not running");
			}
		}
	
		/* Gets an instance of the storage service */
		final StorageService storageService = new StorageService();
		
		if(storageService.isInitialized() == false)
		{
			return setErrorMessage("Storage service has not been initialized");
		}
		
		/* Build a response object with list of graph names */
		JSONObject response = new JSONObject();
		
		for(String scriptName : storageService.getNamedScripts())
		{
			response.accumulate("scriptNames", scriptName);
		}
		
		/* Success */
		return setDataMessage(response);
	}
	
	@Override
	public void setServletContext(ServletContext servletContext) 
	{
		this.servletContext = servletContext;
	}
}
