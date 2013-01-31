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

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import edu.utk.mabe.scopelab.scope.ScopeError;

public class ScriptService 
{
	public static class Script
	{
		protected final Multimap<Integer, Event> eventsByIteration;
		protected final Integer highestIteration;	
		
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
			
			
			
			int highestIteration = 0;
			
			for(Event event : events)
			{
				
				highestIteration = Math.max(highestIteration, event.getIteration());
				eventsByIteration.put(event.getIteration(), event);
			}
			
			this.highestIteration = highestIteration;		
		}
		
		public int getHighestIteration()
		{
			return this.highestIteration;
		}
		
		public Collection<Event> getEventsForIteration(int i)
		{
			return this.eventsByIteration.get(i);
		}
		
		public Collection<Event> getAllEvents()
		{
			return this.eventsByIteration.values();
		}
		
		public Collection<NewsEvent> getAllNewsEvents()
		{
			return Lists.newArrayList(Iterables.filter(this.getAllEvents(), NewsEvent.class));
		}
	}
	
	public abstract static class Event
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
	
	public static class NewsEvent extends Event
	{
		/* Instance variables */
		protected final String message;
		
		
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
	
	public static class ChoiceEvent extends Event
	{
		/* Instance variables */
		protected final Collection<Integer> participants;
		protected final String message;
		protected final Collection<String> choices;
		
		
		ChoiceEvent(int iteration, int duration, Collection<Integer> participants,
				String message, Collection<String> choices) 
		{
			super(iteration, duration);
			
			this.participants = participants;
			this.message	  = message;
			this.choices      = choices;
		}

		public Collection<Integer> getParticipants() 
		{
			return participants;
		}

		public String getMessage() 
		{
			return message;
		}

		public Collection<String> getChoices() 
		{
			return choices;
		}
	}
	
	
	public static Script parseScript(String script) throws ScopeError
	{	
		List<Event> events = new LinkedList<>();
		

		try(CSVReader input = new CSVReader(new StringReader(script)))
		{	
			for(String[] fields : input.readAll())
			{
				/* Must have 3 fields (iteration, duration, event type) */
				if(fields.length < 3)
				{
					throw new ScopeError("Invalid script file");
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
						
						if(fields.length < 4)
						{
							System.out.printf("Field length = %d\n", fields.length);
							
							for(int i=0; i<fields.length; i++)
							{
								System.out.printf("field[%d] = %s\n", i, fields[i]);
							}
							
							throw new ScopeError("Invalid script file ");
						}
						
						events.add(new NewsEvent(iteration, duration, fields[3]));
						
						break;
					
					/* Choice event */
					case "choice":
						
						System.out.printf("Choice event");
						
						if(fields.length < 6)
						{
							System.out.printf("Field length = %d\n", fields.length);
							
							for(int i=0; i<fields.length; i++)
							{
								System.out.printf("field[%d] = %s\n", i, fields[i]);
							}
							
							throw new ScopeError("Invalid script file ");
						}
						
						/* Gets the participants (field 4) */
						System.out.printf("Participants = %s\n", fields[3]);
						
						List<Integer> participants = new LinkedList<>();
						
						for(String participantID : fields[3].split(","))
						{
							participants.add(Integer.parseInt(participantID));
						}
						
						
						/* Gets the message (field 5) */
						String message = fields[4];
						
						/* Gets the choices (field 6) */
						System.out.printf("Choices = %s", fields[5]);
						
						List<String> choices = new LinkedList<String>();
						
						for(String choice : fields[5].split(","))
						{
							choices.add(choice);
						}
		
						events.add(new ChoiceEvent(iteration, duration, 
								   participants, message, choices));
						
						break;
						
					/* Unknown event type */
					default:
						throw new ScopeError(
							"Invalid script file: Unknown event type ");
				}
				
			}
			
			return new Script(events);
			
		}catch(IOException | NumberFormatException e) 
		{	
			throw new ScopeError(e.getMessage());
		}
	}
}
