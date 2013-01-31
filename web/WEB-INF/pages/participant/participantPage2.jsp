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
            toServerDestination : null,
            
            neighbors : { },
        };
   
    function getActiveSessions(callback) 
    {
        // Send ajax request 
        jQuery.ajax(
            {
                url : "/ScopeProject/participant/getActiveSessionList",
                async : true,
                type : "POST",
                data : { 'participantID' : scopeParticipantApp.participantID.toString()  },
                error : function(jqXHR,textStatus, errorThrown) {
                    jQuery("#main_div").html(textStatus);
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
                    			 joinDestination: response.joinDestination.replace("queue://", "/queue/")
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
    }
    
  
    
    function raiseStackTrace(stacktraceMsg)
    {
         jQuery("#stacktrace-dialog-message").html(stacktraceMsg);
         
         console.log(jQuery("#stacktrace-dialog"));
         
         jQuery("#stacktrace-dialog").dialog("open");   
    }
    
    function raiseErrorDialog(errMsg)
    {
        jQuery("#error_dialog_message").html(errMsg);
        jQuery("#error_dialog").dialog("open");
    }
    
    function processJSONResult(dataObject, dataCallback)
    {
    	console.log(dataObject);
    	
        switch(dataObject.msgType)
        {
           // Login required so raise the login window
           case "login":
        	  raiseErrorDialog('You must login first'); //getParticipantID();
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

    function processNewsFeedMessage(dataObject)
    {
    	console.log(dataObject);
    
    	// Display the message in the NewsFeed 
    	jQuery('#NewsFeed').append(dataObject['message'] +'\n');
    }
    
    function choiceMade(value)
    {
    	// Sends a response to the session
    	var client = getConnection();
    
    	client.send(scopeParticipantApp.toServerDestination, {}, 
                JSON.stringify(
                {
                    participantID : scopeParticipantApp.participantID.toString(),
                    choice : value,
                }));  
    	
    	jQuery("#choices").hide();
    }
    
    function processChoiceMessage(dataObject)
    {
    	console.log(dataObject);
    	
    	
    	// Remove the previous choice
    	jQuery('#choices_table').empty();
    	
    	// Add the choice
    	var content = "<tr width='100%'><td style='text-align:center;'>"+dataObject.choice+"</td></tr>";
    	
    	content += "<tr>";
    	
    	jQuery.each(dataObject.choices, function(index, value)
            {
    		     content += "<td style='text-align:center;'><button onclick=choiceMade('"+value+"')>"+value+"</button></td>";
    		});
    	
    	content += "</tr>";
    	
    	jQuery('#choices_table').append(content);
    	
    	jQuery("#choices").show();
    }
    
    function processNeighbors(dataObject)
    {
    	console.log(dataObject);
    	
    	// Traverses the list neighbors
    	jQuery.each(dataObject['neighbors'], function(neighborID, neighbor)
    		{
    	        console.log(neighbor);
    	        console.log(neighbor.id);
    		    
    	        var rowid = "neighbors_table_tr_"+neighborID;
    		    
    		    // Neighbor is not already in the list 
                if(neighbor.id in scopeParticipantApp.neighbors == false)
                {   
                    scopeParticipantApp.neighbors[neighbor.id] = neighbor;
                
                    // Insert into table
                    jQuery('#neighbors_table').append('<tr id="'+rowid+'"><td>'+neighbor.id+'</td><td></td></tr>');
                }
    		});
    }
    
    
    function processNeighborUpdate(dataObject)
    {
    	console.log(dataObject);
    	
    	var rowid = "neighbors_table_tr_"+dataObject.neighborID;
    	
    	jQuery(rowid).empty();
    	
    	jQuery('#'+rowid).replaceWith(
    		'<tr id="'+rowid+'"><td>'+dataObject.neighborID+'</td><td>'+
    		dataObject.choice+'</td></tr>');
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
			        data : { 'participantID' : scopeParticipantApp.participantID.toString() },
			        error : function(jqXHR, textStatus, errorThrown) 
			                {
			                   alert(textStatus);
			                },
			
			        success : function(dataObject, textStatus, jqXHR) 
			        {
			            processJSONResult(dataObject, function(data) 
			                    {
			            	        // Close the session dialog box
                                   jQuery("#session_dialog").dialog("close");
			            	  
			                       queueName = data.queueName.replace("queue://", "/queue/");
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
    	
    	console.log(session);
    	console.log("destination = "+session.joinDestination);
    	
    	console.log("ParticipantID = "+ scopeParticipantApp.participantID.toString());
    	
    	
    	console.log("Queuename = "+queueName);
    	
    	// Subscribe for response from the scope server
    	id = client.subscribe(queueName, function(message)
    			{
    		         console.log("Message = "+message);
    		         
    		         dataObject = JSON.parse(message.body);
    		         
    		         processJSONResult(dataObject, function(data)
    		        		 {		        	 
    		        	         alert("Successfully joined!!!");
    		        	         
    		        	         console.log(data);
    		        	         
    		        	         client.unsubscribe(id);
    		        	         
    		        	         client.subscribe(queueName, function(message)
    		        	            {
    		        	        	    console.log(message);    
    		        	        	    
    		        	        	    /* Processes the messages */
    		        	        	    processJSONResult(JSON.parse(message.body), function(data)
    		        	        	    {
    		        	        	        /* By message subtype */
	    		        	        	    switch(data['type'])
	    		        	        	    {
	    		        	        	        // Neighbors
	    		        	        	        case 'neighbors':
	    		        	        	        	processNeighbors(data);
	    		        	        	            break;
	    		        	        	            
	    		        	        	        // News feed 
	    		        	        	        case 'newsfeed':
	    		        	        	        	processNewsFeedMessage(data);
	    		        	        	        	break;
	    		        	        	            
	    		        	        	        // Choice
	    		        	        	        case 'choice':
	    		        	        	        	processChoiceMessage(data);
	    		        	        	        	break;
	    		        	        	        	
	    		        	        	        // Neighbors Update
	    		        	        	        case 'neighbor update':
	    		        	        	        	processNeighborUpdate(data);
	    		        	        	        	break;
	    		        	        	        	
	    		        	        	        // Unknown
	    		        	        	        default:
	    		        	        	        	alert('Unknown message type');
	    		        	        	        	break;
	    		        	        	    }
    		        	        	    });
    		        	            });
    		        	         
    		        	         // Stores the queue for sending messages
    		        	         scopeParticipantApp.toServerDestination = 
    		        	        	 data["toServerDestination"].replace("queue://", "/queue/");
    		        		 });
    			});
    	
    	// Send the request to join the session 
    	client.send(session.joinDestination, {}, 
    			JSON.stringify(
    			{
    			    participantID : scopeParticipantApp.participantID.toString(),
    			    queueName : queueName.replace("/queue/", "queue://")
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

											//if(scopeParticipantApp.sessionID == null) 
											//{
											//	jQuery("#session_dialog")
											//			.dialog("open");
											//}
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
					                                 joinDestination: session.joinDestination.replace("queue://", "/queue/")
					                            };
	
	                                        addSessionToSessionSelectBox(session.sessionID);
	                                    }
								    });
							}
						});

						
						
						// Need to get the participant ID
						if(scopeParticipantApp.participantID == null) 
						{
							//scopeParticipantApp.participantID = getParticipantID();
						}

						
						//Hide the session select box 
						jQuery("#session_select").hide();

					
					    // Signin button
						jQuery('#signin_button').button().click(
							function()
							{
								scopeParticipantApp.participantID = getParticipantID();
							});
					    
						jQuery('#join_button').button().click(
                            function()
                            {
                                // No participant id 
                            	if(scopeParticipantApp.participantID == null)
                            	{
                            		raiseErrorDialog('You must login first');
                            		return;
                            	}
                            	
                                // Already a member of a session
                            	if(scopeParticipantApp.sessionID != null)
                            	{
                            		raiseErrorDialog('Already participating in '
                            				+ scopeParticipantApp.sessionID);
                            	}
                            	
                                // Open the session dialog box
                                jQuery("#session_dialog").dialog("open");
                            });
					
						jQuery('#reset_button').button().click(
                            function()
                            {
                                alert("Reset");
                            });
						
						jQuery('#quit_button').button().click(
                            function()
                            {
                                alert("Quit");
                            });
						
						
						jQuery("#choices").hide();
					});
	
	 
</script>


<style type="text/css">
body.main 
{
    font-family: "Trebuchet MS", "Helvetica", "Arial", "Verdana",
            "sans-serif";
    font-size: 90%;
        
	background-color: #e1ddd9;
	
	color: #564b47;
	padding: 20px;
	margin: 0px;
	text-align: center;
}

div.center_panel 
{
	text-align: left;
	vertical-align: middle;
	margin: 0px auto;
	padding: 0px;
	width: 550px;
	background-color: #ffffff;
	border: 1px;
	height: 600px;
}

#toolbar 
{
    padding: 4px;
   /* display: inline-block; */
    /*margin-top: 50px;*/
    margin-bottom: 10px;
    margin-left: 50px;
    margin-right: 50px;
}

#choices
{
    padding: 4px;
   /* display: inline-block; */
    /*margin-top: 50px;*/
    margin-bottom: 10px;
    margin-left: 50px;
    margin-right: 50px;
}


</style>




</head>
<body class="main" background="/ScopeProject/resources/images/charcoal-gray-parchment-paper-texture.jpg">
	<div class="center_panel ui-widget-header ui-corner-all" style="background:white;">

        <!-- Error dialog -->
        <div id="error_dialog" title="Error Occurred">
            <p id="error_dialog_message"></p>
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
        
        <br/>
        <div style="text-align:center;font-size:28px;">
            Participant Interface
        </div>
        <br/>
        
        <!-- Toolbar -->
        <div id="toolbar" class='ui-widget-header ui-corner-all' 
             style="text-align:center;background:none">
            <button id='signin_button' style="font-size:12px;">Sign in</button>
            <button id='join_button' style="font-size:12px;">Join Test</button>
            <button id='reset_button' style="font-size:12px;">Reset</button>
            <button id='quit_button' style="font-size:12px;">Quit</button>
        </div>
        
     
        <table width='100%'>
            <tr><td style="text-align:right;" width="50%">Status:</td><td></td></tr>
        </table>
        <!--  <div style="text-align:center;">Status: Huh?</div> -->
        
        <br/>
        
        <table width="100%">
            <tr style="text-align:center;vertical-align:top;">
                <td width="50%">
                    <table class="ui-widget-header ui-corner-all" 
                        style="text-align:center;background:none"
                        width="100%"
                        title="News Feed"> 
                        <tr><td style="text-align:center;">News Feed<br/><hr width="30%"/></td></tr>
                        <tr><td width="100%">
                            <div  style="width:95%;">
                            <textarea id='NewsFeed' readonly rows="10" style="resize:none;width:95%;border:none"></textarea>
                            </div>
                        </td></tr>
                    </table>
                </td>
                <td width="50%" height="100%">
                    <table class="ui-widget-header ui-corner-all" 
                        style="text-align:center;background:none"
                        width='100%' 
                        title="Neighbors">
                        <tr>
                            <td style="text-align:center;vertical-align:top;">
                                Neighbors<br/><hr width="30%"/>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <table id="neighbors_table" height="100%">
                                </table>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
        
        <br/></br>
        
        <div id="choices" class='ui-widget-header ui-corner-all' 
             style="text-align:center;background:none" title="Choices">
             Choices<br/><hr width="20%"/>
             <table id="choices_table" width="100%" style="text-align:center;"></table>
        </div>
        
	</div>
</body>
</html>