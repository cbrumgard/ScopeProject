<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Sessions Page</title>

<script type="text/javascript">

jQuery(document).ready(

	    function() 
	    {
	    	// Creates the session tabs (hidden)
	        jQuery("#session_tabs").tabs(
	                {

	                });
	    	
	    
	        for(key in adminSessionsToWatch)
	        {
	        	addSessionTab(key);
	        };
	        
	      
	        getSessionStatus("<s:property value='sessionName' />");
	    });


function getSessionStatus(sessionID)
{
    console.log("Inside of getSessionStatus for "+sessionID);
    
    jQuery.ajax(
    	{
    		url: "/ScopeProject/admin/getSessionStatus",
    		async: true,
    		data: {  
    			     sessionID : sessionID
    			  },
            error: function(jqXHR, textStatus, errorThrown)
            {
            	raiseErrorDialog("Request failed: " + errorThrown);
            },
            
            success: function(dataObject, textStatus, jqXHR)
            {
            	console.log(dataObject);
            	
            	// Gets the data 
            	var data = dataObject.data;
            	
            	
            	var tabContent = '';
            	
            	tabContent += 'Active: ' + data['Active'] + '<br/>';
            	tabContent += 'Collecting Participants: ' + data['CollectingParticipants'] + '<br/>';
            	tabContent += 'Number of participants: ' + data['NumberParticipants'] + 
            	              ' of '+ data['MaxNumberOfParticipants'] + '<br/>';
            	
            	              
            	console.log(data['NumberParticipants'] + " " +data['MaxNumberOfParticipants']);
            	
            	if(data['Active'] == false)
            	{
            		tabContent += 
            			"<button type='button' onclick='activateSession("+
            			'"'+sessionID+'"'+")'>Activate Session</button>";
            				
            	}else if(data['NumberParticipants'] == 
            		     data['MaxNumberOfParticipants'])
            	{
            		tabContent += 
                        "<button type='button' onclick='runSession(" +
                        '"'+sessionID+'"'+")'>Run Session</button>";
            	}
            	              
            
            	// Adds the html to the body of the tab
            	jQuery('#'+sessionID+'_tab').html(tabContent);
            	
            	
            	// Schedule the session check again 
                setTimeout(function() { getSessionStatus(sessionID); }, 3000);
            }
            
    	}).done(function() { })
          .fail(function(jqXHR, textStatus) { raiseErrorDialog("Request failed: " + textStatus); })
          .always(function() { });;
    
    
   
}



</script>

</head>
<body>
<div id="session_tabs">
    <ul> </ul>
</div>
</body>
</html>