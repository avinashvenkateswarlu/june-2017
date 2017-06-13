/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hcl.ai_bot.npl.intent;

import com.hcl.ai_bot.common.CommonFuns;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.ServletContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

/**
 *
 * @author root
 */
public class Preparator {

    static com.hcl.ai_bot.npl.intent.Preparator pre=null;
    
    ServletContext appContext;
    
    public static com.hcl.ai_bot.npl.intent.Preparator getInstance()
    {
        if(pre==null)
        {
            pre=new com.hcl.ai_bot.npl.intent.Preparator();
        }
        
            return pre;
    }

    public Preparator() {
    }
    
    public void prepare(ServletContext appContext)
    {
        this.appContext=appContext;
        ((ArrayList<HashMap<String,String>>)appContext.getAttribute("intents")).clear();
        
            delCurIntentsServer();

        this.appContext=appContext;
        
        //empty WEB-INF/intent-source to put the custom code embded in xml
        try
        {
            File dir=new File(appContext.getRealPath("WEB-INF/")+"intent-source");
            if(dir.exists())
            {
                CommonFuns.emptyFolder(dir);
                appContext.log(appContext.getRealPath("WEB-INF/intent-source"));
            }
            else
            {
                appContext.log("WEB-INF/intent-source directory not exists");
                dir.mkdir();
                appContext.log(appContext.getRealPath("WEB-INF/intent-source"));
            }
            
        }
        catch(Exception ex)
        {
            appContext.log(ex.getMessage(),ex);
        }
        //End of empty previous sources
        
        
                
                if(appContext.getRealPath("WEB-INF/Intent-Mapper.xml")==null)
                {
                    appContext.log("Intent-Mapper file not found in WEB-INF");
                    return;
                }
                else
                {
                    File f=new File(appContext.getRealPath("WEB-INF/Intent-Mapper.xml"));
                    if(!f.exists())
                    {
                        appContext.log("Intent-Mapper file not found in WEB-INF");
                        return;
                    }
                }
                
        
            appContext.log("Intent-Mapper file found in WEB-INF");
            
            try
            {
                FileReader fr=new FileReader(appContext.getRealPath("WEB-INF/Intent-Mapper.xml"));
                Scanner sc=new Scanner(fr);
                String XML_TEXT="";
                while(sc.hasNext())
                {
                    XML_TEXT=XML_TEXT+sc.nextLine();
                }


                JSONObject xmlJSONObj = XML.toJSONObject(XML_TEXT);
                appContext.log(xmlJSONObj.toString(5));
                
                if(xmlJSONObj.has("Intent-Mapping") && (xmlJSONObj.get("Intent-Mapping") instanceof JSONObject))
                {
                    xmlJSONObj=xmlJSONObj.getJSONObject("Intent-Mapping");
                    if(xmlJSONObj.has("Intent"))
                    {
                        if(xmlJSONObj.get("Intent") instanceof JSONObject)
                        {
                            createIntent(xmlJSONObj.getJSONObject("Intent"));
                        }
                        else if(xmlJSONObj.get("Intent") instanceof JSONArray)
                        {
                            JSONArray jr=xmlJSONObj.getJSONArray("Intent");
                            
                            for(int i=0;i<jr.length();i++)
                            {
                                if(jr.get(i) instanceof JSONObject)
                                {
                                    createIntent(jr.getJSONObject(i));
                                }
                                else
                                {
                                    appContext.log("\nelse called");
                                }
                            }
                        }
                    }
                    else
                    {
                        appContext.log("<Intent> must be the child tag for Intent-Mapping tag in Intent-Mapping.xml in WEB-INF");
                    }
                }
                else
                {
                    appContext.log("Intent-Mapping tag must be the parent tag for all the tags in Intent-Mapping.xml in WEB-INF");
                }
            }
             catch(Exception ex)
             {
                 appContext.log(ex.getMessage(), ex);
             }
    }
    
    
    private void delCurIntentsServer()
    {
        appContext.log("inside child");
        final String key=appContext.getInitParameter("api.ai access token");
            
        final HashMap<String,String> headers=new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer "+key);
        String url=appContext.getInitParameter("api.ai base-url")+"intents?v=20150910";
        String method="GET";
        
        
        String parameters=null;
        HashMap<Integer,Object> ob2=CommonFuns.requestHandler(headers, url, method, parameters);
        if(ob2.containsKey(200))
        {
            Predicate<Object> pre=ab ->
            {
                HashMap<String,Object> ob=(HashMap<String, Object>)ab;
                return (!(ob.get("name").toString().equalsIgnoreCase("Default Fallback Intent") || ob.get("name").toString().equalsIgnoreCase("Default Welcome Intent")));
            };
            
            List<Object> input=Arrays.asList((new JSONArray(ob2.get(200).toString()).toList()).stream().filter(pre).toArray());
            
            input.forEach(tp->{
                HashMap<String,Object> jo=(HashMap<String, Object>)tp;
                String url2=appContext.getInitParameter("api.ai base-url")+"intents/"+jo.get("id")+"?v=20150910";
                appContext.log("\n"+CommonFuns.requestHandler(headers, url2, "DELETE", null).toString());
            });
        }
    }
    
