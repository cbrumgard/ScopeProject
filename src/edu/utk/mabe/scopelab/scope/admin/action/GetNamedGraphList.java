package edu.utk.mabe.scopelab.scope.admin.action;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import javax.servlet.ServletContext;

import org.apache.struts2.util.ServletContextAware;

import net.sf.json.JSONObject;

import edu.utk.mabe.scopelab.scope.BaseScopeAction;
import edu.utk.mabe.scopelab.scope.ScopeServer;
import edu.utk.mabe.scopelab.scope.admin.service.StorageService;
import edu.utk.mabe.scopelab.scope.admin.service.websocket.WebSocketService;

public class GetNamedGraphList extends BaseScopeAction 
	implements ServletContextAware
{
	/* Serialization stuff*/
	private static final long serialVersionUID = 6879848382690526045L;

	/* Instance variables */
	protected ServletContext servletContext = null;
	
	
	@Override
	public String execute() throws Exception 
	{
		synchronized(servletContext) 
		{
			/* Gets the scope server */
			ScopeServer scopeServer = (ScopeServer)servletContext.getAttribute(
					"edu.utk.mabe.scopelab.scope.ScopeServer");

			/* Error since there is already a server running */
			if(scopeServer == null)
			{
				return setErrorMessage("Server is not running");
			}
		}
		
		
		/* Gets an instance of the storage service */
		final StorageService storageService = new StorageService();
		
		if(storageService.isInitialized() == false)
		{
			return setErrorMessage("Storage service has not been initialized");
		}
		
		/* Build a response object with list of graph names */
		JSONObject response = new JSONObject();
		
		for(String graphName : storageService.getNamedGraphs())
		{
			response.accumulate("graphNames", graphName);
		}
		
		/* Success */
		return setDataMessage(response);
	}


	@Override
	public void setServletContext(ServletContext servletContext) 
	{
		this.servletContext = servletContext;
	}
}
