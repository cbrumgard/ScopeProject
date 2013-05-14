package edu.utk.mabe.scopelab.scope.admin.action;

import edu.utk.mabe.scopelab.scope.BaseScopeAction;

public class GetLoadScriptPage extends BaseScopeAction 
{
	/* Serialization stuff */
	private static final long serialVersionUID = -6890122540695197269L;

	@Override
	public String execute() throws Exception 
	{
		return "loadNamedScriptPage";
	}
}
