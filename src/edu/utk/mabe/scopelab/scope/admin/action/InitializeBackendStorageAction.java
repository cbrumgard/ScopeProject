package edu.utk.mabe.scopelab.scope.admin.action;

import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import edu.utk.mabe.scopelab.scope.BaseScopeAction;
import edu.utk.mabe.scopelab.scope.admin.service.StorageService;
import edu.utk.mabe.scopelab.scope.admin.service.StorageService.Strategies;

public class InitializeBackendStorageAction extends BaseScopeAction
{
	/* Serialization crap */
	private static final long serialVersionUID = -7490528426045726471L;

	private String strategy = null;
	private String replicationFactor = null;
	
	
	@Override
	public void validate() 
	{
		
		if(StringUtils.isBlank(this.strategy))
		{
			this.addFieldError("strategy", "Strategy must be provided");
		}
				
		if(StringUtils.isBlank(this.replicationFactor))
		{
			this.addFieldError("replicationFactor", "Replication factor must be given");
		}
	}
	
	@Override
	public String execute() throws Exception
	{
		/* Initialize the backend service */
		StorageService backendStorageService = 
				new StorageService();

		Strategies[] validStrategies = backendStorageService.getStrategies();

		boolean isValidStrategy = false;

		for(Strategies strategy : validStrategies)
		{
			System.out.printf("strategy = %s\n", strategy.name());
			
			if(strategy.name().equals(this.strategy))
			{
				isValidStrategy = true;
				break;
			}
		}

		if(isValidStrategy == false)
		{
			return setErrorMessage("Invalid strategy: "+this.strategy);
		}

		int numReplicas = NumberUtils.toInt(this.replicationFactor);

		if(numReplicas < 1)
		{
			return setErrorMessage("Replication factor must be an integer > 0");
		}

		/* Initialize the backend storage */
		backendStorageService.initialize(Strategies.valueOf(strategy), 
				numReplicas);

		/* Send a success full result back */
		return setDataMessage(new JSONObject());
	}

	public String getStrategy() 
	{
		return strategy;
	}


	public void setStrategy(String strategy) 
	{
		this.strategy = strategy;
	}


	public String getReplicationFactor() 
	{
		return replicationFactor;
	}


	public void setReplicationFactor(String replicationFactor) 
	{
		this.replicationFactor = replicationFactor;
	}
}
