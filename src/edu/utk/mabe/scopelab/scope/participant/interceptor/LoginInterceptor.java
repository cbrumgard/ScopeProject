package edu.utk.mabe.scopelab.scope.participant.interceptor;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

import edu.utk.mabe.scopelab.scope.JSONResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.StrutsStatics;

public class LoginInterceptor implements Interceptor 
{
	private static final long serialVersionUID = -6075146515933367940L;

	@Override
	public void destroy() 
	{
		// Do nothing
	}

	@Override
	public void init() 
	{
		// Do nothing
	}

	protected void setOutput(ActionInvocation actionInvocation) 
			throws UnsupportedEncodingException
	{
		actionInvocation.getInvocationContext().put("inputStream", 
			new ByteArrayInputStream(
					JSONResponse.createLoginRequiredResponse(
						"/ScopeProject/participant/" +
								actionInvocation.getInvocationContext()
									.getName().replace(".action", "")).toBytes()));
	}
	
	@Override
	public String intercept(ActionInvocation actionInvocation) throws Exception 
	{
		String result;
		
		System.out.println("Participant interceptor");
		
		/* Gets the servlet request */
		HttpServletRequest request = 
				(HttpServletRequest)actionInvocation.getInvocationContext()
					.get(StrutsStatics.HTTP_REQUEST);
		
		/* Gets the session */
		HttpSession session = request.getSession();
		
		/* Attempts to get the participant's ID from the session cookie */
		String participantID = (String)session.getAttribute("participantID");
		
		/* Has a participant ID so continue to the requested action */
		if(StringUtils.isNotBlank(participantID))
		{
			System.out.println("Participant ID = "+participantID);
			
			result = actionInvocation.invoke();
			
		/* Does not have a participant ID so redirect to the
		 * participant login page */
		}else
		{
			System.out.println("No participant ID in session");
			
			participantID = (String) request.getParameter("participantID");
			
			/* Has logged in and created a participant ID */
			if(StringUtils.isNotBlank(participantID))
			{
				/* Store in the session  and continue */
				session.setAttribute("participantID", participantID);
				
				/* Continue with the original action */
				result = actionInvocation.invoke();
				
			}else
			{
				/* Stores the original request path so that after login 
				 * we'll be redirected to that page
				 */
				session.setAttribute("originalPath", 
						actionInvocation.getInvocationContext().getName().replace(".action", ""));
				
				setOutput(actionInvocation);
				
				/* Need to log in */
				result = "streamOutput";
			}
		}
			
		/* Return the result */
		return result;
	}
}
