<%@ page language="java" contentType="text/html; charset=US-ASCII"
    pageEncoding="US-ASCII"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<title>Load Named Graph Page</title>

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

    function sendGraph()
    {
    	console.log('Sending graph');
    	
    	// Gets the name and the file for the graph
    	var graphName = jQuery('#graphName').val();
    	var graphFile = jQuery('#graphFile').val();
    	
    	// Checks a name was specified
    	if(graphName == '')
    	{
    	   raiseErrorDialog("Must specify a name");
    	   return;
    	}
    	
    	// Checks a file was specified
    	if(graphFile == '')
    	{
    	   raiseErrorDialog("Must specify a file");
    	   return;
    	}
    	
    	jQuery("#progress-dialog").dialog("open");
    	
    	 // Sends the command to the server
        jQuery.ajax(
                {
                    url: "/ScopeProject/admin/loadNamedGraph",
                    type: "POST",
                    async: false,
                    data: {  graphName: graphName,
                             graphFile: window['graphfile']
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

Please load your graph:<br/></br>

<form action="javascript:sendGraph()">
    <label for="graphName">Name:</label>
    <input type="text" id="graphName" name="graphName" /><br/>
    <label for="graphFile">File:</label>
    <input type="file" id="graphFile" name="graphFile" 
           onchange="javascript:readFile(this.files[0], 'graphfile')"/><br/>
    <input type="submit" />
</form>

</body>
</html>