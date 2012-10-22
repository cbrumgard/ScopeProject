package edu.utk.mabe.scopelab.scope.admin.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import edu.utk.mabe.scopelab.scope.admin.service.GraphService.Graph;
import edu.utk.mabe.scopelab.scope.admin.service.GraphService.Node;

public class BackendStorageService 
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
	protected boolean 	 isInitialized = false;
	

	public BackendStorageService() 
			throws NamingException, ClassNotFoundException, SQLException 
	{
		Context env =  (Context)new InitialContext().lookup("java:comp/env");
		
		init((String)env.lookup("scope.admin.backend.host"), 
			 (String)env.lookup("scope.admin.backend.port"));
	}
	
	public BackendStorageService(String hostname, String port) 
			throws ClassNotFoundException, SQLException
	{
		init(hostname, port);
	}
	
	protected void init(String hostname, String port) 
		throws ClassNotFoundException, SQLException
	{
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
			conn.createStatement().execute("USE Scope");
			
		}else
		{
			throw new SQLException("Invalid strategy "+strategy);
		}
		
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
			"	sessionID text PRIMARY KEY" +
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
		
		
		/* Inserts the graph */
		conn.createStatement().execute(
				"INSERT INTO graphs (" + columns + ") VALUES ("+ values + ") " +
				"USING CONSISTENCY QUORUM");		
		
		System.out.println("Done storing graph\n");
	}
	
	public void registerServer(String serverURL, long duration) throws SQLException
	{
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
