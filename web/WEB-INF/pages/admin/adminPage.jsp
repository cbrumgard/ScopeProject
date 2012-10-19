<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="s" uri="/struts-tags" %>

<!doctype html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Scope Admin Page</title>
   
    <link rel="stylesheet" href="http://code.jquery.com/ui/1.9.0/themes/base/jquery-ui.css" />
    <script src="http://code.jquery.com/jquery-1.8.2.js"></script>
    <script src="http://code.jquery.com/ui/1.9.0/jquery-ui.js"></script>
    
   
    <!-- Functions -->
    <script type="text/javascript">
    
    function getTestPage(url, params)
    {
    	console.log("Params ="+params);
    	
    	// Sends an async request for the page
    	jQuery.ajax({url:      url,
    		         //dataType: "json",
    		         async:    true,
    		         data: params,
    		         error: function(jqXHR, textStatus, errorThrown)
    		         {
    		        	 jQuery("#main_div").html(textStatus);  
    		         },
    		         
    		         success: function(dataObject, textStatus, jqXHR)
    		         {
    		        	 console.log(dataObject);
    		        	 
    		        	 // Normal page return
    		        	 if(typeof(dataObject) == "string")
    		        	 {
    		        		 console.log(dataObject);
    		        		 jQuery("#main_div").html(dataObject);
    		        		
    		        	 // Json response object
    		        	 }else
    		        	 {
    		        		 switch(dataObject.msgType)
    		        		 {
    		        			case "login":
    		        			   jQuery("#login-dialog-url").val(dataObject.data);
                                   jQuery("#login-dialog").dialog("open");
    		        			   break;
    		        			   
    		        			case "stackTrace":
    		        			   break;
    		        			   
    		        			case "error":
    		        			   console.log(dataObject.data);
                                   jQuery("#error-dialog-message").html(dataObject.data);
                                   jQuery("#error-dialog").dialog("open");
    		        			   break;
    		        			   
    		        			case "data":
    		        			   break;
    		        		 }
    		        	 }
    		         }
    		         
    	            }).done(function() { })
    	              .fail(function() { })
    	              .always(function() { });
    }
    
    
   
    
    jQuery(
    	function() 
    	{
    		jQuery("#menu").menu(
    			{
    				select: function(event, ui)
    				    {
	    					var link = ui.item.children( "a:first" );
	    	                
	    					if (link.attr( "target" ) || event.metaKey || event.shiftKey || event.ctrlKey ) 
	    	                {
	    	                    return;
	    	                }
	    	                	
	    					location.href = link.attr( "href" );
	    					
	    					switch(link.attr( "href" )) 
	    					{
	    					 
								case "#serverPage":
									getTestPage('/ScopeProject/admin/getServerStatusPage');
									break;
								
								case "#configureServerPage":
								    getTestPage('/ScopeProject/admin/getServerPageAction');
									break;
									
								case "#backendstorage":
									getTestPage('/ScopeProject/admin/getBackendStoragePage');
									break;
									
								case "#createSession":
									getTestPage('/ScopeProject/admin/getCreateSessionPage');
                                    break;
                                    
								default:
									break;
							}
    				    }	
    			});
    		
    		
    	});
    
    
    // Login dialog box 
    jQuery(function() 
    	{
    	    // login dialog
	        jQuery("#login-dialog").dialog(
	        	{ 
	        		autoOpen: false,
	        	    modal: true,
	        	    buttons: 
	        	       {
			                Login: function() 
			                {
			                    var username = jQuery("#login-dialog-username").val();
			                	var password = jQuery("#login-dialog-password").val();
			                	var url      = jQuery("#login-dialog-url").val();
			                	
			                	console.log(username);
			                	console.log(password);
			                	console.log(url);
			                	
			                	// Get the url with the login credentials 
			                	getTestPage(url, 
				                		{
				                		   'userID'   : username,
				                		   'password' : password
				                		});
			                    
			                	// Close the dialog box 
			                    jQuery(this).dialog( "close" );
			                }
	                    }
        });
    });
    
    // Error dialog message
    jQuery(function() 
    { 
    	jQuery("#error-dialog").dialog(
    		{
    			autoOpen: false,
    			modal:    true,
    			buttons:
    				{
    			        Ok: function()
    			        {
    			        	// Close the dialog box 
                            jQuery(this).dialog( "close" );
    			        }
    				}
    		});
    });
    
    // Error dialog message
    jQuery(function() 
    { 
        jQuery("#stacktrace-dialog").dialog(
            {
                autoOpen: false,
                modal:    true,
                width:    600,
                buttons:
                    {
                        Ok: function()
                        {
                            // Close the dialog box 
                            jQuery(this).dialog( "close" );
                        }
                    }
            });
    });
    
   
    
    </script>
    
    <style type="text/css">
        body 
        {
            font-family: "Trebuchet MS", "Helvetica", "Arial",  "Verdana", "sans-serif";
            font-size: 80%;
        }
      
        h2.center { text-align: "center"; }
        .ui-menu { width: 200px; margin-bottom: 2em; }
        div.menu { width: 200px; background-color: white; float: left;  }
        div.main { width: 600px; height: 100%; margin-left: 20px;  background-color: white; float: left; }
        div.clear { clear: both;}
    </style>
    
</head>

<body>


    <!-- login dialog -->
    <div id="login-dialog" title="Login Required">
        <form>
            <table>
                <tr><td><label for="login-dialog-username">Username:</label></td>
                    <td><input type="text"     id="login-dialog-username"/></td>
                </tr>
                <tr>
                    <td><label for="login-dialog-password">Password:</label></td>
                    <td><input type="password" id="login-dialog-password"/></td>
                </tr>
            </table>
            <input type="hidden"   id="login-dialog-url"/>
        </form>
    </div>
    
    <!-- Error dialog -->
    <div id="error-dialog" title="Error Message">
        <p id="error-dialog-message"></p>
    </div>

    <!-- Stacktrace -->
    <div id="stacktrace-dialog" title="Stacktrace">
        <p id="stacktrace-dialog-message"></p>
    </div>
    
    <!-- Title -->
    <h2 class="center">Admin Page</h2><br />
    
    <!-- Menu area -->
	<div class="menu">
	    <ul id="menu">
	        <li>
	           <a href="#serverPage">Server</a>
	           <ul>
	               <li><a href="#serverPage">View</a></li>
	               <li><a href="#configureServerPage">Configure</a></li>
	           </ul>
	        </li>
            <li>
                <a href="#backendstorage">Storage</a>
                <ul>
                    <li><a href="#backendstorage">Configure</a></li>
                </ul>
            </li>
	        <li>
	           <a href="#createSession">Session</a>
	           <ul>
	               <li><a href="#createSession">Create</a>
	           </ul>
	        </li>
	        <li><a href="/ScopeProject/admin/logout.action" >Logout</a></li>
	    </ul> 
	</div>
        
    <!-- Main area -->
    <div class="main" id="main_div">
        Main Window
    </div>
    
    <div class="clear"></div>
</body>
</html>