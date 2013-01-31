package edu.utk.mabe.scopelab.scope.admin.service;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import edu.utk.mabe.scopelab.scope.SessionDescription;
import edu.utk.mabe.scopelab.scope.admin.service.GraphService.Graph;
import edu.utk.mabe.scopelab.scope.admin.service.GraphService.Node;
import edu.utk.mabe.scopelab.scope.admin.service.ScriptService.Event;
import edu.utk.mabe.scopelab.scope.admin.service.ScriptService.Script;
import edu.utk.mabe.scopelab.scope.admin.service.session.Session;

public class StorageService 
{
	/* Valid strategies Enum */
	public enum Strategies
	{
		SimpleStrategy;
	}
	
	
	/* Class variables */
	protected static String driverClassName = 
			"org.apache.cassandra.cql.jdbc.CassandraDriver";
	
	/* Instance Variables */
	protected Connection conn = null;
	protected String hostname;
	protected String port;
	protected boolean 	 isInitialized = false;
	

	public StorageService() 
			throws NamingException, ClassNotFoundException, SQLException 
	{
		Context env =  (Context)new InitialContext().lookup("java:comp/env");
		
		init((String)env.lookup("scope.admin.backend.host"), 
			 (String)env.lookup("scope.admin.backend.port"));
	}
	
	public StorageService(String hostname, String port) 
			throws ClassNotFoundException, SQLException
	{
		init(hostname, port);
	}
	
	protected void init(String hostname, String port) 
		throws ClassNotFoundException, SQLException
	{
		this.hostname = hostname;
		this.port = port;
		
		/* Loads the driver class */
		Class.forName(driverClassName);
		
		/* Gets a connection to cassandra */
		conn = DriverManager.getConnection(
						String.format("jdbc:cassandra://%s:%s/system", 
							hostname, port));
									  
		
		ResultSet rs = null;
		
		try
		{
			
			rs = conn.createStatement().executeQuery(
					"SELECT name FROM schema_keyspaces WHERE 'keyspace' = 'Scope'");

			if(rs.next())
			{
				System.out.println(rs.getString(1));
				
				isInitialized = true;
				
				/* Sets the keyspace */
				conn.createStatement().execute("USE Scope");
				
			}else
			{
				isInitialized = false;
			}
			
		}catch(SQLException e)
		{
			isInitialized = false;

		}finally
		{
			if(rs != null)
			{
				rs.close();
			}
		}
	}
	
	protected Connection getConnection() throws SQLException
	{
		/* Gets a connection to cassandra */
		return DriverManager.getConnection(
						String.format("jdbc:cassandra://%s:%s/system", 
							hostname, port));
	}
	
	
	/**
	 * Initialize the backend storage
	 * @param numReplicas 
	 * @param strategies 
	 */
	public void initialize(Strategies strategy, int numReplicas) 
			throws SQLException
	{
		System.out.println(strategy.name());
		System.out.println(numReplicas);
		
		/* Creates the keyspace */
		if(strategy == Strategies.SimpleStrategy)
		{
			conn.createStatement().execute(
					"CREATE KEYSPACE Scope WITH strategy_class = " +strategy.name()+
							" AND strategy_options:replication_factor = "+numReplicas
					);
		
			/* Change to the Scope keyspace */
			//conn.createStatement().execute("USE Scope");
			
		}else
		{
			throw new SQLException("Invalid strategy "+strategy);
		}
		
		/* Change to the Scope keyspace */
		conn.createStatement().execute("USE Scope");
		
		/* Creates the server column family */
		conn.createStatement().execute(
			"CREATE COLUMNFAMILY servers" +
			"("+
			"	server_url  text PRIMARY KEY, " +
			"   last_update timestamp" +
			")");
		
		/* Creates the session column family */
		conn.createStatement().execute(
			"CREATE COLUMNFAMILY sessions " +
			"(" +
			"	sessionID text PRIMARY KEY, " +
			"   last_update timestamp" +
			") " +
			
			"WITH comparator=text AND default_validation=text");
		
		/* Creates the session column family */
		conn.createStatement().execute(
			"CREATE COLUMNFAMILY activeSessions " +
			"(" +
			"	sessionID text PRIMARY KEY, " +
			"   joinQueue text, "+
			"   last_update timestamp" +
			") " +
			
			"WITH comparator=text AND default_validation=text");
		
		/* Creates the participant column family */
		conn.createStatement().execute(
				"CREATE COLUMNFAMILY participants" +
				"(" +
				"	participantID text PRIMARY KEY" +
				") "+
				
				"WITH comparator=text AND default_validation=text");
		
		/* Creates the graph column family */
		conn.createStatement().execute(
				"CREATE COLUMNFAMILY scripts " +
				"(" +
				"	scriptID  uuid PRIMARY KEY " +
				")" +
				
				"WITH comparator=text AND default_validation=text");
		
		/* Creates the graph column family */
		conn.createStatement().execute(
				"CREATE COLUMNFAMILY graphs " +
				"(" +
				"	graphID  uuid PRIMARY KEY," +
				"   numNodes int " +
				")" +
				
				"WITH comparator=text AND default_validation=text");
	}
	
