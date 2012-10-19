package edu.utk.mabe.scopelab.scope.admin.action;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class GetNetworkInterfacesAction extends BaseScopeAction 
{
	/* Serialization stuff */
	private static final long serialVersionUID = -6707156806361684669L;

	
	public GetNetworkInterfacesAction() 
	{
		// Do nothing
	}

	@Override
	public String execute() throws Exception 
	{
		System.out.println("In GetNetworkInterfaces");
		
	
		JSONObject addresses = new JSONObject();
		
		Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
		while(nics.hasMoreElements())
		{
		    NetworkInterface nic = nics.nextElement();	

		    Enumeration<InetAddress> addrs = nic.getInetAddresses();
		    
		    while(addrs.hasMoreElements())
		    {
		    	InetAddress addr = addrs.nextElement();
		    	
		    	System.out.printf("host: %s\n", addr.getHostName());
		       
		    	addresses.element(addr.getHostName(), addr.getHostName());
		    	
		    }
		}
		
		/* Send the data message */
		return setDataMessage(addresses);
	}
}
