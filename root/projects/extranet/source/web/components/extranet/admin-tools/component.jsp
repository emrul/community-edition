<%
	// safety check
	org.alfresco.connector.User user = org.alfresco.web.site.RequestUtil.getRequestContext(request).getUser();
	if(user == null || !user.isAdmin())
	{
		out.println("Access denied");
		return;
	}
%>
<%
	// base path
	String path = "/components/extranet/admin-tools/";
	
	// place to which we want to dispatch	
	String dispatchTo = (String) request.getParameter("dispatchTo");
	if(dispatchTo == null)
	{
%>
<a href="?p=admin-tools&dispatchTo=admin-entities">Entities</a>
<br/>
<a href="?p=admin-tools&dispatchTo=admin-bulk-invite-user">Bulk Invite User</a>
<br/>
<a href="?p=admin-tools&dispatchTo=admin-invite-user">Invite User</a>
<br/>
<a href="?p=admin-tools&dispatchTo=admin-show-invited-users">Show Invited Users</a>
<br/>
<%
		return;
		
	}
	else
	{
		path += dispatchTo + ".jsp";	
	
		// do the dispatch
		request.getRequestDispatcher(path).include(request, response);
	}
%>