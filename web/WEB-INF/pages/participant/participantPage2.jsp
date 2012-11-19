<%@ page language="java" contentType="text/html; charset=US-ASCII"
	pageEncoding="US-ASCII"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">

<title>Participant Page</title>

<link rel="stylesheet"
	href="http://code.jquery.com/ui/1.9.0/themes/base/jquery-ui.css" />
<script src="http://code.jquery.com/jquery-1.8.2.js"></script>
<script src="http://code.jquery.com/ui/1.9.0/jquery-ui.js"></script>
<script src="http://crypto-js.googlecode.com/svn/tags/3.0.2/build/rollups/sha1.js"></script>
<script src="/ScopeProject/resources/js/stomp.js"></script>
 
<script type="text/javascript">

    // Globals
    scopeParticipantApp = 
        {
    		participantID: null,
    	    sessionID: null,
    	    activeSessions: { },
            webSocketAddress: '<s:property value="webSocketURI"/>',
            newSessionAnnouncementTopicName: "/topic/" +
              '<s:property value="newSessionAnnouncementTopicName"/>',
            
            webSocket : null,   
        };
   
    function getActiveSessions(callback) 
    {
        // Send ajax request 
        jQuery.ajax(
            {
                url : "/ScopeProject/participant/getActiveSessionList",
                async : true,
                type : "POST",

                error : function(jqXHR,
                        textStatus, errorThrown) {
                    jQuery("#main_div").html(
                            textStatus);
                },

                success : function(dataObject, textStatus, jqXHR) 
                {
                    processJSONResult(dataObject,function(data) 
                            {
                                for(var session in data) 
                                {
                                    callback(data[session]);
                                }
                            });
                }

            }).always(
                function(jqXHR, textStatus) 
                {
                    console.log("Done. textStatus="+ textStatus);
                })
              .error(
                function(jqXHR, textStatus) {
                    console.log("Error. responseText="
                                    + jqXHR.responseText
                                    + " textStatus="
                                    + textStatus);
                });
    }

    function listenForSessionAnnoucements(topicName) 
    {
        client = getConnection();

        client.subscribe(
                topicName,
                function(message) 
                {
                    console.log(message.body);

                    // Parses the response
                    var response = JSON.parse(message.body);

                    var sessionID = response.sessionID;
                    
                    if(sessionID in scopeParticipantApp.activeSessions == false) 
                    {
                    	scopeParticipantApp.activeSessions[sessionID] = 
                    		   { 
                    			 sessionID: sessionID,
                    			 joinDestination: response.joinDestination
                    		   };

                        addSessionToSessionSelectBox(sessionID);
                    }
                });
    }

    
   
    function getConnection()
    {
        if(scopeParticipantApp.webSocket == null)
        {
            var client = Stomp.client(scopeParticipantApp.webSocketAddress);
            
            client.debug = function(str) 
            {
                console.log(str);
            };
            
            var onconnect = function(frame) 
            {
                console.log("Connected!!!");
                
                // Listen for new sessions
                listenForSessionAnnoucements(
                        scopeParticipantApp.newSessionAnnouncementTopicName);
            };
            
            console.log("connecting...");
            client.connect("", "", onconnect);
            
            scopeParticipantApp.webSocket = client;
        }
        
        
        return scopeParticipantApp.webSocket;
    }
    
        
    // Check if webSocket are supported
    if(window.WebSocket == false)
    {
    	alert("WebSockets are not supported.  Try a real browser instead.")
    }
   
    function getParticipantID()
    {
    	jQuery('#participant_dialog').dialog("open");
    	
    	console.log(jQuery('#participant_dialog'));
    }
    
  
    
    function raiseStackTrace(stacktraceMsg)
    {
         jQuery("#stacktrace-dialog-message").html(stacktraceMsg);
         
         console.log(jQuery("#stacktrace-dialog"));
         
         jQuery("#stacktrace-dialog").dialog("open");   
    }
    
    function raiseErrorDialog(errMsg)
    {
        jQuery("#error-dialog-message").html(errMsg);
        jQuery("#error-dialog").dialog("open");
    }
    
    function processJSONResult(dataObject, dataCallback)
    {
        switch(dataObject.msgType)
        {
           // Login required so raise the login window
           case "login":
              raiseLoginDialog(dataObject.data);
              break;
              
           // Stacktrace result so raise stack trace window
           case "stackTrace":
              raiseStackTrace(dataObject.data);
              break;
              
           // Error result so raise error window
           case "error":
              raiseErrorDialog(dataObject.data);
              break;
              
           // Data result so call callback function
           case "data":
              dataCallback(dataObject.data);
              break;
        }
    }
    
    
    function addSessionToSessionSelectBox(sessionName)
    {
        // Add the session to the select box 
    	jQuery("#session_select").append(new Option(sessionName, sessionName));

        // Display the session box 
		jQuery("#session_select").show();
	}

    function requestToJoinSession(sessionID)
    {
    	console.log("Requesting to join sessionID = "+sessionID);
    	
    	var queueName = null;
    	
    	jQuery.ajax(
    			{
			    	url : "/ScopeProject/participant/getMessageQueue",
			        async : false,
			        type : "POST",
			
			        error : function(jqXHR, textStatus, errorThrown) 
			                {
			                   alert(textStatus);
			                },
			
			        success : function(dataObject, textStatus, jqXHR) 
			        {
			            processJSONResult(dataObject, function(data) 
			                    {
			                       console.log("queue = "+data.queueName);
			                       
			                       queueName = data.queueName;
			                    });
			        }
			
			    }).always(
			        function(jqXHR, textStatus) 
			        {
			            console.log("Done. textStatus="+ textStatus);
			        })
			      .error(
			        function(jqXHR, textStatus) {
			            console.log("Error. responseText="
			                            + jqXHR.responseText
			                            + " textStatus="
			                            + textStatus);
			        });
    	
    	var client = getConnection();
    	
    	var session = scopeParticipantApp.activeSessions[sessionID];
    	
    	console.log("destination = "+"/queue/"+session.joinDestination);
    	
    	console.log("ParticipantID = "+ scopeParticipantApp.participantID.toString());
    	
    	
    	console.log("Queuename = "+queueName.replace("queue://", "/queue"));
    	
    	// Subscribe for response from the scope server
    	client.subscribe(queueName.replace("queue://", "/queue/"), function(message)
    			{
    		         console.log("Message = "+message);
    		         
    		         dataObject = JSON.parse(message.body);
    		         
    		         processJSONResult(dataObject, function(data)
    		        		 {		        	 
    		        	         alert("Successfully joined!!!");
    		        	         
    		        	         console.log("data = " + data);
    		        	         
    		        	         for(var i=0; i<data["ListenQueues"].length; i++)
    		        	         {
    		        	        	 var listenQueue = "/queue/"+data["ListenQueues"][i];
    		        	        	 
    		        	        	 console.log(listenQueue);
    		        	        	 
    		        	        	 client.subscribe(listenQueue, function(message)
    		        	        			 {
    		        	        		         console.log(message);
    		        	        			 });
    		        	         }
    		        	         
    		        	         var topicName = "/topic/"+data["PublishTopic"];
    		        	         console.log(topicName);
    		        	         
    		        	         var counter = 0;
    		        	         
    		        	         setInterval(function()
    		        	            {
    		        	        	    console.log("sending message to "+topicName);
    		        	        	    client.send(topicName, {}, "Hello there "+(counter++));
    		        	            }, 3000);
    		        	        	
    		        		 });
    		         
    		        
    		    
    			});
    	
    	
    	// Send the request to join the session 
    	client.send("/queue/"+session.joinDestination, {}, 
    			JSON.stringify(
    			{
    			    participantID : scopeParticipantApp.participantID.toString(),
    			    queueName : queueName
    		    }));    	
    }
    
	jQuery(document)
			.ready(
					function() {
						// Error Dialog box 
						jQuery('#error_dialog').dialog({
							autoOpen : false,
							modal : true,
							buttons : {
								Ok : function() {
									// Close the dialog box
									jQuery(this).dialog("close");
								}
							}
						});

						// stacktrace pages
						jQuery("#stacktrace_dialog").dialog({
							autoOpen : false,
							modal : true,
							buttons : {
								Ok : function() {
									// Close the dialog box
									jQuery(this).dialog("close");
								}
							}
						});

						// Sets up the participant dialog box
						jQuery('#participant_dialog').dialog(
								{
									autoOpen : false,
									modal : true,
									buttons : 
									{
										"Ok" : function() {
											var emailAddress = jQuery(
													'#email_field').val();

											console.log(emailAddress);

											// Calculates the sha-1 hash of the input
											scopeParticipantApp.participantID = CryptoJS
													.SHA1(emailAddress);

											jQuery('#participant_dialog')
													.dialog("close");

											if(scopeParticipantApp.sessionID == null) 
											{
												jQuery("#session_dialog")
														.dialog("open");
											}
										}
									}
								});

						// Sets up the session dialog box
						jQuery("#session_dialog").dialog({
							autoOpen : false,
							modal : true,

							// On open query for the events */
							open : function(event, ui) 
							{
								jQuery("#session_select").hide();

								getConnection();
								
								getActiveSessions(function(session) 
									{
								        console.log("Session is "+session);
								        
										if(session.sessionID in scopeParticipantApp.activeSessions == false) 
	                                    {
											scopeParticipantApp.activeSessions[session.sessionID] =   
											    { 
					                                 sessionID: session.sessionID,
					                                 joinDestination: session.joinDestination.replace("queue://","")
					                            };
	
	                                        addSessionToSessionSelectBox(session.sessionID);
	                                    }
								    });
							}
						});

						
						
						// Need to get the participant ID
						if(scopeParticipantApp.participantID == null) 
						{
							scopeParticipantApp.participantID = getParticipantID();
						}

						
						//Hide the session select box 
						jQuery("#session_select").hide();

						

						
					});
	
	 
