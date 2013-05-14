package edu.utk.mabe.scopelab.scope.admin.action;

import javax.servlet.ServletContext;

import edu.utk.mabe.scopelab.scope.BaseScopeAction;
import edu.utk.mabe.scopelab.scope.admin.service.StorageService;

public class GetBackendStoragePageAction extends BaseScopeAction 
{

	/* Serialization stuff */
	private static final long serialVersionUID = 665024698948300756L;

	/* Instance variables */
	protected ServletContext servletContext = null;

	

	public String execute() throws Exception
	{
		StorageService storageService = new StorageService();
		
	
		if(storageService.isInitialized() == false)
		{
			return setErrorMessage("Storage service is not initialized");
		}
		
		
		
		return null;
	}
}
