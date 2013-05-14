package edu.utk.mabe.scopelab.scope.admin.service.session;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import net.sf.json.JSONObject;
import edu.utk.mabe.scopelab.scope.admin.service.messenging.MessageDestination;

class NeighborUpdateTask extends FutureTask<Void> 
{
	public NeighborUpdateTask(final MessageDestination destination, 
			final int participantIndex, final String choice) 
	{
		super(new Callable<Void>()
				{
					@Override
					public Void call() throws Exception 
					{
						/* Sends the message */
						destination.sendMessage(
							new JSONObject()
									.element("msgType", "data")
									.element("data", 
										new JSONObject()
											.element("type", "neighbor update")
											.element("neighborID", participantIndex)
											.element("choice", choice)).toString());		
						
						/* Return */
						return null;
					}
				});
	}
}