    private void createIntent(JSONObject intent)
    {
        HashMap<String, String> intent_info=new HashMap<>();
        
        HashMap<String,Object> ob=CommonFuns.parsejson("intent", intent);
        ob=(HashMap<String,Object>) ob.get("intent");
        if(ob.containsKey("name"))
        {
            if(ob.containsKey("process"))
            {
                if(intent.get("process") instanceof JSONObject)
                {
                    try
                    {
                        HashMap<String,Object> ob_temp=(HashMap<String,Object>) ob.get("process");
                        ob_temp.put("name", ob.get("name"));
                        processCreator(ob_temp);
                    }
                    catch(Exception ex)
                    {
                        appContext.log(ex.getMessage(),ex);
                    }
                    
                }
                else if(intent.get("process") instanceof JSONArray)
                {
                    //Yet to be coded
                }
                else
                {
                    appContext.log("Error in syntax of Process Tag for intent "+ob.get("name")+" in Intent-Mapping.xml in WEB-INF");    
                    return;
                }
            }
            else
            {
                appContext.log("Process Tag is missing for intent "+ob.get("name")+" in Intent-Mapping.xml in WEB-INF");
                return;
            }
            
            ArrayList<String> contexts=new ArrayList<>();
            if(ob.containsKey("Context-In"))
            {
                if(intent.get("Context-In") instanceof JSONObject)
                {
                    if(intent.getJSONObject("Context-In").has("name"))
                    {
                        contexts.add(intent.getJSONObject("Context-In").getString("name"));
                    }
                }
                else if(intent.get("Context-In") instanceof String)
                {
                    contexts.add(intent.getString("Context-In"));
                }
                else if(intent.get("Context-In") instanceof JSONArray)
                {
                    JSONArray jr=intent.getJSONArray("Context-In");
                    for(int i=0;i<jr.length();i++)
                    {
                        if(jr.get(i) instanceof JSONObject)
                        {
                            if(jr.getJSONObject(i).has("name"))
                            {
                                contexts.add(jr.getJSONObject(i).getString("name"));
                            }
                        }
                        else if(jr.get(i) instanceof String)
                        {
                            contexts.add(jr.getString(i));
                        }
                    }
                }
            }
            
            
            ArrayList<HashMap<String,String>> prompts=new ArrayList<>();
            if(intent.has("prompts"))
            {
                if(intent.get("prompts") instanceof JSONObject)
                {
                    prompts=getPrompts(intent.getJSONObject("prompts"));
                }
                else if(intent.get("prompts") instanceof JSONArray)
                {
                    prompts=getPrompts(intent.getJSONArray("prompts").getJSONObject(0));
                }
            }
            appContext.log("\nPrompts: \n"+prompts.toString());
            if(intent.has("User-Says"))
            {
                ArrayList<String> sentenses=new ArrayList<>();
                
                if(intent.get("User-Says") instanceof String)
                {
                    sentenses.add(intent.getString("User-Says"));
                }
                else if(intent.get("User-Says") instanceof JSONArray)
                {
                    JSONArray jr=intent.getJSONArray("User-Says");
                    for(int i=0;i<jr.length();i++)
                    {
                        if(jr.get(i) instanceof String)
                        {
                            sentenses.add(jr.getString(i));
                        }
                    }
                }
                
                int priority=500000;
                if(intent.has("priority"))
                {
                    if(intent.get("priority") instanceof Integer)
                    {
                        priority=intent.getInt("priority");
                    }
                }
                
                String key=appContext.getInitParameter("api.ai access token").toString();
        
        final ArrayList<HashMap<String,String>> dt=(ArrayList<HashMap<String,String>>)appContext.getAttribute("intents");
        final String in_name=ob.get("name")+"";
        Predicate<HashMap<String,String>> pre= ku -> ku.get("name").equalsIgnoreCase(in_name);
        if(dt.stream().anyMatch(pre))
        {
            HashMap<String,String> headers=new HashMap<String, String>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer "+key);
            String url=appContext.getInitParameter("api.ai base-url").toString()+"intents?v=20150910";
            String method="POST";


            ArrayList<Object> res=prepareJsonForIntent(ob.get("name").toString(), contexts, sentenses,priority,prompts);
            String parameters=((JSONObject)res.get(1)).toString();
            HashMap<Integer,Object> ob2=CommonFuns.requestHandler(headers, url, method, parameters);
            
            if(!ob2.keySet().contains(200))
            {
                dt.removeIf(pre);
            }
            else
            {
                
                for(HashMap<String,String> t : (ArrayList<HashMap<String,String>>)appContext.getAttribute("intents"))
                {
                    if(t.get("name").equalsIgnoreCase(in_name))
                    {
                        for(HashMap<String,Object> tor : (ArrayList<HashMap<String,Object>>)res.get(0))
                        {
                            t.put(tor.get("name").toString(), tor.get("dataType").toString());
                        }
                    }
                }
            }
        }
            }
            else
            {
                appContext.log("User-Says tag is missing for intent "+ob.get("name")+" in Intent-Mapping.xml in WEB-INF");
                return;
            }
            
        }
        else
        {
            appContext.log("Name attribute is missing for a intent in Intent-Mapping.xml in WEB-INF");
        }
    }
    
