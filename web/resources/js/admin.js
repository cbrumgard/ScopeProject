

var adminSessionsToWatch = { };

jQuery(document).ready(

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

		// Error dialog message
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


		// Error dialog message
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

function raiseLoginDialog(url)
{
	jQuery("#login-dialog-url").val(url);
	jQuery("#login-dialog").dialog("open");
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

function showSessionPanel()
{
	console.log("show session tabs");
	jQuery("#session_tabs").tabs("show");
}

function startSession(sessionID)
{
	alert("Start session "+sessionID);
	
	jQuery.ajax(
			{
				url: "/ScopeProject/admin/startSession",
				async: true,
				data: { "sessionID" : sessionID },
		
				error:  function(jqXHR, textStatus, errorThrown)
				{
					raiseErrorDialog(errorThrown);
				},
				
				success: function(dataObject, textStatus, jqXHR)
				{
					processJSONResult(dataObject);
				}
				
			}).done(function() { })
			  .fail(function(jqXHR, textStatus) { raiseErrorDialog("Request failed: " + textStatus); })
			  .always(function() { });
}

function addSessionTab(sessionName)
{
	tabTemplate = "<li><a href='#{href}'>#{label}</a> <span class='ui-icon ui-icon-close'>Remove Tab</span></li>";
	
	content = "<button type='button' onclick='startSession("+'"'+sessionName+'"'+")'>Start</button>";
	
	var label = sessionName; //tabTitle.val() || "Tab " + tabCounter;
    var id = "tabs_session_" + sessionName;
    var li = $( tabTemplate.replace( /#\{href\}/g, "#" + id ).replace( /#\{label\}/g, label ) );
    
    //var tabContentHtml = tabContent.val() || "Tab " + tabCounter + " content.";

    var sessionTabs = jQuery("#session_tabs");
    	
    console.log(sessionTabs);
    
    // Create the tab
    sessionTabs.find(".ui-tabs-nav").append(li);
    sessionTabs.append( "<div id='" + id + "'><p>" + content + "</p></div>" );
    
    
    
    
    console.log(sessionTabs);
    
    // Order a refresh on the tabs
    sessionTabs.tabs("refresh");
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
				processJSONResult(dataObject);

			}
		}
		

	}).done(function() { })
	.fail(function(jqXHR, textStatus) { raiseErrorDialog("Request failed: " + textStatus); })
	.always(function() { });
}


