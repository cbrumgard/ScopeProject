package edu.utk.mabe.scopelab.scope.admin.interceptor;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.StrutsStatics;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ValidationAware;
import com.opensymphony.xwork2.interceptor.Interceptor;

import edu.utk.mabe.scopelab.scope.JSONResponse;
import edu.utk.mabe.scopelab.scope.admin.service.SecurityService;

public class LoginInterceptor implements Interceptor
{
	private static final long serialVersionUID = -1569502511866571739L;


	
	@Override
	public void init() 
	{
		// Do nothing
	}

	@Override
	public void destroy() 
	{
		// Do nothing
	}
	
	protected void setOutput(ActionInvocation actionInvocation) 
			throws UnsupportedEncodingException
	{
		actionInvocation.getInvocationContext().put("inputStream", 
			new ByteArrayInputStream(
					JSONResponse.createLoginRequiredResponse(
						"/ScopeProject/admin/" +
								actionInvocation.getInvocationContext()
									.getName().replace(".action", "")).toBytes()));
	}
	
	@Override
	public String intercept(ActionInvocation actionInvocation) throws Exception 
	{
		/* Declarations */
		String result;
		
		
		/* Attempts to get the userID and password from the session */
		HttpServletRequest request = (HttpServletRequest)actionInvocation
				.getInvocationContext().get(StrutsStatics.HTTP_REQUEST);



		HttpSession session = request.getSession();

		System.out.println(request.getRequestURI());

		

		String userID 	= (String) session.getAttribute("userID");
		String password = (String) session.getAttribute("password");

		/* No session with userID and password */
		if(StringUtils.isBlank(userID) || StringUtils.isBlank(password))
		{
			/* Attempts to the get the user ID and password from the
			 * parameters */
			userID 	 = (String) request.getParameter("userID");
			password = (String) request.getParameter("password");

			/* No parameters with userID and password, let's redirect to the
			 * login page */
			if(StringUtils.isBlank(userID) || StringUtils.isBlank(password))
			{

				session.setAttribute("originalPath", 
						actionInvocation.getInvocationContext().getName().replace(".action", ""));

				setOutput(actionInvocation);
				
				result = "streamOutput";	

			/* Parameters have been provided so let's verify the credentials */
			}else
			{ 
				/* Good username/password so */
				if(new SecurityService().verifyAccount(userID, password))
				{
					/* Stores the username/password in the session */
					session.setAttribute("userID",   userID);
					session.setAttribute("password", password);

					/* Call the action */
					result = actionInvocation.invoke();

				/* Bad username/password */
				}else
				{
					Object action = actionInvocation.getAction();

					if(action instanceof ValidationAware) 
					{
						((ValidationAware) action).addActionError ("Username or password incorrect.");
					}

					setOutput(actionInvocation);
					
					result = "streamOutput";
				}
			}

		/* Credentials provided in the session */
		}else
		{
			/* Session credentials are good */
			if(new SecurityService().verifyAccount(userID, password))
			{	
				result = actionInvocation.invoke();

				/* Session credentials are bad */
			}else
			{
				Object action = actionInvocation.getAction();

				if(action instanceof ValidationAware) 
				{
					((ValidationAware) action).addActionError("Username or password incorrect.");
				}

				setOutput(actionInvocation);
				
				result = "streamOutput";
			}
		}



		/* Returns the result of either action or the redirection to login */
		return result;
	}
}
