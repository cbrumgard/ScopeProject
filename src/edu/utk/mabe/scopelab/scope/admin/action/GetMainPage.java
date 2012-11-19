package edu.utk.mabe.scopelab.scope.admin.action;

import com.opensymphony.xwork2.ActionSupport;

import edu.utk.mabe.scopelab.scope.BaseScopeAction;

public class GetMainPage extends BaseScopeAction
{
	private static final long serialVersionUID = -5516867950876192189L;

	@Override
	public String execute() throws Exception 
	{
		return "mainPage";
	}
}