    private ArrayList<Object> prepareJsonForIntent(String name,ArrayList<String> context,ArrayList<String> sentences,int priority,ArrayList<HashMap<String,String>> prompts)
    {
        ArrayList<Object> result=new ArrayList<>();
        
        appContext.log("\nName: "+name+"\n"
                + "context: "+context+"\n"
                        + "Sentences: "+sentences);
        JSONObject intent=new JSONObject();
        intent.put("name", name);
        intent.put("auto", true);
        intent.put("contexts", context);
        
        
        ArrayList<String> st=new ArrayList<>();
        sentences.stream().forEach(t -> {
            st.add(t.replace("(@entity#", "@").replace(")", "").replace("(@pattern#", "@pattern-"));
            });
        
        intent.put("templates", st);
        ArrayList<HashMap<String,Object>> parameters=new ArrayList<>();
        
        JSONArray usersays=new JSONArray();
        for(String s : sentences)
        {
                Pattern p=Pattern.compile("(@entity#\\w+:\\w+)");
                Matcher m=p.matcher(s);
                while(m.find())
                {
                    HashMap<String,Object> par_details=new HashMap<>();
                    String exp[]=m.group().replace("@entity#", "").split(":");

                    Predicate<HashMap<String,Object>> pre= e -> e.get("name").toString().equalsIgnoreCase(exp[1]);
                    Predicate<HashMap<String,String>> pre2= e -> e.containsKey(exp[1]);
                    
                    ArrayList<String> req_prompts=new ArrayList<>();
                    
                    for(HashMap<String,String> ko : prompts.stream().filter(pre2).collect(Collectors.toList()))
                    {
                        req_prompts.add(ko.get(exp[1]));
                    }
                    
                    if(parameters.stream().noneMatch(pre))
                    {
                        par_details.put("dataType", "@"+exp[0]);
                        par_details.put("name", exp[1]);
                        par_details.put("value", "$"+exp[1]);
                        par_details.put("required", Boolean.TRUE);
                        par_details.put("prompts", req_prompts);
                        parameters.add(par_details);
                    }
                }

                p=Pattern.compile("(@pattern#\\w+:\\w+)");
                m=p.matcher(s);
                while(m.find())
                {
                    HashMap<String,Object> par_details=new HashMap<>();
                    String exp[]=m.group().replace("@pattern#", "").split(":");

                    Predicate<HashMap<String,Object>> pre= e -> e.get("name").toString().equalsIgnoreCase(exp[1]);
                    Predicate<HashMap<String,String>> pre2= e -> e.containsKey(exp[1]);
                    
                    ArrayList<String> req_prompts=new ArrayList<>();
                    
                    for(HashMap<String,String> ko : prompts.stream().filter(pre2).collect(Collectors.toList()))
                    {
                        req_prompts.add(ko.get(exp[1]));
                    }
                    
                    if(parameters.stream().noneMatch(pre))
                    {
                        par_details.put("dataType", "@"+"pattern-"+exp[0]);
                        par_details.put("name", exp[1]);
                        par_details.put("value", "$"+exp[1]);
                        par_details.put("required", Boolean.TRUE);
                        par_details.put("prompts", req_prompts);
                        parameters.add(par_details);
                    }
                }
            
            
            JSONObject ob=new JSONObject();
            ob.put("isTemplate", true);
            ob.put("count", 0);
            ob.put("data", new JSONArray().put(new JSONObject().put("text", s.replace("(@entity#", "@").replace("(@pattern#", "@pattern-").replace(")",""))));
            usersays.put(ob);
        }
        
        JSONArray responses=new JSONArray();
        JSONObject response=new JSONObject();
        response.put("resetContexts", false);
        response.put("action", name);
        response.put("parameters", new JSONArray(parameters));
        response.put("speech", "success");
        responses.put(response);
        intent.put("responses", responses);
        intent.put("userSays", usersays);
        intent.put("priority", priority);
        
        Predicate<HashMap<String,Object>> pre =t -> {
            
            return t.get("dataType").toString().startsWith("@pattern-");
                    };
        
        if(parameters.size()>=1)
        {
            appContext.log("\nparameters is"+parameters.get(parameters.size()-1).get("dataType").getClass());
        }
        
        result.add(parameters.stream().filter(pre).collect(Collectors.toList()));
        
        appContext.log(intent.toString(8));
        result.add(intent);
        appContext.log("\nreturning as :" + result);
        return result;
    }
    
