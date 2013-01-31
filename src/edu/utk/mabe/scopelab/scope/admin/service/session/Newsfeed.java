package edu.utk.mabe.scopelab.scope.admin.service.session;


import java.util.Collection;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;

import edu.utk.mabe.scopelab.scope.ScopeError;
import edu.utk.mabe.scopelab.scope.admin.service.ScriptService;
import edu.utk.mabe.scopelab.scope.admin.service.ScriptService.Script;
import edu.utk.mabe.scopelab.scope.admin.service.messenging.MessageDestination;
import edu.utk.mabe.scopelab.scope.admin.service.messenging.MessengingException;
import edu.utk.mabe.scopelab.scope.admin.service.messenging.MessengingService;

class Newsfeed 
{
	/* Instance variables */
	final MessengingService messengingService;
	final MessageDestination destination;
	
	protected Timer timer = null;
	
	public Newsfeed(MessengingService messengingService, 
			MessageDestination destination, 
			Collection<ScriptService.Event> newsEvents) 
	{
		this.messengingService = messengingService;
		this.destination	   = destination;
		
		PriorityQueue<ScriptService.Event> events = new PriorityQueue<>();
	}
	
	synchronized void start()
	{
		if(timer != null)
		{
			new ScopeError("Newsfeed already started");
		}
				
		timer = new Timer();
		
		timer.schedule(new TimerTask() 
		{		
			@Override
			public void run()
			{
				try 
				{
					destination.sendMessage("Hello world from Newsfeed");
				}catch (MessengingException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}, 0, 5000);
	}
	
	boolean hasStarted()
	{
		return this.timer != null;
	}
}
