package edu.utk.mabe.scopelab.scope.admin.action;

import javax.servlet.ServletContext;

import org.apache.struts2.util.ServletContextAware;

import edu.utk.mabe.scopelab.scope.BaseScopeAction;

public class GetLoadNamedGraphPage extends BaseScopeAction 
{
	/* Serialization stuff */
	private static final long serialVersionUID = 4251929618243497710L;
	
	
	@Override
	public String execute() throws Exception 
	{
		return "loadNamedGraphPage";
	}
}
