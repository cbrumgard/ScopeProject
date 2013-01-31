package edu.utk.mabe.scopelab.scope.admin.service.session;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import edu.utk.mabe.scopelab.scope.admin.service.session.Session.ParticipantEntry;

class ChoiceTask extends FutureTask<Map<String, String>> 
{
	ChoiceTask(final Collection<ParticipantEntry> participants, 
			final String choice, final Collection<String> choices, 
			final long duration)
	{
		super(new Callable<Map<String, String>>() 
				{
					@Override
					public Map<String, String> call() throws Exception 
					{
						long startedRunning = System.currentTimeMillis();
						
						/* Constructs the message */
						String message = 
								new JSONObject()
									.element("msgType", "data")
									.element("data", 
										new JSONObject()
									 		.element("type", "choice")
									 		.element("choice", choice)
											.element("choices", choices)).toString();
							
					
						/* Sends the message to each of the participants */
						for(ParticipantEntry participant : participants)
						{
							participant.getClientDestination().sendMessage(message);
						}
							
						Map<String, String> results = new HashMap<>();
				
						/* Waits up the duration time for message to come in */
						for(ParticipantEntry participant : participants)
						{
							long timeToSleep = Math.max((duration * 1000) - 
									(System.currentTimeMillis() - startedRunning), 0);
							
							String rawResponse = participant.getServerDestination()
									.receiveMessage(timeToSleep);
							
							if(message != null)
							{
								System.out.printf("Response = %s\n", message);

								try
								{
									JSONObject response = JSONObject.fromObject(rawResponse);
									
									results.put(
											response.getString("participantID"), 
											response.getString("choice"));
								
								}catch(JSONException e)
								{
									e.printStackTrace();
								}
							}
						}
				
						/* Sleeps the remaining time before returning */
						long timeToSleep = Math.max((duration * 1000) - 
								(System.currentTimeMillis() - startedRunning), 0);
		
						if(timeToSleep > 0)
						{
							Thread.sleep(timeToSleep);
						}
						
						/* Returns the results */
						return results;
					}
				});
	}
}
