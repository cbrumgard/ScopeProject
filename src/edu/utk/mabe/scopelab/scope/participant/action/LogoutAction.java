package edu.utk.mabe.scopelab.scope.participant.action;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionSupport;

public class LogoutAction extends ActionSupport implements SessionAware
{
	private static final long serialVersionUID = 3371540790223065863L;

	protected Map<String, Object> sessionMap = null;
	
	public String execute()
	{
		for(Entry<String, Object> entry : sessionMap.entrySet())
		{
			System.out.printf("session key %s = %s\n", entry.getKey(), entry.getValue());
		}
		  
		sessionMap.remove("participantID");
		
		return "logout";
	}


	@Override
	public void setSession(Map<String, Object> sessionMap) 
	{
		this.sessionMap = sessionMap;
	}
}
