package edu.utk.mabe.scopelab.scope.admin.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import edu.utk.mabe.scopelab.scope.admin.service.GraphService.Node;



public class GraphService 
{
	
	public enum GraphTypes
	{
		EBA,
		USER
	}
	
	
	public static interface Graph
	{
		public int 		getNumNodes();
		public boolean  containsLink(Node fromNode, Node toNode);
		public void 	addNode(Node node);
		public void     addLink(Node fromNode, Node toNode);
		public void     removeLink(Node fromNode, Node toNode);
		public Collection<Node> getNodes();
		public Collection<Node> getNodesRepeatedByNumConnections();
		public Collection<Node> getConnectedNodes(Node node);
		public Collection<Node> getConnectedNodes(int nodeIndex);
	}
	
	public static class Node implements Comparable<Node>
	{
		protected final int id;
		
		Node(int id)
		{
			this.id = id;
		}

		@Override
		public int compareTo(Node o) 
		{
			return this.id - o.id;
		}
		
		public int getID()
		{
			return this.id;
		}
	}
	
	static class ExtendBAGraph implements Graph
	{
		final protected int numNodes;
		final protected int m;
		final protected float p;
		final protected float q;
		final protected Multimap<Node, Node> links = TreeMultimap.create();
			
		
		ExtendBAGraph(int numNodes, int m, float p, float q) 
		{
			this.numNodes = numNodes;
			this.m        = m;
			this.p        = p;
			this.q        = q;
		}

		
		@Override
		public int getNumNodes() 
		{
			return this.numNodes;
		}


		@Override
		public void addNode(Node node) 
		{
			/* Link to itself so this provides the + 1 degree factor */
			links.put(node, node);
		}
		
		@Override
		public Collection<Node> getNodes() 
		{
			/* Returns the nodes */
			return links.keySet();
		}
		
		
		
		@Override
		public boolean containsLink(Node fromNode, Node toNode)
		{
			return links.containsEntry(fromNode, toNode);
		}


		@Override
		public Collection<Node> getConnectedNodes(Node fromNode) 
		{	
			return links.get(fromNode);
		}
		

		@Override
		public Collection<Node> getConnectedNodes(int nodeIndex) 
		{
			return getConnectedNodes(new Node(nodeIndex));
		}


		@Override
		public void addLink(Node fromNode, Node toNode) 
		{
			links.put(fromNode, toNode);
		}


		@Override
		public Collection<Node> getNodesRepeatedByNumConnections() 
		{
			return links.values();
		}


		@Override
		public void removeLink(Node fromNode, Node toNode) 
		{
			links.remove(fromNode, toNode);
		}
	}
	

	static class UserGraph implements Graph
	{
		final protected Multimap<Node, Node> links = TreeMultimap.create();
		final protected int numNodes;
	
	
		public UserGraph(int numNodes) 
		{
			this.numNodes = numNodes;
		}
		
		@Override
		public int getNumNodes() 
		{
			return this.numNodes;
		}


		@Override
		public void addNode(Node node) 
		{
			/* Link to itself so this provides the + 1 degree factor */
			links.put(node, node);
		}

		@Override
		public Collection<Node> getNodes() 
		{
			/* Returns the nodes */
			return links.keySet();
		}
		
		
		
		@Override
		public boolean containsLink(Node fromNode, Node toNode)
		{
			return links.containsEntry(fromNode, toNode);
		}


		@Override
		public Collection<Node> getConnectedNodes(Node fromNode) 
		{	
			return links.get(fromNode);
		}


		@Override
		public void addLink(Node fromNode, Node toNode) 
		{
			links.put(fromNode, toNode);
		}


		@Override
		public Collection<Node> getNodesRepeatedByNumConnections() 
		{
			return links.values();
		}


		@Override
		public void removeLink(Node fromNode, Node toNode) 
		{
			links.remove(fromNode, toNode);
		}

		@Override
		public Collection<Node> getConnectedNodes(int nodeIndex) 
		{
			return getConnectedNodes(new Node(nodeIndex));
		}
	}
	
