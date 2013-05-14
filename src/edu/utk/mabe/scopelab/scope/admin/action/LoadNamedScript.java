package edu.utk.mabe.scopelab.scope.admin.action;

import net.sf.json.JSONObject;
import edu.utk.mabe.scopelab.scope.BaseScopeAction;
import edu.utk.mabe.scopelab.scope.admin.service.ScriptService;
import edu.utk.mabe.scopelab.scope.admin.service.ScriptService.Script;
import edu.utk.mabe.scopelab.scope.admin.service.StorageService;

public class LoadNamedScript extends BaseScopeAction 
{
	/* Serialization stuff*/
	private static final long serialVersionUID = 7299680492478239841L;

	/* Input variables */
	protected String scriptName = null;
	protected String scriptFile = null;
	
	
	@Override
	public String execute() throws Exception 
	{
		/* Checks that a script name was given */
		if(scriptName == null)
		{
			return setErrorMessage("Must specify a name for the script");
		}
		
		/* Checkes that script was given */
		if(scriptFile == null)
		{
			return setErrorMessage("Must specify a script");
		}
		
		System.out.printf("ScriptName = %s\n", scriptName);
		System.out.printf("Script = %s\n", scriptFile);
		
		/* Creates the script from the description */
		Script script = ScriptService.parseScript(scriptFile);
		
		/* Creates the storage service */
		StorageService storageService = new StorageService();
		
		/* Checks that it is initialized */
		if(storageService.isInitialized() == false)
		{
			return setErrorMessage("Storage service has not been initialized");
		}
		
		/* Stores the script */
		storageService.storeNamedScript(scriptName, script);
		
		/* Success */
		return setDataMessage(new JSONObject());
	}

	public String getScriptName() 
	{
		return scriptName;
	}

	public void setScriptName(String scriptName) 
	{
		this.scriptName = scriptName;
	}

	public String getScriptFile() 
	{
		return scriptFile;
	}

	public void setScriptFile(String scriptFile) 
	{
		this.scriptFile = scriptFile;
	}
}
