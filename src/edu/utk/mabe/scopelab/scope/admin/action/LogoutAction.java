package edu.utk.mabe.scopelab.scope.admin.action;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.struts2.interceptor.SessionAware;

import com.opensymphony.xwork2.ActionSupport;

public class LogoutAction extends BaseScopeAction implements SessionAware
{
	private static final long serialVersionUID = 9033445536465550406L;

	protected Map<String, Object> sessionMap = null;
	
	
	public String execute()
	{
		for(Entry<String, Object> entry : sessionMap.entrySet())
		{
			System.out.printf("session key %s = %s\n", entry.getKey(), entry.getValue());
		}
		  
		/* Removes the session entry */
		sessionMap.remove("userID");
		sessionMap.remove("password");
		
		/* Go to the logout page */
		return "logout";
	}


	@Override
	public void setSession(Map<String, Object> sessionMap) 
	{
		this.sessionMap = sessionMap;	
	}
}
