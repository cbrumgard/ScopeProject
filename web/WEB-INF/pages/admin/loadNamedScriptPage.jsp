<%@ page language="java" contentType="text/html; charset=US-ASCII"
    pageEncoding="US-ASCII"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
	<title>Load Named Script Page</title>
	
	<!--  <link rel="stylesheet" href="http://code.jquery.com/ui/1.9.0/themes/base/jquery-ui.css" />
	<script src="http://code.jquery.com/jquery-1.8.2.js"></script>
	<script src="http://code.jquery.com/ui/1.9.0/jquery-ui.js"></script> -->
	    
	<script type="text/javascript">
	
	jQuery(function() 
	        {
	           jQuery("#progress-dialog").dialog(
	                   {
	                       autoOpen: false,
	                       modal: true,
	                   });   
	        });
	
	
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
	
	function sendScript()
	{
	    console.log("Sending script");	
	    
	    console.log("name = " + jQuery('#scriptName').val());
        console.log("script = " + jQuery('#scriptFile').val());
	    
	    var scriptName = jQuery('#scriptName').val();
	    var scriptFile = jQuery('#scriptFile').val();
	    
	    // Checks that a script name was given
	    if(scriptName == '')
	    {
	    	raiseErrorDialog("Must specify a script name");
	    	return;	
	    }
	    
	    // Checks that a file was given
	    if(scriptFile == '')
	    {
	    	raiseErrorDialog("Must specify a script");
	    	return;
	    }
	    
	    jQuery("#progress-dialog").dialog("open");
	    
	    // Sends the command to the server
	    jQuery.ajax(
	            {
	                url: "/ScopeProject/admin/loadNamedScript",
	                type: "POST",
	                async: false,
	                data: {  scriptName: scriptName,
	                	     scriptFile: window['scriptfile']
	                	  },
	                error: function(jqXHR, textStatus, errorThrown) 
	                  {
	                     // TODO handle error 
	                     console.log(textStatus);
	                  },
	                  
	                success: function(dataObject, textStatus, jqXHR) 
	                  {
	                     // TODO handle success 
	                     console.log(dataObject);    
	                     
	                     processJSONResult(dataObject, function(data)
	                    		 {
	                    	         raiseMessageDialog('Success!!!');
	                    		 });
	                     
	                  }
	                  
	            }).fail(function(jqXHR, textStatus) 
	            	    { 
	            	       raiseErrorDialog("Request failed: " + textStatus); 
	            	    })
	              .always(function(jqXHR, textStatus) 
	            		  { 
	            	         jQuery("#progress-dialog").dialog("close");
	            	      });
	    
	   
	}
	
	</script>
</head>
<body>

<div id="progress-dialog" title="Starting Server">
    <img src="/ScopeProject/resources/images/progress_wheel.gif" /><br/>
    Waiting on the server...
    <!-- <div id="progressbar"></div>  -->
</div>

Please load your script: <br/><br/>

<form action="javascript:sendScript()">
    <label for="scriptName">Name:  </label>
    <input id="scriptName" name="scriptName" type="text" />
    <br/>
    <label for="scriptFile">Script:</label>
    <input id="scriptFile" name="scriptFile" type="file" 
           onchange="javascript:readFile(this.files[0], 'scriptfile');" />
    <br/>
    <input type="submit" />
</form>

</body>
</html>