	public static Graph createExtendedBA(int initialNumNodes, int numNodes, int m, float p, float q)
	{
		/* Check arguments */
		if(numNodes < 3 || numNodes < initialNumNodes || m>initialNumNodes 
				|| p < 0 || q < 0 || (p+q) > 1)
		{
			throw new IllegalArgumentException(
					"numNodes must be >= 3, numNodes >= initialNumNodes, "+
					"m <= initialNumNodes, p >=0, q>= 0 and p+q <=1");
		}
		
		/* Instance variables */
		Graph graph = new ExtendBAGraph(numNodes, initialNumNodes, p, q);
		
		Random random = new Random();
		int nodeID    = 0;
		
		/* Initialize isolated nodes */
		for(int i=0; i<initialNumNodes; i++)
		{
			/* Create the node */
			graph.addNode(new Node(nodeID++));
		}
		

		/* List of nodes in the graph */
		for(Node[] nodes = graph.getNodes().toArray(new Node[0]);
				nodes.length < numNodes; 
				nodes = graph.getNodes().toArray(new Node[0]))
		{
			/* Adds new m links with probability p */
			if(random.nextFloat() < p)
			{
				for(int i=0; i<m; i++)
				{
					Node[] toNodes = graph.getNodesRepeatedByNumConnections().toArray(new Node[0]);

					/* Picks the from node at random */
					Node fromNode = nodes[random.nextInt(nodes.length)];

					/* Picks the to node randomly by it's degree */
					Node toNode   = toNodes[random.nextInt(toNodes.length)];

					/* Adds the link */
					if(graph.containsLink(fromNode, toNode) == false)
					{
						graph.addLink(fromNode, toNode);
					}
				}
			}

			/* Rewires m links with probability q */
			if(random.nextFloat() < q)
			{
				Node[] toNodes = new Node[0];

				for(int i=0; i<m; i++)
				{
					/* Randomly picks a from node */
					Node fromNode = nodes[random.nextInt(nodes.length)];

					/* Generates candidate destination nodes (removing 
					 * the link-to-itself node */
					List<Node> candidateNodes = new ArrayList<Node>(
											graph.getConnectedNodes(fromNode));
					for(Iterator<Node> nodeIterator=candidateNodes.iterator(); 
							nodeIterator.hasNext();)
					{
						if(nodeIterator.next() == fromNode)
						{
							nodeIterator.remove();
						}
					}
							
					/* Randomly selects a link to remove from the node */
					toNodes  		= candidateNodes.toArray(new Node[0]);
					
					if(toNodes.length == 0)
					{
						continue;
					}

					Node oldToNode  = toNodes[random.nextInt(toNodes.length)];
					
					
					
					/* List of nodes (repeated by the number of incoming links) */
					toNodes = graph.getNodesRepeatedByNumConnections().toArray(toNodes);

					/* Picks the to node randomly by it's degree */
					Node newToNode   = toNodes[random.nextInt(toNodes.length)];

					/* Adds a new link */
					if(graph.containsLink(fromNode, newToNode) == false)
					{
						graph.addLink(fromNode, newToNode);
						
						/* Removes the link form the graph */
						graph.removeLink(fromNode, oldToNode);
					}
				}
			}

			/* Adds a new node */
			if(random.nextFloat() < (1 - p - q))
			{
				/* Create the node */
				Node node = new Node(nodeID++);

				graph.addNode(node);

				for(int i=0; i<m; i++)
				{
					/* List of nodes (repeated by the number of incoming links) */
					Node[] toNodes = graph.getNodesRepeatedByNumConnections().toArray(new Node[0]);

					/* Picks the to node randomly by it's degree */
					Node toNode   = toNodes[random.nextInt(toNodes.length)];

					/* Adds the link */
					if(graph.containsLink(node, toNode) == false)
					{
						graph.addLink(node, toNode);
					}
				}
			}
		}
	
		/* Returns the graph */
		return graph;
	}
	
	public static Graph createFromUserSpecification(
			Map<Integer, List<Integer>> connectedNodes)
	{
		/* Creates the graph */
		Graph graph = new UserGraph(connectedNodes.size());
		
		Map<Integer, Node> nodes = new HashMap<Integer, Node>();
				
		/* Creates the nodes for the graph */
		for(Integer nodeID : connectedNodes.keySet())
		{
			Node node = new Node(nodeID);
			nodes.put(nodeID, node);
			graph.addNode(node);
		}
		
		/* Adds the links */
		for(Entry<Integer, List<Integer>> linkEntrySet : connectedNodes.entrySet())
		{
			Node fromNode = nodes.get(linkEntrySet.getKey());
			
			for(Integer nodeID : linkEntrySet.getValue())
			{
				graph.addLink(fromNode, nodes.get(nodeID));
			}
		}
		
		/* Returns the graph */
		return graph;
	}
}
