/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hcl.ai_bot.process;

import com.hcl.ai_bot.common.CommonFuns;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.tools.ToolProvider;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author root
 */
public class QueryForMatch {

    public Object process(String query,HttpSession ses,ServletContext appContext)
    {
        String key=appContext.getInitParameter("api.ai access token").toString();
            
        HashMap<String,String> headers=new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer "+key);
        String url=appContext.getInitParameter("api.ai base-url").toString()+"query?v=20150910";
        String method="POST";
        
        JSONObject request=new JSONObject();
        request.put("query", query);
        request.put("lang", "en");
        request.put("sessionId", ses.getId());
        HashMap<String, HashMap<String,String>> contexts=(HashMap<String, HashMap<String,String>>)ses.getAttribute("context");
        JSONArray ctx=new JSONArray();
        for(String s : contexts.keySet())
        {
            JSONObject ob=new JSONObject();
            ob.put("name", s);
            ob.put("lifespan", 0);
            ctx.put(ob);
        }
        request.put("contexts", ctx);
        if(query.equalsIgnoreCase("exit"))
        {
            request.put("resetContexts", true);
        }
        else
        {
            request.put("resetContexts", false);
        }
        
        
        ArrayList<HashMap<String,String>> intents=(ArrayList<HashMap<String,String>>)appContext.getAttribute("intents");
        if(ses.getAttribute("last_intent")!=null)
        {
            final HashMap<String,String> op=(HashMap<String,String>) ses.getAttribute("last_intent");
            Predicate<HashMap<String,String>> pre= o -> o.get("name").equalsIgnoreCase(op.get("intent"));
            
            if(true)
            {
                appContext.log("\nOP has : "+op.toString()+"\nName: "+op.get("intent")+"\n"+intents.toString());
            }
            
            intents=(ArrayList<HashMap<String,String>>) intents.stream().filter(pre).collect(Collectors.toList());
            
            if(true)
            {
                appContext.log("\nName: "+op.get("intent")+"\n"+intents.toString());
            }
            
            
            
            String pattern_name=intents.get(0).get(op.get("param")).replaceFirst("@pattern-", "");
            appContext.log("\n pushing for : "+op.get("param")+" of type "+pattern_name);
            HashMap<String,String> pattern=((HashMap<String,HashMap<String,String>>) appContext.getAttribute("patterns")).get(pattern_name);
            
            appContext.log(pattern.toString());
            
            if(pattern.get("code type").equalsIgnoreCase("custom"))
            {
                String class_name=pattern.get("name");
                 Compiler.enable();
                    
                    URL u[]=new URL[1];
                    File f=new File(appContext.getRealPath("WEB-INF/pattern-source/"+class_name+".java"));
                    if(f.exists())
                    {
                        try
                        {
                            //u[0]=f.toURI().toURL().;
                            appContext.log("\npath: "+f.getAbsoluteFile().toString());
                            
                            ToolProvider.getSystemJavaCompiler().run(null, null, null, f.getAbsoluteFile().toString());
                            appContext.log("\ncompilation done");
                            
                            u[0]=new URL("file://"+f.getParent().replace(class_name+".java", "")+"/");
                            appContext.log("\n Class p[ath : "+u[0]);
                            ClassLoader cl=new URLClassLoader(u);
                            
                            Class c=cl.loadClass(class_name);
                            Object ob=c.newInstance();
                            //System.out.println(c.getDeclaredFields().length);
                            for (Field field : c.getDeclaredFields()) {
                                appContext.log(field.getName());
                            }

                             for (Method m : c.getMethods()) {
                                 if(m.getName().compareTo("process")==0)
                                 {
                                     if(((boolean)m.invoke(ob,query)))
                                     {
                                         request.put("entities", createUserEntity(appContext,"pattern-"+pattern_name,query));
                                     }
                                 }
                                 appContext.log(m.getName());
                            }
                        }
                        catch(Exception ex)
                        {
                            appContext.log(ex.getMessage(),ex);
                        }
                        catch(Error e)
                        {
                            appContext.log(e.getMessage(),e);
                        }
                    }
                    
            }
            if(pattern.get("code type").equalsIgnoreCase("Classpath"))
            {
                try
                {
                            ClassLoader cl=appContext.getClassLoader();
                            
                            Class c=cl.loadClass(pattern.get("class"));
                            Object ob=c.newInstance();
                            
                            for (Method m : c.getMethods()) {
                                 if(m.getName().compareTo("process")==0)
                                 {
                                     if(((boolean)m.invoke(ob,query)))
                                     {
                                         request.put("entities", createUserEntity(appContext,"pattern-"+pattern_name,query));
                                     }
                                 }
                                 appContext.log(m.getName());
                            }
                }
                catch(Exception ex)
                {
                    appContext.log(ex.getMessage(),ex);
                }
                catch(Error e)
                {
                    appContext.log(e.getMessage(),e);
                }
            }
            
        }
        
        String parameters=request.toString(8);
        HashMap<Integer,Object> ob2=CommonFuns.requestHandler(headers, url, method, parameters);
        if(ob2.containsKey(200))
        {
            
            if(ses.getAttribute("last_intent")!=null)
            {
                ses.removeAttribute("last_intent");
            }
            
            final JSONObject res=new JSONObject(ob2.get(200).toString());
            appContext.log(res./*getJSONObject("result").*/toString(8));
            
            
            
        //appContext.getAttribute("patterns");
        Predicate<HashMap<String,String>> pre= o-> o.get("name").equalsIgnoreCase(res.getJSONObject("result").getString("action"));
        intents=(ArrayList<HashMap<String,String>>)appContext.getAttribute("intents");
        intents=(ArrayList<HashMap<String,String>>)intents.stream().filter(pre).collect(Collectors.toList());
        
            
            JSONArray jr=res.getJSONObject("result").getJSONArray("contexts");
            for(int i=0;i<jr.length();i++)
            {
                if(jr.get(i) instanceof JSONObject)
                {
                    JSONObject temp=jr.getJSONObject(i);
                    if(temp.getString("name").contains("_dialog_params_"))
                    {
                        for(String s : temp.getJSONObject("parameters").keySet())
                        {
                            if(!s.contains(".original"))
                            {
                                HashMap<String,String> param_info=new HashMap<>();
                                param_info.put("intent", res.getJSONObject("result").getString("action"));
                                param_info.put("param", s);
                                if(temp.getJSONObject("parameters").getString(s).trim().length()<=0)
                                {
                                    ses.setAttribute("last_intent", param_info);
                                    appContext.log("\nsession variable is setted with " + param_info.toString());
                                }
                            }
                        }
                    }
                }
            }
            
            if(res.getJSONObject("result").getJSONObject("fulfillment").getString("speech").equalsIgnoreCase("success"))
            {
                appContext.log(intents.toString());
                if(intents.size()==1)
                {
                    if(intents.get(0).get("code type").equalsIgnoreCase("custom"))
                    {
                        return "This Custom coding feature through XMl has yet to be coded";
                         /*Compiler.enable();

                            URL u[]=new URL[1];
                            File f=new File(appContext.getRealPath("WEB-INF/intent-source/"+intents.get(0).get("name")+".java"));
                            if(f.exists())
                            {
                                try
                                {
                                    String s="javac "+f.getAbsoluteFile().toString();   
                                    Process pro2 = Runtime.getRuntime().exec(s);
                                    Scanner in = new Scanner(pro2.getInputStream());
                                    String output="";
                                    while(in.hasNext())
                                    {
                                        output+=in.nextLine()+"\n";
                                    }
                                    appContext.log("\nOutput: "+output);
                                    appContext.log("\npath: "+f.getAbsoluteFile().toString());

                                    ToolProvider.getSystemJavaCompiler().run(null, null, null, f.getAbsoluteFile().toString());
                                    appContext.log("\ncompilation done");

                                    u[0]=new URL("file://"+f.getParent().replace(intents.get(0).get("name")+".java", "")+"/");
                                    appContext.log("\n Classp[ath : "+u[0]);
                                    ClassLoader cl=new URLClassLoader(u);

                                    Class c=cl.loadClass(intents.get(0).get("name"));
                                    Object ob=c.newInstance();
                                    
                                     for (Method m : c.getMethods()) {
                                         if(m.getName().compareTo("process")==0)
                                         {
                                             return m.invoke(ob,ses,query);
                                         }
                                         appContext.log(m.getName());
                                    }
                                }
                                catch(Exception ex)
                                {
                                    appContext.log(ex.getMessage(),ex);
                                }
                                catch(Error e)
                                {
                                    appContext.log(e.getMessage(),e);
                                }
                            }*/
                    }
                    else if(intents.get(0).get("code type").equalsIgnoreCase("Classpath"))
                    {
                        try
                        {
                            /*
                            [{code type=Classpath, name=EmployeeFinder, pt1=@pattern-employee_id, class=com.hcl.testing.KnowHolidayForEmp}]
                            */
                                    ClassLoader cl=appContext.getClassLoader();

                                    Class c=cl.loadClass(intents.get(0).get("class"));
                                    Object ob=c.newInstance();

                                    Object args[]=new Object[2];
                                    args[0]=ses;
                                    args[1]=(HashMap<String,Object>)CommonFuns.parsejson("parameters", res.getJSONObject("result").getJSONObject("parameters")).get("parameters");
                                    
                                    for (Method m : c.getMethods()) {
                                         if(m.getName().compareTo("process")==0)
                                         {
                                             return m.invoke(ob,args);
                                         }
                                    }
                        }
                        catch(Exception ex)
                        {
                            appContext.log(ex.getMessage(),ex);
                        }
                        catch(Error e)
                        {
                            appContext.log(e.getMessage(),e);
                        }
                    }
                }
                appContext.log("'ngoing to call:\n "+intents.toString());
                return res.getJSONObject("result").getJSONObject("fulfillment").getString("speech");
            }
            else
            {
                return res.getJSONObject("result").getJSONObject("fulfillment").getString("speech");
            }
        }
        else
        {
            return "Error Occured while processing your query.";
        }
    }
    
    private JSONArray createUserEntity(ServletContext appContext,String pattern_name,String value)
    {
        ArrayList<HashMap<String,Object>> entries=new ArrayList<>();
        
        ArrayList<String> sy=new ArrayList<>();
        sy.add(value);
        HashMap<String,Object> synonym=new HashMap<>();
        synonym.put("value", value);
        synonym.put("synonyms", sy);
        entries.add(synonym);
        
        ArrayList<HashMap<String,Object>> full=new ArrayList<>();
        HashMap<String,Object> full_par=new HashMap<>();
        full_par.put("name", pattern_name);
        full_par.put("entries", entries);
        full.add(full_par);
        
        
        appContext.log("\nArray for use enttitu:\n "+new JSONArray(full).toString(8));
        return new JSONArray(full);
    }
}
