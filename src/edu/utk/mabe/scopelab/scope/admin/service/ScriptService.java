package edu.utk.mabe.scopelab.scope.admin.service;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import edu.utk.mabe.scopelab.scope.ScopeError;

public class ScriptService 
{
	public static class Script
	{
		protected final Multimap<Integer, Event> eventsByIteration;
			
		
		Script(Collection<Event> events)
		{
			eventsByIteration = TreeMultimap.create(
					
					/* Key Comparator */
					new Comparator<Integer>() 
					{
						@Override
						public int compare(Integer o1, Integer o2) 
						{
							return o1.compareTo(o2);
						}
					},
					
					/* Event comparator */
					new Comparator<Event>() 
					{
						@Override
						public int compare(Event o1, Event o2) 
						{
						
							return 0;
						}
					});
			
			
			
			for(Event event : events)
			{
				eventsByIteration.put(event.getIteration(), event);
			}
		}
		
		public Collection<Event> getEventsForIteration(int i)
		{
			return this.eventsByIteration.get(i);
		}
		
		public Collection<Event> getAllEvents()
		{
			return this.eventsByIteration.values();
		}
	}
	
	abstract static class Event
	{
		protected final int iteration;
		protected final int duration;
		
		Event(int iteration, int duration)
		{
			this.iteration = iteration;
			this.duration  = duration;
		}
		
		public int getIteration()
		{
			return this.iteration;
		}
		
		public int getDuration()
		{
			return this.duration;
		}
	}
	
	static class NewsEvent extends Event
	{
		/* Instance variables */
		private final String message;
		
		
		NewsEvent(int iteration, int duration, String message)
		{
			super(iteration, duration);
			
			this.message = message;
		}

		public String getMessage() 
		{
			return message;
		}	
	}
	
	
	public static Script parseScript(String script) throws ScopeError
	{	
		List<Event> events = new LinkedList<>();
		
		
		try(LineNumberReader input = new LineNumberReader(new StringReader(script)))
		{
			for(String line = input.readLine(); line != null; line = input.readLine())
			{
				System.out.println(line);
				
				String[] fields = line.split(",", 4);
				
				/* Must have 3 fields (iteration, duration, event type) */
				if(fields.length < 2)
				{
					throw new ScopeError("Invalid script file at line "+
											input.getLineNumber());
				}
				
				/* Gets the iteration and duration */
				int iteration = Integer.parseInt(fields[0].trim());
				int duration  = Integer.parseInt(fields[1].trim());
				
				System.out.printf("iteration = %d duration = %d type = %s\n", iteration, duration, fields[2]);
				switch(fields[2].trim().toLowerCase())
				{
					/* News event */
					case "news": 
						
						System.out.printf("News event\n");
						
						if(fields.length != 4)
						{
							throw new ScopeError("Invalid script file at line "+
									input.getLineNumber());
						}
						
						events.add(new NewsEvent(iteration, duration, fields[2]));
						
						break;
					
					/* Unknown event type */
					default:
						throw new ScopeError(
							"Invalid script file: Unknown event type at line "+
							input.getLineNumber());
				}
				
			}
			
			return new Script(events);
			
		}catch(IOException | NumberFormatException e) 
		{	
			throw new ScopeError(e.getMessage());
		}
	}
}