	public boolean isInitialized()
	{
		return isInitialized;
	}
	
	public Strategies[] getStrategies()
	{
		return Strategies.values();
	}
	
	public void storeGraph(UUID uuid, Graph graph) throws SQLException
	{
		System.out.println("Storing graph\n");
		
					
	
		StringBuilder columns        = new StringBuilder();
		StringBuilder values         = new StringBuilder();
		
		columns.append("graphID"); columns.append(',');
		values.append(uuid.toString()); values.append(',');
		
		columns.append("numNodes");
		values.append(graph.getNumNodes());
		
		for(Node node: graph.getNodes())
		{
			columns.append(','); columns.append(node.id);
			
		
			values.append(", '");
			
			/* Adds in the connected nodes */
			Collection<Node> connectedNodes = graph.getConnectedNodes(node);
			
			if(connectedNodes.isEmpty() == false)
			{
				for(Node connectedNode : connectedNodes)
				{
					values.append(connectedNode.id);
					values.append(',');
				}
				
				values.replace(values.length()-1, values.length(), "'");
			}else
			{
				values.append("'");
			}
		}
		
		/* Change to the Scope keyspace */
		conn.createStatement().execute("USE Scope");
		
		/* Inserts the graph */
		conn.createStatement().execute(
				"INSERT INTO graphs (" + columns + ") VALUES ("+ values + ") " +
				"USING CONSISTENCY QUORUM");		
		
		System.out.println("Done storing graph\n");
	}
	
	public void storeScript(UUID uuid, Script script) throws SQLException
	{
		StringBuilder columns = new StringBuilder("scriptID");
		StringBuilder values  = new StringBuilder("'"+uuid.toString()+"'");
				
		for(Event event : script.getAllEvents())
		{
			columns.append(','); columns.append(event.getIteration());
			values.append(",'"); 
			values.append(event.toString()); 
			values.append("'");
			
		}
		
		/* Change to the Scope keyspace */
		conn.createStatement().execute("USE Scope");

		conn.createStatement().execute(
				"INSERT INTO scripts("+columns+") VALUES("+values+") " +
				"USING CONSISTENCY QUORUM");
		
		System.out.println("Done storing script\n");
	}
	
	public boolean doesSessionExist(String sessionID) throws SQLException
	{
		/* Change to the Scope keyspace */
		conn.createStatement().execute("USE Scope");
		
		PreparedStatement pstat = conn.prepareStatement(
				"SELECT last_update FROM sessions WHERE sessionID = ?");
		
		pstat.setString(1, sessionID);
		
		ResultSet rs = pstat.executeQuery();
		
		boolean exists = rs.next();
		
		System.out.println("Exists = "+exists);
		
		
		try
		{
			rs.getDate(1);
		}catch(SQLException e)
		{
			exists = false;
		}
		
		return exists;
	}
	