    public Object processCreator(HashMap<String,Object> ob) throws Exception
    {
        if(ob.containsKey("code"))
        {
            //To create pattern source directory if not exist
            File dir=new File(appContext.getRealPath("WEB-INF/")+"intent-source");
            if(!dir.exists())
            {
                appContext.log("WEB-INF/intent-source directory not found, Created Successfully");
            }
            
            FileWriter fw=new FileWriter(appContext.getRealPath("WEB-INF/intent-source")+"/"+ob.get("name")+".java");
            String fields[]={"imports","logic","functions"};
            
            
            for(String t : fields)
            {
                if(((HashMap<String,Object>)ob.get("code")).containsKey(t))
                {   
                    if(t.equalsIgnoreCase("logic"))
                    {
                        String temp="\npublic class "+ob.get("name")+" implements IntentProcessor\n{\n" +
"@Override\n" +
"    public "+ob.get("name")+" GetInstance() {\n" +
"        return new "+ob.get("name")+"();\n" +
"    }\n"+
"@Override\n" +
"    public String process(HttpSession ses,JSONObject request) {\n" +
((HashMap<String,Object>)ob.get("code")).get(t).toString()+"\n" +
"    }\n";
                        fw.write(temp);
                    }
                    else
                    {
                        fw.write(((HashMap<String,Object>)ob.get("code")).get(t).toString());
                    }
                }
                else
                {
                    appContext.log("<t> is not found in <code> of intent "+ob.get("name"));
                }
            }
            
            fw.write("\n}");
            fw.flush();
            fw.close();
            
            HashMap<String,String> config=new HashMap<>();
            config.put("name", ob.get("name").toString());
            config.put("code type", "custom");
            ((ArrayList<HashMap<String,String>>)appContext.getAttribute("intents")).add(config);
        }
        else if(ob.containsKey("file"))
        {
            try
            {
                HashMap<String,String> file_attr=(HashMap<String,String>)ob.get("file");
                if(file_attr.containsKey("path-type") && file_attr.containsKey("class"))
                {
                    if(file_attr.get("path-type").equalsIgnoreCase("class"))
                    {
                        if(file_attr.get("class")!=null)
                        {
                            appContext.log("class match");
                            try
                            {
                                ClassLoader cl=appContext.getClassLoader();
                                Class c=cl.loadClass(file_attr.get("class"));
                                boolean valid=false;
                                for(Class m: c.getInterfaces())
                                {
                                    if(m.toString().compareTo("interface com.hcl.ai_bot.common.IntentProcessor")==0 && (!Modifier.isAbstract(c.getModifiers())))
                                    {
                                        valid=true;
                                    }
                                }
                                                            
                                if(valid)
                                {
                                    appContext.log(c.getName()+" class is valid");
                                    HashMap<String,String> config=new HashMap<>();
                                    config.put("name", ob.get("name").toString());
                                    config.put("code type", "Classpath");
                                    config.put("class", file_attr.get("class"));
                                    
                                    ((ArrayList<HashMap<String,String>>)appContext.getAttribute("intents")).add(config);
                                }
                                else
                                {
                                    appContext.log(c.getName()+" class is Invalid");
                                }
                            }
                            catch(Exception ex)
                            {
                                appContext.log(file_attr.get("class")+" class is not found in the class path.");
                            }
                        }
                        else
                        {
                            appContext.log("Class not found in the file "+ob.toString()); 
                        }
                    }
                    else
                    {
                        appContext.log("path_type is invalid in the file "+ob.toString()); 
                    }
                }
                else
                {
                    appContext.log("path_type or url attributes missing in the file "+ob.toString()); 
                }
           }
           catch(Exception ex)
           {
                appContext.log(ex.getMessage(),ex);
           }
        }
        else
        {
            appContext.log("Code && File Element is missing in "+ob.toString()); 
        }
        return null;
    }
    
    private ArrayList<HashMap<String,String>> getPrompts(final JSONObject ob)
    {
        ArrayList<HashMap<String,String>> prompts=new ArrayList<>();
        
        ob.keySet().stream().forEach(p ->{
            if(ob.get(p) instanceof JSONArray)
            {
                JSONArray jr=ob.getJSONArray(p);
                
                for(int i=0;i<jr.length();i++)
                {
                    if(jr.get(i) instanceof String)
                    {
                        HashMap<String,String> temp=new HashMap<>();
                        temp.put(p, jr.getString(i));
                        prompts.add(temp);
                    }
                }
                
            }
            else if(ob.get(p) instanceof String)
            {
                HashMap<String,String> temp=new HashMap<>();
                temp.put(p,ob.getString(p));
                prompts.add(temp);
            }
                                        });
        return prompts;
    }
}