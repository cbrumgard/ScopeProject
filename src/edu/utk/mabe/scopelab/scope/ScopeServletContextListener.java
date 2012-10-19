package edu.utk.mabe.scopelab.scope;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ScopeServletContextListener implements ServletContextListener 
{

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) 
	{
		System.out.println("In context initialized\n");
		
		
		ServletContext servletContext = servletContextEvent.getServletContext();
		
		Enumeration<String> names = servletContext.getAttributeNames();
		
		while(names.hasMoreElements())
		{
			System.out.println(names.nextElement());
		}
		
		try
		{
			/* Crates and starts the scope server */
			//ScopeServer scopeServer = new ScopeServer();
			
			/* Sets the attribute */
			servletContext.setAttribute(
					"edu.utk.mabe.scopelab.scope.ScopeServer", 
					null); //scopeServer);

		}catch(Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) 
	{
		
		
	}
}
