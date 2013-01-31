package edu.utk.mabe.scopelab.scope.admin.service.session;

import java.util.HashMap;

import javax.jms.JMSException;

import edu.utk.mabe.scopelab.scope.admin.service.GraphService;
import edu.utk.mabe.scopelab.scope.admin.service.ScriptService;
import edu.utk.mabe.scopelab.scope.admin.service.StorageService;
import edu.utk.mabe.scopelab.scope.admin.service.GraphService.Graph;
import edu.utk.mabe.scopelab.scope.admin.service.ScriptService.Script;
import edu.utk.mabe.scopelab.scope.admin.service.messenging.MessengingService;

public class SessionService 
{
	/* Instance variables */
	final protected StorageService storageService;
	
	
	public SessionService(StorageService storageService)
	{
		this.storageService = storageService;
	}
	
	public Session createSession(String sessionID, Graph graph, 
				Script script, MessengingService messengingService, 
				StorageService storageService) throws JMSException
	{
		/* Creates the session */
		Session session = new Session(sessionID, graph, script, messengingService, 
				storageService);
		
		return session;
	}
}
