package edu.utk.mabe.scopelab.scope.participant.action;

import javax.servlet.ServletContext;

import org.apache.struts2.util.ServletContextAware;

import edu.utk.mabe.scopelab.scope.BaseScopeAction;
import edu.utk.mabe.scopelab.scope.ScopeServer;
import edu.utk.mabe.scopelab.scope.admin.service.messenging.MessengingService;

public class GetMainPageAction extends BaseScopeAction 
	implements ServletContextAware
{
	/* Serialization stuff */
	private static final long serialVersionUID = -4423049940915965976L;

	/* Instance variables */
	protected ServletContext servletContext = null;
	protected ScopeServer scopeServer 		= null;

	protected String webSocketURI = null;
	private String newSessionAnnouncementTopicName = null;
	
	
	
	@Override
	public void setServletContext(ServletContext servletContext) 
	{
		this.servletContext = servletContext;
	}
	
	public ServletContext getServletContext() 
	{
		return servletContext;
	}
	
	
	public String getWebSocketURI() 
	{
		return webSocketURI;
	}

	public void setWebSocketURI(String webSocketURI) 
	{
		this.webSocketURI = webSocketURI;
	}

	
	public String getNewSessionAnnouncementTopicName() 
	{
		return newSessionAnnouncementTopicName;
	}

	public void setNewSessionAnnouncementTopicName(
					String newSessionAnnouncementTopicName) 
	{
		this.newSessionAnnouncementTopicName = newSessionAnnouncementTopicName;
	}

	public String execute() throws Exception
	{
		/* Gets the scope server */
		ScopeServer scopeServer = (ScopeServer)servletContext.getAttribute(
				"edu.utk.mabe.scopelab.scope.ScopeServer");

		/* No server so not running */
		if(scopeServer == null)
		{
			return setErrorMessage("Server is not running");
		}

		MessengingService messengingService = scopeServer.getMessengingService();
		setWebSocketURI(messengingService.getWebSocketURI());
		setNewSessionAnnouncementTopicName(
				messengingService.getNewSessionAnnoncementTopicName());
		
		
		/* Return the main page */
		return "mainPage";
	}
}