</script>


<style type="text/css">
body.main {
	background-color: #e1ddd9;
	font-size: 12px;
	color: #564b47;
	padding: 20px;
	margin: 0px;
	text-align: center;
}

div.center_panel {
	text-align: left;
	vertical-align: middle;
	margin: 0px auto;
	padding: 0px;
	width: 550px;
	background-color: #ffffff;
	border: 1px;
	height: 600px;
}
</style>




</head>
<body class="main">
	<div class="center_panel">

        <!-- Error dialog -->
        <div id="error_dialog" title="Error Occurred">
            <p id="error-dialog-message"></p>
        </div>
        
        <!-- Stacktrace -->
        <div id="stacktrace_dialog" title="Stacktrace">
            <p id="stacktrace_dialog_message"></p>
        </div>
    
        <!-- Participant dialog  -->
		<div id="participant_dialog" title="Participant Login">
			<form>
				<label for="email">Email Address</label>
				<input type="text" id="email_field" />
			</form>
		</div>

        <!-- Session dialog -->
        <div id="session_dialog" title="Select session">
            <select size="4" name="session_select" id="session_select" 
                class="listbox" ondblclick="requestToJoinSession(this.value)">
               <!--   style="visibility:hidden">-->
            </select>
        </div>
        
        <table width="100%">
            <tr>
                <td width="50%">
                    <div class="ui-widget ui-state-default ui-corner-all" title="News Feed">
                        News Feed
                    </div>
                </td>
                <td width="50%">
                    <div class="ui-widget ui-state-default ui-corner-all" title="Neighbors">
                        Neighbors
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <div class="ui-widget ui-state-default ui-corner-all" title="Buttons">
                       buttons
                    </div>
                </td>
            </tr>
        </table>
        
	</div>
</body>
</html>