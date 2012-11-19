package edu.utk.mabe.scopelab.scope;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.UnsupportedEncodingException;

import net.sf.json.JSONObject;

import com.opensymphony.xwork2.ActionSupport;


public abstract class BaseScopeAction extends ActionSupport 
{
	/* Serialization stuff */
	private static final long serialVersionUID = 3326157713205926428L;

	/* Instance variables */
	protected InputStream inputStream = null;
	
	
	protected String setErrorMessage(String errorMsg) 
			throws UnsupportedEncodingException
	{
		inputStream = new ByteArrayInputStream(
				JSONResponse.createErrorResponse(errorMsg).toBytes());
		
		return "streamOutput";
	}
	
	protected String setDataMessage(JSONObject data) 
			throws UnsupportedEncodingException
	{
		inputStream = new ByteArrayInputStream(
				JSONResponse.createDataResponse(data).toBytes());
		return "streamOutput";
	}
	
	public InputStream getInputStream() 
	{
		return inputStream;
	}
}
