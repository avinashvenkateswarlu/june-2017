<%@page import="com.hcl.ai_bot.common.CommonFuns"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    </head>
    <body>
        <%
            if(request.getHeader("key").compareTo(application.getInitParameter("api.ai access token"))==0)
            {
            
                com.hcl.ai_bot.npl.intent.Preparator.getInstance().prepare(application);
                out.print(application.getAttribute("intents"));
            }
            else
            {
                out.print(request.getHeader("key"));
            }
        %>
    </body>
</html>
