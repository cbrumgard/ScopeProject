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
    GRAPHTYPE  = 'graphType';
    SCRIPTTYPE = 'scriptType';
    GRAPHFILE  = 'graphfile';
    SCRIPTFILE = 'scriptfile';
    
    
    // Globals
    var graphType = undefined;
    var ws = undefined;
    
    function validateEBA()
    {
        
    }
    
    function getScriptNames()
    {
    	// Ajax query
        jQuery.ajax(
            {
                url: "/ScopeProject/admin/getNamedScriptList",
                type: "POST",
                async: true,
                
                error: function(jqXHR, textStatus, errorThrown) 
                  {
                     // TODO handle error 
                     console.log(textStatus);
                  },
                  
                success: function(dataObject, textStatus, jqXHR) 
                  {
                     processJSONResult(dataObject, function(data)
                        {
                            console.log(data);
                            
                            if(jQuery.isArray(data.scriptNames))
                            {
	                            jQuery.each(data.scriptNames, function(index, scriptName)
	                            {
	                                console.log(scriptName);  
	                               
	                                jQuery("#scriptNameSelect").append(
	                                        new Option(scriptName, scriptName));
	                            });
                            
                            }else
                            {
                            	jQuery("#scriptNameSelect").append(
                                   new Option(data.scriptNames, data.scriptNames));
                            }
                        });     
                  }
                  
            }).fail(function(jqXHR, textStatus) { raiseErrorDialog("Request failed: " + textStatus); });
    }
    
    
    function getGraphNames()
    {
    	// Ajax query
        jQuery.ajax(
            {
                url: "/ScopeProject/admin/getNamedGraphList",
                type: "POST",
                async: true,
                
                error: function(jqXHR, textStatus, errorThrown) 
                  {
                     // TODO handle error 
                     console.log(textStatus);
                  },
                  
                success: function(dataObject, textStatus, jqXHR) 
                  {
                     processJSONResult(dataObject, function(data)
                    	{
                    	    console.log(data);
                    	    
                    	    if(jQuery.isArray(data.graphNames))
                    	    {
	                    	    jQuery.each(data.graphNames, function(index, graphName)
	                    	    {
	                    	        console.log(graphName);	 
	                    	       
	                    	        jQuery("#graphNameSelect").append(
	                    	        	new Option(graphName, graphName));
	                    	    });
                    	    }else
                    	    {
                    	    	jQuery("#graphNameSelect").append(
                                    new Option(data.graphNames, data.graphNames));	
                    	    }
                    	   
                    	});     
                  }
                  
            }).fail(function(jqXHR, textStatus) { raiseErrorDialog("Request failed: " + textStatus); });
    	
    	
    }
    
    function switchScriptType(value)
    {
    	var form = document.getElementById("scriptForm");
    	
    	console.log("Script Type ="+value);
    	
    	window[SCRIPTTYPE] = value;
    	
    	if(value == 'FILE')
    	{
    		form.innerHTML = document.getElementById("scriptQuestions").attributes["FILE"].value;
    	}else if(value == 'NAMED')
    	{
    		form.innerHTML = document.getElementById("scriptQuestions").attributes["NAMED"].value;
    		getScriptNames();
    	}else
        {
            form.innerHTML = "";    
        }
    	
    	 jQuery('#scriptSelect').children('option[value="choose..."]').attr('disabled', true);
    }
    
    function switchGraphParameters()
    {
        var graphSelect = document.getElementById("graphSelect");
        graphType   = graphSelect.options[graphSelect.selectedIndex].value;
     
        var form = document.getElementById("extraFormData");
        
        window[GRAPHTYPE] = graphType;
        
        /* Extended BA */
        if(graphType == "EBA")
        {
            form.innerHTML = document.getElementById("graphQuestions").attributes["EBA"].value;
            
        /* User input graph */
        }else if(graphType == "USER") 
        {
            form.innerHTML = document.getElementById("graphQuestions").attributes["USER"].value;
        }else if(graphType = "NAMED")
        {
        	form.innerHTML = document.getElementById("graphQuestions").attributes["NAMED"].value;
        	getGraphNames();
        }else
        {
        	form.innerHTML = "";	
        }
        
        jQuery('#graphSelect option[value="choose..."]').attr('disabled', true);
        
    }
    
    
    function onSubmit()
    {
    	// Ajax query
        jQuery.ajax(
        	{
        		url: "/ScopeProject/admin/createSession",
        		type: "POST",
        		async: true,
        		data: { 
                        sessionName : jQuery('#sessionName').val(),
                        scriptType : window[SCRIPTTYPE],
                        scriptfile : window[SCRIPTFILE],
                        graphType : window[GRAPHTYPE],
                        graphfile : window[GRAPHFILE], 
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
        			 
        			
        			 // Normal page return
       	             if(typeof(dataObject) == "string")
       	             {
       	            	//params["sessionName"]
       	            	adminSessionsToWatch[jQuery('#sessionName').val()] = true;
       	            	 
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
    
    function setGraphName(name)
    {
    	window[GRAPHFILE] = name;
    }
    
    function setScriptName(name)
    {
    	window[SCRIPTFILE] = name;
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
    
    </script>
</head>
<body>


 <div id='scriptQuestions'
    FILE="<label for='script'>File: </label>
         <input type='file' name='script' id='script' onchange='readFile(this.files[0], SCRIPTFILE);'/><br/>"
         
    NAMED="<label for='scriptNameSelect'>Name: </label>
           <select id='scriptNameSelect' name='scriptNameSelect' 
               onchange='javascript:setScriptName(this.options[this.selectedIndex].value)'>
               <option disabled>Choose...</option>
           </select><br />"
 ></div>

 <div id="graphQuestions" 
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
           
    NAMED="<label for='graphNameSelect'>Graph: </label>
           <select id='graphNameSelect' name='graphNameSelect' 
                onchange='javascript:setGraphName(this.options[this.selectedIndex].value)'>
                <option disabled>Choose...</option>
           </select><br />
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
        <label for='scriptSelect'>Script Type</label>
        <select name='scriptSelect' id='scriptSelect'
            onchange="javascript:switchScriptType(this.options[this.selectedIndex].value)">
            <option value='choose...'>Choose...</option>
            <option value="FILE">File</option>
            <option value="NAMED">Named</option>
        </select>
        <table><tr><td id='scriptForm'></td></tr></table>
        <label for="graphSelect">Select Graph Type</label>
        <select id="graphSelect" name="graphType"  onchange="switchGraphParameters();" >
            <option value='choose...'>Choose...</option>
            <option value="EBA">EBA</option>
            <option value="USER">User</option>
            <option value="NAMED">Named</option>
        </select>         
        </td>
        </tr>
        <tr><td id="extraFormData" />
        </tr>
        </table>
    </form>
   
</body>
</html>