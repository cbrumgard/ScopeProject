package edu.utk.mabe.scopelab.scope.admin.service;


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import edu.utk.mabe.scopelab.scope.ScopeError;
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
									  
		
		System.out.println("here1");
		ResultSet rs = null;
		
		try
		{
			conn.createStatement().execute("USE system");
			
			rs = conn.createStatement().executeQuery(
					"SELECT keyspace_name FROM schema_keyspaces WHERE keyspace_name = 'scope'");

			if(rs.next())
			{
				isInitialized = true;
				
				/* Sets the keyspace */
				conn.createStatement().execute("USE Scope");
				
			}else
			{
				isInitialized = false;
			}
			
			System.out.println("here3");
			
		}catch(SQLException e)
		{
			isInitialized = false;
			e.printStackTrace();
			

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
				String.format(
					"CREATE KEYSPACE Scope " +
					"	WITH REPLICATION = { 'class': '%s', 'replication_factor': %d }",
					strategy.name(), numReplicas));
			
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
			"	sessionID   text PRIMARY KEY, " +
			"   last_update timestamp, " +
			"	graphID     uuid, " +
			"   scriptID	uuid, " +
			"   numNodes    varint " +
			") ");
		
		/* Maps from session participant ID and persistent ID  
		 * int <--> participant ID */
		conn.createStatement().execute(
			"CREATE COLUMNFAMILY session_participants " +
			"(" +
			"	sessionID 		 text, " +
			"	participantIndex varint, " +
			"   participantID    text, " + 
			
			" 	PRIMARY KEY (sessionID, participantIndex) " + 
			//"	-- session participant index <-> participant ID " + 
			
			") ");
		
		conn.createStatement().execute(
			"CREATE INDEX ON session_participants (participantID)");

		/*   */
		conn.createStatement().execute(
			"CREATE COLUMNFAMILY session_results " +
		    "(" +
		    "	sessionID text, " +
		    "   interval varint, " +
		    "	participantIndex varint, " +
		    "	choice text," +
		  
		  	//"	-- interval --> list of choices (participant index, choice) " +
		    "	PRIMARY KEY (sessionID, interval)" +
		    
		    ")");
			
	    //"	-- participant index --> list of choices (interval, choice) " +
		conn.createStatement().execute("CREATE INDEX ON session_results(participantIndex)");
		
		
		/* Creates the session column family */
		conn.createStatement().execute(
			"CREATE COLUMNFAMILY activeSessions " +
			"(" +
			"	sessionID text PRIMARY KEY, " +
			"   joinQueue text, "+
			"   last_update timestamp" +
			") ");
		
		/* Creates the participant column family */
		conn.createStatement().execute(
				"CREATE COLUMNFAMILY participants" +
				"(" +
				"	participantID text PRIMARY KEY" +
				")");
		
		/* Creates the script column family */
		conn.createStatement().execute(
				"CREATE COLUMNFAMILY scripts " +
				"(" +
				"	scriptID  uuid, " +
				"	interval  varint, " +
				"	events	  list<varchar>, " + 
				
				"	PRIMARY KEY(scriptID, interval)" +
				")");
		
		/* Creates the named script column family */
		conn.createStatement().execute(
				"CREATE COLUMNFAMILY named_scripts " +
				"(" +
				"	name	 varchar," +
				"	scriptID uuid," +
				
				"	PRIMARY KEY(name)" +
				")");
		
		
		/* Creates the graph column family */
		conn.createStatement().execute(
				"CREATE COLUMNFAMILY graphs " +
				"(" +
				"	graphID  uuid PRIMARY KEY," +
				"   numNodes int " +
				")");
		
		conn.createStatement().execute(
				"CREATE COLUMNFAMILY named_graphs " +
				"(" +
				"	name 	varchar," +
				"   graphID uuid," +
			
				"	PRIMARY KEY(name)" +
				")");
		
		conn.createStatement().execute(
				"CREATE COLUMNFAMILY graphNodes " +
				"(" +
				"	graphID  uuid, " +
				"   nodeID varint, " +
				"   connectedNodes list<int>, " +
				
				"   PRIMARY KEY(graphID, nodeID) " +
				")");
	}
	
	public boolean isInitialized()
	{
		return isInitialized;
	}
	
	public Strategies[] getStrategies()
	{
		return Strategies.values();
	}
	
	public Iterable<String> getNamedGraphs() throws SQLException
	{
		return new Iterable<String>() 
		{
			@Override
			public Iterator<String> iterator() 
			{
				return new Iterator<String>() 
				{
					List<String> graphNames = new LinkedList<>();
					String start = null;
					
					@Override
					public boolean hasNext() 
					{
						if(graphNames.isEmpty() == false)
						{
							return true;
						}
					
						if(start == null)
						{
							try(PreparedStatement pstat = conn.prepareStatement(
									"SELECT * FROM named_graphs LIMIT 1");
								ResultSet rs = pstat.executeQuery())
							{	

								while(rs.next())
								{
									start = rs.getString(1);
									graphNames.add(start);
								}

							}catch(SQLException e) 
							{
								throw new RuntimeException(e);
							}

						}else
						{

							try(PreparedStatement pstat = conn.prepareStatement(
								"SELECT * FROM named_graphs " +
								"WHERE TOKEN(name) > TOKEN(?) LIMIT 1"))
							{	
								for(int i=0; i<1000; i++)
								{
									pstat.setString(1, start);
									ResultSet rs = pstat.executeQuery();

									if(rs.next())
									{
										start = rs.getString(1);
										graphNames.add(start);
										rs.close();
									}else
									{
										rs.close();
										break;
									}
								}
								
							}catch(SQLException e) 
							{
								throw new RuntimeException(e);
							}
						}
						
						return graphNames.isEmpty() == false;
					}

					@Override
					public String next() 
					{
						if(this.hasNext() == false)
						{
							throw new NoSuchElementException();
						}
						
						String graphName = this.graphNames.remove(0);
						
						return graphName;
					}

					@Override
					public void remove() 
					{
						throw new UnsupportedOperationException();	
					}
				};
			}
		};
	}
	
	public Graph retrieveNamedGraph(String graphName) throws ScopeError 
	{
		String graphID = null;
		Graph graph = null;
		
		try(PreparedStatement pstat = conn.prepareStatement(
			"SELECT graphID FROM named_graphs WHERE name = ? LIMIT 1"))
		{
			pstat.setString(1, graphName);
			
			try(ResultSet rs = pstat.executeQuery())
			{
				if(rs.next())
				{
					graphID = rs.getString(1);
				
					graph =  this.retrieveGraph(UUID.fromString(graphID));
				}else
				{
					throw new ScopeError("No such graph");
				}
			}
			
		}catch (SQLException e) 
		{
			e.printStackTrace();
			System.exit(-1);
		}
		
		return graph;
	}
	
	public void storeNamedGraph(String name, Graph graph) throws SQLException
	{
		/* Stores the graph */
		this.storeGraph(graph);
		
		
		/* Assigns a name to the graph */
		try(PreparedStatement pstat = conn.prepareStatement(
				"INSERT INTO named_graphs(name, graphID) VALUES(?, ?)");
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(16);
			DataOutputStream dataStream = new DataOutputStream(byteStream))
		{
			dataStream.writeLong(graph.getGraphID().getMostSignificantBits());
			dataStream.writeLong(graph.getGraphID().getLeastSignificantBits());
			dataStream.flush();
			
			pstat.setString(1, name);
			pstat.setBytes(2, byteStream.toByteArray());
			
			pstat.execute();
			
		}catch(IOException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public Graph retrieveGraph(UUID uuid) 
			throws SQLException
	{
		/* Change to the Scope keyspace */
		conn.createStatement().execute("USE Scope");
	
		Map<Integer, List<Integer>> nodes = new HashMap<>();
		
		try(PreparedStatement getNodesStat = conn.prepareStatement(
				"SELECT nodeID, connectedNodes FROM graphNodes WHERE graphID = ?");
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(16);
			DataOutputStream s = new DataOutputStream(byteStream))
		{
			s.writeLong(uuid.getMostSignificantBits());
			s.writeLong(uuid.getLeastSignificantBits());
			s.flush();
		
			getNodesStat.setBytes(1, byteStream.toByteArray());
			byteStream.reset();
			
			
			
			try(ResultSet rs = getNodesStat.executeQuery())
			{
				while(rs.next())
				{
					String nodeID = rs.getString(1);
					
					List<Integer> connectedNodes = (List<Integer>)rs.getObject(2);
					
					nodes.put(Integer.parseInt(nodeID), 
							connectedNodes);
				}
			}
			
		}catch(IOException e) 
		{	
			e.printStackTrace();
			System.exit(-1);
		}
		
		return GraphService.createFromUserSpecification(uuid, nodes);
	}
	
	public void storeGraph(Graph graph) throws SQLException
	{
		System.out.println("Storing graph\n");
		
		/* Change to the Scope keyspace */
		conn.createStatement().execute("USE Scope");
	
		
		/* Inserts the graph */
		
		/* TODO Waiting on consistency specifier for jdbc driver */
		try(PreparedStatement pstat1 = conn.prepareStatement(
				"INSERT INTO graphs (graphID, numNodes) VALUES(?, ?)");
	        PreparedStatement pstat = conn.prepareStatement(
				"INSERT INTO graphNodes(graphID, nodeID, connectedNodes) " +
				"VALUES(?, ?, ?)");
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(16);
			DataOutputStream s = new DataOutputStream(byteStream))
		{
			for(Node node: graph.getNodes())
			{
				List<Integer> connectedNodes = new LinkedList<>();
				
				for(Node connectedNode : graph.getConnectedNodes(node))
				{
					connectedNodes.add(connectedNode.getID());
				}

				s.writeLong(graph.getGraphID().getMostSignificantBits());
				s.writeLong(graph.getGraphID().getLeastSignificantBits());
				s.flush();
							
				pstat.setBytes(1, byteStream.toByteArray());
				pstat.setInt(2, node.getID());
				pstat.setObject(3, connectedNodes);
				pstat.execute();
				
				byteStream.reset();
			}
			
			s.writeLong(graph.getGraphID().getMostSignificantBits());
			s.writeLong(graph.getGraphID().getLeastSignificantBits());
			s.flush();
			
			
			pstat1.setBytes(1, byteStream.toByteArray());
			pstat1.setInt(2, graph.getNumNodes());
			pstat1.execute();
			
			byteStream.reset();
			
		}catch(IOException e)
		{
			e.printStackTrace();
		}
		
		
		System.out.println("Done storing graph\n");
	}
	
	public Iterable<String> getNamedScripts() throws SQLException
	{
		return new Iterable<String>() 
				{
					@Override
					public Iterator<String> iterator() 
					{
						return new Iterator<String>() 
						{
							List<String> scriptNames = new LinkedList<>();
							String start = null;
							
							@Override
							public boolean hasNext() 
							{
								if(scriptNames.isEmpty() == false)
								{
									return true;
								}
							
								if(start == null)
								{
									try(PreparedStatement pstat = conn.prepareStatement(
											"SELECT * FROM named_scripts LIMIT 1");
										ResultSet rs = pstat.executeQuery())
									{	

										while(rs.next())
										{
											start = rs.getString(1);
											scriptNames.add(start);
										}

									}catch(SQLException e) 
									{
										throw new RuntimeException(e);
									}

								}else
								{

									try(PreparedStatement pstat = conn.prepareStatement(
										"SELECT * FROM named_scripts " +
										"WHERE TOKEN(name) > TOKEN(?) LIMIT 1"))
									{	
										for(int i=0; i<1000; i++)
										{
											pstat.setString(1, start);
											ResultSet rs = pstat.executeQuery();

											if(rs.next())
											{
												start = rs.getString(1);
												scriptNames.add(start);
												rs.close();
											}else
											{
												rs.close();
												break;
											}
										}
										
									}catch(SQLException e) 
									{
										throw new RuntimeException(e);
									}
								}
								
								return scriptNames.isEmpty() == false;
							}

							@Override
							public String next() 
							{
								if(this.hasNext() == false)
								{
									throw new NoSuchElementException();
								}
								
								String scriptName = this.scriptNames.remove(0);
								
								return scriptName;
							}

							@Override
							public void remove() 
							{
								throw new UnsupportedOperationException();	
							}
						};
					}
				};
	}

	
	public void storeNamedScript(String name, Script script) 
			throws SQLException
	{
		/* Stores the script by uuid */
		this.storeScript(script);
		
		/* Inserts the name and the uuid of the script into named_scripts
		 * column family */
		try(PreparedStatement pstat = conn.prepareStatement(
				"INSERT INTO named_scripts(name, scriptID) VALUES(?, ?)");
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(16);
			DataOutputStream dataStream = new DataOutputStream(byteStream))
		{
			dataStream.writeLong(script.getScriptID().getMostSignificantBits());
			dataStream.writeLong(script.getScriptID().getLeastSignificantBits());
			dataStream.flush();
			
			pstat.setString(1, name);
			pstat.setBytes(2, byteStream.toByteArray());
			
			pstat.execute();
			
		}catch(IOException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public Script retrieveNamedScript(String scriptName) 
			throws SQLException, ScopeError
	{
		/* Change to the Scope keyspace */
		conn.createStatement().execute("USE Scope");
		
		try(PreparedStatement pstat = conn.prepareStatement(
				"SELECT scriptID FROM named_scripts WHERE name = ?"))
		{
			pstat.setString(1, scriptName);
			
			try(ResultSet rs = pstat.executeQuery())
			{
				if(rs.next())
				{
					return retrieveScript(UUID.fromString(rs.getString(1)));
				}
			}
		}
		
		throw new ScopeError("No such script with the name "+scriptName);
	}
	
	
	
	
	public Script retrieveScript(UUID scriptID) throws SQLException, ScopeError
	{
		List<Event> events = new LinkedList<>();
		
		/* Change to the Scope keyspace */
		conn.createStatement().execute("USE Scope");
		
		try(PreparedStatement pstat = conn.prepareStatement(
				"SELECT interval, events FROM scripts WHERE scriptID = ?");
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(16);
			DataOutputStream dos = new DataOutputStream(byteStream))
		{
			dos.writeLong(scriptID.getMostSignificantBits());
			dos.writeLong(scriptID.getLeastSignificantBits());
			dos.flush();
			
			pstat.setBytes(1, byteStream.toByteArray());
			
			try(ResultSet rs = pstat.executeQuery())
			{
				while(rs.next())
				{
					System.out.printf("Interval: %d Events: %s\n", rs.getInt(1), rs.getObject(2));
					
					for(String eventDescription : (List<String>)rs.getObject(2))
					{
						events.add(ScriptService.Event.deserialize(eventDescription));
					}
				}
			}
			
		}catch(ScopeError e)
		{
			throw e;
			
		}catch(Throwable e) 
		{
			throw new ScopeError("Unable to retrieve script");
		}
		
		return new ScriptService.Script(scriptID, events);
	}
	
	
	public void storeScript(Script script) throws SQLException
	{
		/* Change to the Scope keyspace */
		conn.createStatement().execute("USE Scope");
		
		List<String> events = new LinkedList<>();
		
		try(PreparedStatement pstat = conn.prepareStatement(
				"INSERT INTO scripts(scriptID, interval, events) VALUES(?, ?, ?)");
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(16);
			DataOutputStream dos = new DataOutputStream(byteStream))
		{
			dos.writeLong(script.getScriptID().getMostSignificantBits());
			dos.writeLong(script.getScriptID().getLeastSignificantBits());
			dos.flush();
			
			for(int i=0; i<=script.getHighestIteration(); i++)
			{
				for(Event event : script.getEventsForIteration(i))
				{
					events.add(event.serialize());
				}
				
				
				pstat.setBytes(1, byteStream.toByteArray());
				pstat.setInt(2, i);
				pstat.setObject(3, events);
				pstat.execute();
				
				events.clear();
			}
			
		}catch(IOException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		
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
	
	public void storeSession(Session session, UUID graphUUID, UUID scriptUUID) 
			throws SQLException
	{
		long currTime = System.currentTimeMillis();
		
		
		try(PreparedStatement pstat = conn.prepareStatement(
				"INSERT INTO sessions(sessionID, graphID, scriptID, last_update) " +
				"VALUES(?,?,?,?)");
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(16);
			DataOutputStream dos = new DataOutputStream(byteStream))
		{
			/* Change to the Scope keyspace */
			conn.createStatement().execute("USE Scope");

			pstat.setString(1, session.getSessionID());
			
			dos.writeLong(graphUUID.getMostSignificantBits());
			dos.writeLong(graphUUID.getLeastSignificantBits());
			dos.flush();
			
			pstat.setBytes(2, byteStream.toByteArray());
			
			byteStream.reset();
			dos.writeLong(scriptUUID.getMostSignificantBits());
			dos.writeLong(scriptUUID.getLeastSignificantBits());
			dos.flush();
			
			pstat.setBytes(3, byteStream.toByteArray());
			
			pstat.setLong(4, currTime);

			pstat.execute();
			
		}catch(IOException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void storeParticipants(String sessionID, Map<String, Integer> participantMap) throws SQLException
	{
		try(PreparedStatement pstat = conn.prepareStatement(
				"INSERT INTO session_participants(sessionID, participantIndex, participantID) " +
				"VALUES(?,?,?)"))
		{	
			for(Map.Entry<String, Integer> participantEntry : participantMap.entrySet())
			{
				
				pstat.setString(1, sessionID);
				pstat.setLong(2, participantEntry.getValue());
				pstat.setString(3, participantEntry.getKey());
				pstat.execute();
			}
		}
	}
	
	public void storeChoice(String sessionID, int interval, int participantIndex, 
			String choice) throws SQLException
	{
		try(PreparedStatement pstat = conn.prepareStatement(
				"INSERT INTO session_results(sessionID, interval, participantIndex, choice) " +
				"VALUES(?, ?, ?, ?)"))
		{
			pstat.setString(1, sessionID);
			pstat.setInt(2, interval);
			pstat.setInt(3, participantIndex);
			pstat.setString(4, choice);
			
			pstat.execute();
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
