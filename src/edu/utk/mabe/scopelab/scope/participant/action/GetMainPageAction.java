package edu.utk.mabe.scopelab.scope.participant.action;

import com.opensymphony.xwork2.ActionSupport;

public class GetMainPageAction extends ActionSupport 
{
	private static final long serialVersionUID = -4423049940915965976L;

	public String execute()
	{
		return "mainPage";
	}
}
