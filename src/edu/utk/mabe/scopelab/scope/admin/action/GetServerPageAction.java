package edu.utk.mabe.scopelab.scope.admin.action;

import edu.utk.mabe.scopelab.scope.BaseScopeAction;

public class GetServerPageAction extends BaseScopeAction 
{
	/* Serialization stuff */
	private static final long serialVersionUID = 1178370856192658488L;

	
	public GetServerPageAction() 
	{
		// Do nothing
	}
	
	@Override
	public String execute() throws Exception 
	{
		return "configureServerPage";
	}

}
