package edu.utk.mabe.scopelab.scope.admin.action;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import edu.utk.mabe.scopelab.scope.BaseScopeAction;
import edu.utk.mabe.scopelab.scope.admin.service.GraphService;
import edu.utk.mabe.scopelab.scope.admin.service.GraphService.Graph;
import edu.utk.mabe.scopelab.scope.admin.service.StorageService;

public class LoadNamedGraph extends BaseScopeAction 
{
	/* Serialization stuff */
	private static final long serialVersionUID = -2527087632094320486L;

	/* Input variables */
	private String graphName = null;
    private String graphFile = null;
	
	
	@Override
	public String execute() throws Exception 
	{
		/* Checks that the graph name was given */
		if(getGraphName() == null)
		{
			return setErrorMessage("Must specify a name");
		}

		/* Checks that the graph file was given */
		if(getGraphFile() == null)
		{
			return setErrorMessage("Must specify a file");
		}

		/* Read the graph file */
		try(BufferedReader input = new BufferedReader(new StringReader(getGraphFile())))
		{
			Integer numNodes = null;
			String row       = null;

			Map<Integer, List<Integer>> connectedNodes = new HashMap<>();

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
					return setErrorMessage("Invalid script file");
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
			Graph graph = GraphService.createFromUserSpecification(connectedNodes);

			/* Gets the storage service */
			StorageService storageService = new StorageService();

			/* Checks that the storage service has been initialzed */
			if(storageService.isInitialized() == false)
			{
				return setErrorMessage("Storage service has not been initialized");
			}
			
			/* Stores the graph by name */
			storageService.storeNamedGraph(getGraphName(), graph);

			/* Success */
			return setDataMessage(new JSONObject());
		}
	}


	public String getGraphName() 
	{
		return graphName;
	}


	public void setGraphName(String graphName) 
	{
		this.graphName = graphName;
	}


	public String getGraphFile() 
	{
		return graphFile;
	}


	public void setGraphFile(String graphFile) 
	{
		this.graphFile = graphFile;
	}
}
