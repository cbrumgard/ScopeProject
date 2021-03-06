package edu.utk.mabe.scopelab.scope.admin.action;

import edu.utk.mabe.scopelab.scope.BaseScopeAction;
import edu.utk.mabe.scopelab.scope.admin.service.StorageService;

public class GetConfigureStoragePageAction extends BaseScopeAction
{
	/* Serialization crap */
	private static final long serialVersionUID = -3522362080870249755L;

	protected boolean	isInitialized = false; 
	
	
	public String execute() throws Exception
	{
		String nextPage = null;


		/* New BackendStorage service */
		StorageService backendStorageService = 
				new StorageService();

		isInitialized = backendStorageService.isInitialized();

		System.out.printf("isInitialized = %b\n", isInitialized);
		
		/* Success */
		nextPage = "configureStoragePage";

		/* Tells struts which page to go to next */
		return nextPage;
	}
	

	public boolean isInitialized() 
	{
		return isInitialized;
	}


	public void setInitialized(boolean isInitialized) 
	{
		this.isInitialized = isInitialized;
	}
}
