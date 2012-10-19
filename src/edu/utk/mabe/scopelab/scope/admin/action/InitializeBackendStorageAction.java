package edu.utk.mabe.scopelab.scope.admin.action;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Arrays;

import javax.naming.NamingException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.opensymphony.xwork2.ActionSupport;

import edu.utk.mabe.scopelab.scope.admin.service.BackendStorageService;
import edu.utk.mabe.scopelab.scope.admin.service.BackendStorageService.Strategies;

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
		String nextPage;


		/* Initialize the backend service */
		BackendStorageService backendStorageService = 
				new BackendStorageService();

		Strategies[] validStrategies = backendStorageService.getStrategies();

		boolean isValidStrategy = false;

		for(Strategies strategy : validStrategies)
		{
			if(strategy.name().equals(this.strategy))
			{
				isValidStrategy = true;
				break;
			}
		}

		if(isValidStrategy == false)
		{
			this.addFieldError("strategy", "Invalid strategy");
			nextPage = "input";

			return nextPage;
		}

		int numReplicas = NumberUtils.toInt(this.replicationFactor);

		if(numReplicas < 1)
		{
			this.addFieldError("replicationFactor", 
					"Replication factor must be an integer > 0");
			nextPage = "input";
			return nextPage;
		}

		/* Initialize the backend storage */
		backendStorageService.initialize(Strategies.valueOf(strategy), 
				numReplicas);

		/* Goto the backend storage page */
		nextPage = "backendStoragePage";

		
		/* Returns the page to go to */
		return nextPage;
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