	public Iterable<SessionDescription> getActiveSessions() throws SQLException
	{
		Connection conn = getConnection();
		
		/* Change to the Scope keyspace */
		conn.createStatement().execute("USE Scope");
		
		/* Gets the list of active sessions */
		final PreparedStatement pstat = conn.prepareStatement(
				"SELECT sessionID,joinQueue FROM activeSessions");
		
		final ResultSet rs = pstat.executeQuery();
		
		return new Iterable<SessionDescription>() 
		{
			@Override
			public Iterator<SessionDescription> iterator() 
			{
				return new Iterator<SessionDescription>()
				{
					SessionDescription nextSessionDescription = null;
					
					@Override
					public boolean hasNext() 
					{
						try
						{
							if(nextSessionDescription != null)
							{
								return true;
							}else if(rs.isClosed() == false && rs.next())
							{
								nextSessionDescription = 
										new SessionDescription(
												rs.getString(1), rs.getString(2));
								return true;

							}else
							{
								rs.close();
								pstat.close();
								
								return false;
							}
							
						}catch(SQLException e)
						{
							throw new RuntimeException(e);
						}
					}

					@Override
					public SessionDescription next() 
					{
						if(nextSessionDescription == null && hasNext() == false)
						{
							throw new NoSuchElementException();
						}
						
						SessionDescription tmpSession = nextSessionDescription;
						nextSessionDescription = null;
							
						return tmpSession;
					}

					@Override
					public void remove() 
					{
						throw new UnsupportedOperationException();
					}
					
					@Override
					protected void finalize() throws Throwable 
					{
						super.finalize();
						rs.close();
					}
				};
			}
		};
		
		/* Change to the Scope keyspace */
		//conn.createStatement().execute("USE Scope");
		
		//PreparedStatement pstat = conn.prepareStatement(
		//		"SELECT COUNT(*) FROM sessions WHERE sessionID = ?");
	}
	
	public void storeActiveSession(Session session) 
			throws SQLException
	{
		long currTime = System.currentTimeMillis();
		
		try(Statement statement = conn.createStatement();
			PreparedStatement pstat = conn.prepareStatement(
				"INSERT INTO activeSessions(sessionID, joinQueue, last_update) " +
				"VALUES(?,?, ?)"))
		{
			statement.execute("USE Scope");
			
			pstat.setString(1, session.getSessionID());
			pstat.setString(2, session.getJoinQueueName());
			pstat.setLong(3, currTime);

			pstat.execute();
		}
	}
	
	public void storeSession(Session session) throws SQLException
	{
		long currTime = System.currentTimeMillis();
		
		PreparedStatement pstat = null;
		
		try
		{
			/* Change to the Scope keyspace */
			conn.createStatement().execute("USE Scope");

			pstat = conn.prepareStatement(
					"INSERT INTO sessions(sessionID, last_update) VALUES(?,?)");

			pstat.setString(1, session.getSessionID());
			pstat.setLong(2, currTime);

			pstat.execute();
			pstat.close();
			pstat = null;
			
		}finally
		{
			if(pstat != null)
			{
				pstat.close();
			}
		}
	}
	
	public void registerServer(String serverURL, long duration) throws SQLException
	{
		/* Change to the Scope keyspace */
		conn.createStatement().execute("USE Scope");
		
		/* Inserts the server_url */
		PreparedStatement pstat = conn.prepareStatement(
			"INSERT INTO servers (server_url, last_update) VALUES (?, ?) USING TTL "+duration);
		
		pstat.setString(1, serverURL);
		pstat.setLong(2, System.currentTimeMillis());
		pstat.execute();
	}
	
	@Override
	protected void finalize() throws Throwable 
	{
		super.finalize();
	
		if(conn != null && conn.isClosed() == false)
		{
			conn.close();
		}
	}
}
