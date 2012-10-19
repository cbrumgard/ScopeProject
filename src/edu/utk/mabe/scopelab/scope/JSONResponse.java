package edu.utk.mabe.scopelab.scope;

import java.io.UnsupportedEncodingException;

import net.sf.json.JSONObject;

public class JSONResponse 
{
	/* Instance variable */
	protected JSONObject jsonObject = new JSONObject();
	
	
	public static JSONResponse createLoginRequiredResponse(String url)
	{
		JSONResponse jsonResponse = new JSONResponse();
		jsonResponse.jsonObject.element("msgType", "login");
		jsonResponse.jsonObject.element("data", url);
		
		return jsonResponse;
	}
	
	public static JSONResponse createStackTraceResponse(String data)
	{
		JSONResponse jsonResponse = new JSONResponse();
		jsonResponse.jsonObject.element("msgType", "stackTrace");
		jsonResponse.jsonObject.element("data", data);
		
		return jsonResponse;
	}
	
	public static JSONResponse createErrorResponse(String data)
	{
		JSONResponse jsonResponse = new JSONResponse();
		jsonResponse.jsonObject.element("msgType", "error");
		jsonResponse.jsonObject.element("data", data);
		
		return jsonResponse;
	}
	
	public static JSONResponse createDataResponse(JSONObject data)
	{
		JSONResponse jsonResponse = new JSONResponse();
		jsonResponse.jsonObject.element("msgType", "data");
		jsonResponse.jsonObject.element("data", data);
		
		return jsonResponse;
	}
	
	public byte[] toBytes() throws UnsupportedEncodingException
	{
		return jsonObject.toString().getBytes("utf-8");
	}
}



