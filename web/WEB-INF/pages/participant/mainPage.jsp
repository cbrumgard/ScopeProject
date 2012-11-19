<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Participant Page</title>
    
    <link rel="stylesheet" href="http://code.jquery.com/ui/1.9.0/themes/base/jquery-ui.css" />
    <script src="http://code.jquery.com/jquery-1.8.2.js"></script>
    <script src="http://code.jquery.com/ui/1.9.0/jquery-ui.js"></script>
    <script src="http://jzaefferer.github.com/jquery-validation/jquery.validate.js"></script>
    
    <script src="/ScopeProject/resources/js/stomp.js"></script>
    
    <script type="text/javascript">
    
    function processJSONResponse(dataObject)
    {
    	
    }
    
    jQuery(document).ready(function() 
    		{
                jQuery.ajax(
                		{
                			url: "/ScopeProject/participant/getWebSocketAddress",
                			async: true,
                            type: "GET",
                			
                            success: function(dataObject, textStatus, jqXHR)
                            {
                            	console.log(dataObject);
                            	
                            	switch(dataObject.msgType)
                                {
                                   // Login required so raise the login window
                                   case "login":
                                      break;
                                      
                                   // Stacktrace result so raise stack trace window
                                   case "stackTrace":
                                      break;
                                      
                                   // Error result so raise error window
                                   case "error":
                                      break;
                                      
                                   // Data result so call callback function
                                   case "data":
                                      
                            
                                	  var webSocketAddress = jQuery.validator.format("ws://{0}:{1}/stomp", dataObject.data.host, dataObject.data.port);
                                	   
                                	  console.log(webSocketAddress);
                                	  javascript:alert("WebSockets are " + (window.WebSocket ? "" : "not ") + "supported");
                                      
                
                                	  //var ws = new WebSocket(webSocketAddress, "stomp");
                                	  
                                	  //ws.onopen = function()
                                	   //  {
                                	        // Web Socket is connected, send data using send()
                                	        //ws.send("Message to send");
                                	   //     alert("socket is open");
                                	    // };
                                	     //ws.onerror = function(evt)
                                	     //{
                                	    	// alert("Error");
                                	    	 //console.log(evt);
                                	     //};
                                	     //ws.onmessage = function (evt) 
                                	     //{ 
                                	        //var received_msg = evt.data;
                                	       // alert("Message is received...");
                                	     //};
                                	     //ws.onclose = function()
                                	     //{ 
                                	        // websocket is closed.
                                	       // alert("Connection is closed..."); 
                                	     //};
                                	  
                                	  
                                	 var client = Stomp.client(webSocketAddress);
                                	  
                                	 client.debug = function(str) 
                                	 {
                                		  console.log(str);
                                	 };
                                	    
                                	 
                                	 var onconnect = function(frame) 
                                	 {
                                		  console.log("Connected!!!");
                                	   
                                		  client.subscribe("/topic/FOO.BAR", function(message) 
                                		  {
                                		       console.log(message);
                                		  });
                                		  
                                	 };
                                	    
                                	 console.log("connecting...");
                                	 client.connect("", "", onconnect);
                                	 
                                	 
                                	 
                                     break;
                                }
                            }
                		});
    		});
    
    </script>
</head>
<body>

Welcome participant <%=session.getAttribute("participantID")%> <br />

*list of sessions <br />
*join session   <br />

<br />
<form method="POST" action="participant/logout">
    <input type="submit" value="logout" />
</form>

</body>
</html>