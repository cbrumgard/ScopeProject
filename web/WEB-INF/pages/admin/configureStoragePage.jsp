<%@page import="javax.naming.Context"%>
<%@page import="javax.naming.InitialContext"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Backend Storage page</title>

<script type="text/javascript">

var backendOptions = { backend : null };


function checkToEnableSubmit()
{
	 function _build(map)
	 {
		 jQuery.each(map, function(key, value) 
			{
			    console.log(key + ' = '+ value);
			    
			    if(jQuery.isPlainObject(value))
			    {
			    	_build(value);
			    }
			});
	 }
    
     _build(backendOptions);
      
     // Unhide the submit button
     jQuery('#submitDiv').show();
     
}

function sendInformation()
{
	jQuery("#progress-dialog").dialog("open");
	
	console.log(backendOptions);
	
	params = {};
	
    if(backendOptions.backend.name == 'cassandra')
    {
        params['strategy'] = backendOptions.backend.options.strategy.name;
        params['replicationFactor'] = backendOptions.backend.options.strategy.options.replicationFactor;
    }else
    {
    	raiseErrorDialog("Unknown storage backend");
    }
    
    //jQuery('#submitDiv').hide();
    
    jQuery.ajax(
            {
                url : "/ScopeProject/admin/InitializeBackendStorageAction",
                async : false,
                type : "POST",
                data : params,
                error : function(jqXHR, textStatus, errorThrown) 
                        {
                           alert(textStatus);
                        },
        
                success : function(dataObject, textStatus, jqXHR) 
                {
                	console.log(dataObject);
                	processJSONResult(dataObject, function(dataObject)
                			{
                		        alert("Success.");
                		        // Success
                		        getTestPage('/ScopeProject/admin/getBackendStoragePage', {});
                			});
                }
        
            }).always(
                function(jqXHR, textStatus) 
                {
                    console.log("Done. textStatus="+ textStatus);
                    
                    jQuery("#progress-dialog").dialog("close");
                })
              .error(
                function(jqXHR, textStatus) {
                    console.log("Error. responseText="
                                    + jqXHR.responseText
                                    + " textStatus="
                                    + textStatus);
                });
}

jQuery(document).ready(
		
    function()
    {
    	jQuery(function() 
   	        {
   	           jQuery("#progress-dialog").dialog(
   	                   {
   	                       autoOpen: false,
   	                       modal: true,
   	                   });   
   	        });
    	
	    jQuery('#cassandraDiv').hide();
		jQuery('#optionsDiv').hide();	
		
		jQuery('#submitDiv').hide();
	    
		jQuery('#backendSelect').change(function()
			{
		        var backend = jQuery(this).val();      
		      
		        switch(backend)
		        {
		        	case 'Cassandra':	
		        		backendOptions.backend = { 
		        		                            name : 'cassandra',
		        			                        options : null, 
		        			                      };
		        		
		        		jQuery('#cassandraDiv').show();
		        		break;
		        }
			});
		
		jQuery('#strategy').change(function()
			{
			    var strategy = jQuery(this).val();  
			    
			    console.log(strategy);
			    
			    switch(strategy)
			    {
			        case 'SimpleStrategy':
			        	
			           backendOptions.backend.options = 
			        	   { 
			        		   strategy : { name : 'SimpleStrategy', options : null },
			        	   };
			        	
			           jQuery('#optionsDiv').show();
			           break;
			    }
			});
		
		jQuery('#replicationFactor').change(function()
		  {
		        backendOptions.backend.options.strategy.options = 
		        	  {
		        	      replicationFactor : jQuery(this).val(),
		        	  };
		        
		        checkToEnableSubmit();
		  });
		
    }
	);
	


</script>



</head>
<body>

<div id="progress-dialog" title="Configuring backend">
    <img src="/ScopeProject/resources/images/progress_wheel.gif" /><br/>
    Waiting on the storage backend...
    <!-- <div id="progressbar"></div>  -->
</div>

<!-- Backend is not initialized -->
<s:if test='%{initialized == false}'>
    Status: Storage is not initialized. 
    <br /><br /><br />
    Initialization Parameters:<br />
    <div style='text-indent: 20px;'>
	    <form method='POST' action="javascript:sendInformation()"> <!-- action='InitializeBackendStorageAction'> -->
	        
	        <label for='#backendSelect'>Backend:</label>
	        <select name='backendSelect' id='backendSelect'>
               <option disabled selected style="display:none;">Select Backend</option>
               <option>Cassandra</option>
            </select>
	        
	        
	        <div id='cassandraDiv'>
	           <label for='strategy'>Replication Strategy:</label>
	           <select name="strategy" id='strategy'>
	              <option disabled selected style="display:none;">Select Strategy</option>
		          <option>SimpleStrategy</option>
		       </select>
		       
		       
	           <div id="optionsDiv">
	               <!-- Extra form stuff goes here -->
	            
	               <label for='replicationFactor'>Replication Factor</label>
	               <select name='replicationFactor' id='replicationFactor'> 
	                   <option disabled selected style="display:none;">Select replication level</option>
	                   <option>1</option>
	                   <option>2</option>
	                   <option>3</option>
	               </select>
	            
	           </div>
	        </div>
	        
	        <br/>
	        <div id="submitDiv" >
	           <input type="submit" value='Initialize Storage' /> 
	        </div>
	    </form>
    </div>
</s:if>

<s:else>
    Status: Storage has already been initialized.
</s:else>

</body>
</html>