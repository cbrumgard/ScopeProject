package edu.utk.mabe.scopelab.scope.admin.service;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class SecurityService 
{
	/* Instance variables */
	final protected String username;
	final protected String password;
	
	
	public SecurityService() throws NamingException
	{
		/* Stores the username and password of the admin */
		Context env = (Context) new InitialContext().lookup("java:comp/env");
		
		username = (String) env.lookup("scope.admin.username");
		password = (String) env.lookup("scope.admin.password");
	}
	
	public boolean verifyAccount(String inputUsername, String inputPassword)
	{
		if(inputUsername != null && username.equals(inputUsername))
		{
			if(inputPassword != null && password.equals(inputPassword))
			{
				return true;
			}
		}
		
		return false;
	}
}
