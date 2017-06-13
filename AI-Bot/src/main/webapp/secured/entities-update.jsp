<%@page import="com.hcl.ai_bot.common.CommonFuns"%>
<%@page import="org.json.*,java.io.*,java.util.*"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    </head>
    <body>
        <h1>
            
            <%
                if(request.getHeader("key").compareTo(application.getInitParameter("api.ai access token"))!=0)
                {
                    out.print("Key mismatch");
                    log("Key mismatch while trying to update entities");
                }
                if(getServletContext().getRealPath("/WEB-INF/Entity-Mapper.xml")==null)
                {
                    log("Entity-Mapper file not found in WEB-INF");
                    return;
                }
                
                FileReader fr=new FileReader(getServletContext().getRealPath("/WEB-INF/Entity-Mapper.xml"));
                Scanner sc=new Scanner(fr);
                String XML_TEXT="";
                while(sc.hasNext())
                {
                 XML_TEXT=XML_TEXT+sc.nextLine();
                }
                //System.out.println(XML_TEXT);
                
                JSONObject xmlJSONObj = XML.toJSONObject(XML_TEXT);
                
                if(xmlJSONObj.has("entities"))
                {
                    xmlJSONObj=(JSONObject) xmlJSONObj.get("entities");
                    if(xmlJSONObj.has("entity"))
                    {
                        if(xmlJSONObj.get("entity") instanceof JSONArray)
                        {
                            JSONArray entities=xmlJSONObj.getJSONArray("entity");
                            for(int i=0;i<entities.length();i++)
                            {
                                JSONObject entity=entities.getJSONObject(i);
                                if(entity.has("entries"))
                                {
                                    if(entity.getJSONObject("entries").getJSONArray("entry")!=null)
                                    {
                                        JSONArray jr=entity.getJSONObject("entries").getJSONArray("entry");
                                        for(int j=0;j<jr.length();j++)
                                        {
                                            JSONObject tp=jr.getJSONObject(j);
                                            List<Object> ele=tp.getJSONObject("synonyms").getJSONArray("value").toList();
                                            tp.put("synonyms", ele);
                                        }
                                    }
                                    entity.put("entries", entity.getJSONObject("entries").getJSONArray("entry"));
                                }
                            }
                        }
                        else if(xmlJSONObj.get("entity") instanceof JSONObject)
                        {
                                JSONObject entity=xmlJSONObj.getJSONObject("entity");
                                if(entity.has("entries"))
                                {
                                    if(entity.getJSONObject("entries").getJSONArray("entry")!=null)
                                    {
                                        JSONArray jr=entity.getJSONObject("entries").getJSONArray("entry");
                                        for(int j=0;j<jr.length();j++)
                                        {
                                            JSONObject tp=jr.getJSONObject(j);
                                            List<Object> ele=tp.getJSONObject("synonyms").getJSONArray("value").toList();
                                            tp.put("synonyms", ele);
                                        }
                                    }
                                    entity.put("entries", entity.getJSONObject("entries").getJSONArray("entry"));
                                }
                        }
                    }
                    
                    out.print(xmlJSONObj.get("entity").toString());
                    String key=application.getInitParameter("api.ai access token").toString();
            
        HashMap<String,String> headers=new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer "+key);
        String url=application.getInitParameter("api.ai base-url").toString()+"entities/";
        String method="PUT";
        
        
        String parameters=xmlJSONObj.get("entity").toString();
        HashMap<Integer,Object> ob2=CommonFuns.requestHandler(headers, url, method, parameters);
        out.print(ob2);
        log(ob2.toString());
                }
                else
                {
                    log("Entity Mapping file has error. Missing <entities>");
                }
                %>
        </h1>
    </body>
</html>
