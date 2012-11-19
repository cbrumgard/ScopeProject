package edu.utk.mabe.scopelab.scope.admin.action;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.struts2.util.ServletContextAware;

import edu.utk.mabe.scopelab.scope.BaseScopeAction;
import edu.utk.mabe.scopelab.scope.ScopeError;
import edu.utk.mabe.scopelab.scope.ScopeServer;
import edu.utk.mabe.scopelab.scope.admin.service.BackendStorageService;
import edu.utk.mabe.scopelab.scope.admin.service.GraphService;
import edu.utk.mabe.scopelab.scope.admin.service.GraphService.Graph;
import edu.utk.mabe.scopelab.scope.admin.service.GraphService.GraphTypes;
import edu.utk.mabe.scopelab.scope.admin.service.SessionService.Session;

public class CreateSessionAction extends BaseScopeAction 
	implements ServletContextAware
{
	/* Pesty serialization crap */
	private static final long serialVersionUID = -5260524263132289246L;

	/* Class constants */
	protected final static String EBA 			 = "EBA";
	protected final static String USER_SPECIFIED = "USER";
	
	/* Variables */
	protected ServletContext servletContext = null;
	
	/* User input variables */
	protected String sessionName	  = null;
	protected String graphType 		  = null;
	protected String participantCount = null;
	protected String initialNumNodes  = null;
	protected String m				  = null;
	protected String p				  = null;
	protected String q				  = null;
	protected String userSpecification = null;

	
	
	
	/**
	 * Validates the input from the user.
	 * @throws UnsupportedEncodingException 
	 */
	public String validateInput() throws UnsupportedEncodingException
	{
		System.out.println("Inside of CreateSessionAction validate :-)");
		System.out.printf("SessionName is %s\n", this.sessionName);
		System.out.printf("GraphType is %s\n", this.graphType);
		System.out.printf("initialNumNodes %s\n", initialNumNodes);
		System.out.printf("m %s\n", m);
		System.out.printf("p %s\n", p);
		System.out.printf("q %s\n", q);
		System.out.printf("userFile %s\n", userSpecification);
		
		
		/* Checks that sessionName is valid */
		if(StringUtils.isBlank(this.sessionName))
		{
			return setErrorMessage("sessionName is invalid");
		}
		
		/* Graph type specified */
		if(StringUtils.isNotBlank(this.graphType))
		{
			
			/* EBA */
			if(this.graphType.equals(EBA))
			{
				/* Validates participant count */
				int participantCount = NumberUtils.toInt(this.participantCount);
				
				if(participantCount < 1)
				{
					return setErrorMessage("ParticipantCount is invalid");
				}
				 
				/* Validates initial number of nodes */
				int initialNumNodes = NumberUtils.toInt(this.initialNumNodes);
				
				if(initialNumNodes < 1)
				{
					return setErrorMessage("InitialNumNodes is invalid");
				}
				
				/* Validates m */
				double m = NumberUtils.toDouble(this.m);
				
				if(m <= 0.0d)
				{
					return setErrorMessage("M is invalid");
				}
					
				/* Validates p */
				double p = NumberUtils.toDouble(this.p);
				
				if(p < 0.0d)
				{
					return setErrorMessage("P is invalid");
				}
				
				/* Validates q */
				double q = NumberUtils.toDouble(this.q);
				
				if(q < 0.0d)
				{
					return setErrorMessage("Q is invalid");
				}
				
			/* User graph */
			}else if(this.graphType.equals(USER_SPECIFIED))
			{
				if(userSpecification == null)
				{
					return setErrorMessage("Must specify a file");
				}
				
			/* Invalid graph type */
			}else
			{
				return setErrorMessage("GraphType is invalid");
			}
		
		/* No graph type selected */ 
		}else
		{
			return setErrorMessage("GraphType must be specified");
		}
		
		/* Input is valid */
		return null;
	}
	
	/**
	 * Executes the action 
	 */
	public String execute() throws Exception
	{
		System.out.println("Inside of CreateSessionAction :-)");
		
		/* Variables */
		Graph graph = null;
		String nextPage = "sessionsPage";
		
		
		/* Validates the input */
		String result = validateInput();
		
		if(result != null)
		{
			return result;
		}

		/* Produces the graph */
		switch(GraphTypes.valueOf(graphType))
		{
			/* Extended BA graph */
			case EBA:
				graph = GraphService.createExtendedBA(
						Integer.parseInt(initialNumNodes), 
						Integer.parseInt(participantCount), 
						Integer.parseInt(m), 
						Float.parseFloat(p), 
						Float.parseFloat(q));
				break;
	
			/* User specified graph file */
			case USER:
	
				/* Read the graph file */
				try(BufferedReader input = new BufferedReader(new StringReader(userSpecification)))
				{
					Integer numNodes = null;
					String row       = null;
	
					Map<Integer, List<Integer>> connectedNodes 
					= new HashMap<Integer, List<Integer>>();
	
					for(int rowID=0; (row = input.readLine()) != null; rowID++)
					{
						row = row.trim();
						System.out.printf("row = %s\n", row);
						String[] cols = row.split("\\s+");
	
						System.out.printf("numNodes = %d\n", cols.length	);
	
						for(String col: cols)
						{
							System.out.printf("'%s'", col);
						}
	
	
						/* Gets the number of columns on the first pass */
						if(numNodes == null)
						{
							numNodes = cols.length;
	
							for(int i=0; i<numNodes; i++)
							{
								connectedNodes.put(i, new LinkedList<Integer>());
							}
						}
	
						/* Checks the number of nodes is the same for each
						 * row */
						if(cols.length != numNodes)
						{
							this.addFieldError("user", "Invalid file");
	
							/* Error page */
							return "input";
						}
	
						/* Builds the connected node map */
						for(int colID=0; colID<numNodes; colID++)
						{
							System.out.printf("ColumnID = %s\n", cols[colID]); 
							if(Double.parseDouble(cols[colID]) != 0f)
							{
								connectedNodes.get(rowID).add(colID);
							}
						}
	
					}
	
					/* Creates the graph */
					graph = GraphService.createFromUserSpecification(connectedNodes);
			}

			break;
		}

		/* Gets the scope server */
		ScopeServer scopeServer = (ScopeServer)servletContext.getAttribute(
				"edu.utk.mabe.scopelab.scope.ScopeServer");

		/* Error since there is already a server running */
		if(scopeServer == null)
		{
			return setErrorMessage("Server not running");
		}
	
		/* Create a session */
		try
		{
			Session session = scopeServer.createSession(sessionName, graph);
			
		}catch(ScopeError e)
		{
			return setErrorMessage(e.getMessage());
		}
		
		/* Success */
		System.out.println("Next page is "+nextPage);
		return nextPage;
	}

	public String getNameSession() 
	{
		return sessionName;
	}

	public void setSessionName(String sessionName) 
	{
		this.sessionName = sessionName.trim();
	}

	public String getGraphType() 
	{
		return graphType;
	}

	public void setGraphType(String graphType) 
	{
		this.graphType = graphType.trim();
	}

	public String getParticipantCount() 
	{
		return participantCount;
	}

	public void setParticipantCount(String participantCount) 
	{
		this.participantCount = participantCount.trim();
	}

	public String getInitialNumNodes() 
	{	
		return initialNumNodes;
	}

	public void setInitialNumNodes(String initialNumNodes) 
	{
		this.initialNumNodes = initialNumNodes.trim();
	}

	public String getM() 
	{
		return m;
	}

	public void setM(String m) 
	{
		this.m = m.trim();
	}

	public String getP() 
	{
		return p;
	}

	public void setP(String p) 
	{
		this.p = p.trim();
	}

	public String getQ() 
	{
		return q;
	}

	public void setQ(String q) 
	{
		this.q = q.trim();
	}
	
	public void setUserSpecification(String file) 
	{
        this.userSpecification = file;
    }
	
	@Override
	public void setServletContext(ServletContext servletContext) 
	{
		this.servletContext = servletContext;
	}

}
