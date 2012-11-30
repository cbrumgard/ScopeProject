<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Session creation</title>

    
    <script type="text/javascript">
 
    // Constants
    GRAPHFILE  = 'graphfile';
    SCRIPTFILE = 'scriptfile';
    
    
    // Globals
    var graphType = undefined;
 
    
    function validateEBA()
    {
        
    }
    
    function switchGraphParameters()
    {
        var graphSelect = document.getElementById("graphSelect");
        graphType   = graphSelect.options[graphSelect.selectedIndex].value;
     
        var form = document.getElementById("extraFormData");
        
        /* Extended BA */
        if(graphType == "EBA")
        {
            form.innerHTML = document.getElementById("fillin").attributes["EBA"].value;
            
        /* User input graph */
        }else if(graphType == "USER") 
        {
            form.innerHTML = document.getElementById("fillin").attributes["USER"].value;
        }  
    }
    
    function submitSessionParameters(params)
    {
    	console.log(params);
    	
    	// Ajax query
        jQuery.ajax(
        	{
        		url: "/ScopeProject/admin/createSession",
        		type: "POST",
        		async: true,
        		data: params,
        		error: function(jqXHR, textStatus, errorThrown) 
        		  {
        			 // TODO handle error 
        			 console.log(textStatus);
        		  },
        		  
        		success: function(dataObject, textStatus, jqXHR) 
        		  {
        			 // TODO handle success 
        			 console.log(dataObject);
        			 
        			
        			 // Normal page return
       	             if(typeof(dataObject) == "string")
       	             {
       	            	//params["sessionName"]
       	            	adminSessionsToWatch[params["sessionName"]] = true;
       	            	 
       	                console.log(dataObject);
       	                jQuery("#main_div").html(dataObject);

       	                // Json response object
       	             }else
       	             {
       	                processJSONResult(dataObject);

       	             }
        			 
        			 jQuery("#main_div").html(dataObject);		 
        		  }
        		  
        	}).fail(function(jqXHR, textStatus) { raiseErrorDialog("Request failed: " + textStatus); });
    } 
    
    
    function readFile(filename, variableName)
    {
    	console.log("reading "+filename);
    	
    	// Reset the window variable that holds the content
    	window[variableName] = null;
    	
        // File reader for user file
        var fileReader = new FileReader();
 
  
        // Error handler for user file 
        fileReader.onerror = function(event)
        {
            console.log("file reader error"+event); 
        };
        
        // Handles read complete event for the file reader
        fileReader.onload = function(event) 
        {
            window[variableName] = event.target.result;
        };
           
        fileReader.onloadend = function(event)
        {
            console.log("file reader onloadend" + event);
        };
        
        fileReader.onabort = function(event)
        {
            console.log("file reader abort" + event);
        };
        
        // Read the file asynchronously
        fileReader.readAsText(filename);
    }
    
    function sendEBAParameters()
    {
        // TODO Implement EBA parameters    
    }
    
    function sendUserParameters()
    {
   	    params =   
   	    	{ 
   	    		sessionName : jQuery('#sessionName').val(),
   	    		scriptfile : window[SCRIPTFILE],
                graphType : 'USER',
                graphfile : window[GRAPHFILE], 
            };
   	    
   	    // Submit the session 
   	    submitSessionParameters(params);  
    }
    
    function onSubmit()
    {
    	console.log("Submitting...");
    
    	switch(graphType)
    	{
    	    case "EBA":  sendEBAParameters();  break;
    	    case "USER": sendUserParameters(); break;
    	}
    }
    
    </script>
</head>
<body>



 <div id="fillin" 
    EBA='<label for="participantCount">Participant Count</label>
         <input type="text" id="participantCount" name="participantCount" /><br/>
         <label for="initialNumNodes">initialNumNodes</label>
         <input type="text" id="initialNumNodes" name="initialNumNodes"/><br/>
         <label for="m">M variable</label>
         <input type="text" id="m" name="m" /><br/>
         <label for="p">P probability</label>
         <s:textfield id="p" name="p"/><br/>
         <label for="q">Q probability</label>
         <input type="text" id="q" name="q"/><br/>
         <input type="submit" />'
          
    USER="<label for='user'>file</label>
           <input type='file' name='graph' id='graph' onchange='readFile(this.files[0], GRAPHFILE);' /><br /> 
           <input type='button' value='submit' onclick='onSubmit()' />"
  
 ></div>
    
    <!-- Form for graph entry parameters -->
    <form id="createSessionForm" method="POST" 
          action="javascript:onSubmit()">
        <table>
        <tr>
        <td>
        <label for="sessionName">Session Name</label> 
        <input type="text" id="sessionName" name="sessionName"/><br/>
        <label for='script'>Script File</label>
        <input type='file' name='script' id='script' onchange='readFile(this.files[0], SCRIPTFILE);'/><br/>
        <label for="graphSelect">Select Graph Type</label>
        <select id="graphSelect" name="graphType"  onchange="switchGraphParameters();" >
            <option value=""></option>
            <option value="EBA">EBA</option>
            <option value="USER">User</option>
        </select>         
        </td>
        </tr>
        <tr><td id="extraFormData" />
        </tr>
        </table>
    </form>
   
</body>
</html>