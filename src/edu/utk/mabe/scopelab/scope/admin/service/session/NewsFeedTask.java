package edu.utk.mabe.scopelab.scope.admin.service.session;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import net.sf.json.JSONObject;
import edu.utk.mabe.scopelab.scope.admin.service.messenging.MessageDestination;

class NewsFeedTask extends FutureTask<Void>
{
	/* Instance variables */
	NewsFeedTask(final MessageDestination destination, final String message, 
			final int duration) 
	{
		super(new Callable<Void>() 
			{
				@Override
				public Void call() throws Exception 
				{
					long startedRunning = System.currentTimeMillis();
					
					/* Sends the message to the destination */
					destination.sendMessage(
							new JSONObject()
									.element("msgType", "data")
									.element("data", 
											new JSONObject()
									                .element("type", "newsfeed")
					    			                .element("message", message))
					    			                .toString());
					
					long timeToSleep = (duration * 1000) - 
							(System.currentTimeMillis() - startedRunning);
										
					
					System.out.printf("Time to sleep = %d\n", timeToSleep);
					
					if(timeToSleep > 0)
					{
						Thread.sleep(timeToSleep);
					}
					
					return null;
				}
			});
	}
}
