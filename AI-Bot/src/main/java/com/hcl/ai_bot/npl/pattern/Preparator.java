package com.hcl.ai_bot.npl.pattern;

import com.hcl.ai_bot.common.CommonFuns;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

/**
 *
 * @author root
 */
public final class Preparator
{
    static Preparator pre=null;
    ServletContext appContext;
    HashMap<String,HashMap<String,String>> patterns;
    
    public static Preparator getInstance()
    {
        if(pre==null)
        {
            pre=new Preparator();
        }
        
            return pre;
    }

    public Preparator() {
        patterns=new HashMap<String,HashMap<String,String>>();
    }
    
    
    public void prepare(ServletContext appContext)
    {
        if(appContext.getAttribute("patterns")!=null)
        {
            HashMap<String,HashMap<String,String>> temp=(HashMap<String,HashMap<String,String>>) appContext.getAttribute("patterns");
            
            
            String key=appContext.getInitParameter("api.ai access token").toString();
            
            HashMap<String,String> headers=new HashMap<String, String>();
            headers.put("Content-Type", "application/json");
            headers.put("Authorization", "Bearer "+key);
            String method="DELETE";
            for(String s : temp.keySet())
            {
                String url=appContext.getInitParameter("api.ai base-url").toString()+"entities/"+"pattern-"+s+"?v=20150910";
                String parameters="";
                HashMap<Integer,Object> ob2=CommonFuns.requestHandler(headers, url, method, parameters);
                appContext.log(ob2.toString());
            }
        }
        appContext.setAttribute("patterns", patterns);
        
        this.appContext=appContext;
        try
        {
            File dir=new File(appContext.getRealPath("WEB-INF/")+"pattern-source");
            if(dir.exists())
            {
                CommonFuns.emptyFolder(dir);
            }
            else
            {
                appContext.log("WEB-INF/pattern-source directory not exists");
                dir.mkdir();
            }
            
        }
        catch(Exception ex)
        {
            appContext.log(ex.getMessage(),ex);
        }
        
                if(appContext.getRealPath("WEB-INF/Pattern-Classifier.xml")==null)
                {
                    appContext.log("Pattern-Classifier file not found in WEB-INF");
                    return;
                }
                else
                {
                    File f=new File(appContext.getRealPath("WEB-INF/Pattern-Classifier.xml"));
                    if(!f.exists())
                    {
                        appContext.log("Pattern-Classifier file not found in WEB-INF");
                        return;
                    }
                }
        try
        {
            FileReader fr=new FileReader(appContext.getRealPath("WEB-INF/Pattern-Classifier.xml"));
            Scanner sc=new Scanner(fr);
            String XML_TEXT="";
            while(sc.hasNext())
            {
                XML_TEXT=XML_TEXT+sc.nextLine();
            }
            

            JSONObject xmlJSONObj = XML.toJSONObject(XML_TEXT);
            appContext.log(xmlJSONObj.toString(5));
            
            if(xmlJSONObj.has("pattern-mapping") && (xmlJSONObj.get("pattern-mapping") instanceof JSONObject))
            {
                xmlJSONObj=xmlJSONObj.getJSONObject("pattern-mapping");
                
                if(xmlJSONObj.has("pattern") && (xmlJSONObj.get("pattern") instanceof JSONObject))
                {
                    try
                    {
                        create_pattern(xmlJSONObj.getJSONObject("pattern"));
                    }
                    catch(Exception ex)
                    {
                        appContext.log(ex.getMessage(), ex);
                    }
                }
                else if(xmlJSONObj.has("pattern") && (xmlJSONObj.get("pattern") instanceof JSONArray))
                {
                    JSONArray tp=xmlJSONObj.getJSONArray("pattern");
                    for(int i=0;i<tp.length();i++)
                    {
                        try
                        {
                            create_pattern(tp.getJSONObject(i));
                        }
                        catch(Exception ex)
                        {
                            appContext.log(ex.getMessage(), ex);
                        }
                    }
                }
                else
                {
                    appContext.log("No patterns found in Pattern-Classifier.xml");
                }
            }
            else
            {
                appContext.log("<Pattern Mapping> not found in Pattern-Classifier.xml");
            }
            
            JSONArray jr=new JSONArray();
            for(String s : patterns.keySet())
            {
                JSONObject ob=new JSONObject();
                ob.put("name", "pattern-"+s);
                ob.put("isOverridable", true);
                ob.put("entries", new ArrayList<HashMap<String,String>>());
                ob.put("isEnum", false);
                ob.put("automatedExpansion", true);
                jr.put(ob);
            }
            
            String key=appContext.getInitParameter("api.ai access token").toString();
            
        HashMap<String,String> headers=new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer "+key);
        String url=appContext.getInitParameter("api.ai base-url").toString()+"entities/";
        String method="PUT";
        
        
        String parameters=jr.toString();
        HashMap<Integer,Object> ob2=CommonFuns.requestHandler(headers, url, method, parameters);
        
        appContext.log("From preparator :" + ob2.toString());
        }
        catch(Exception ex)
        {
            appContext.log(ex.getMessage(), ex);
        }
        
    }
    
    void create_pattern(JSONObject xmlJSONObj) throws Exception
    {
        HashMap<String,Object> ob=(HashMap<String,Object>) CommonFuns.parsejson("pattern", xmlJSONObj).get("pattern");
        appContext.log(ob.keySet().toString());
                    if(ob.containsKey("name"))
                    {
                        if(!Pattern.matches("[A-Za-z][A-Za-z0-9_]+", ob.get("name").toString()))
                        {
                            appContext.log("Pattern Name <"+ob.get("name")+"> is not a valid identifier");
                            return;
                        }
                        
                        if(ob.containsKey("type"))
                        {
                            if(ob.get("type").toString().equalsIgnoreCase("code") || ob.get("type").toString().equalsIgnoreCase("regexp"))
                            {
                                if(ob.get("type").toString().equalsIgnoreCase("code"))
                                {
                                    if(ob.containsKey("code"))
                                    {
                                        
                                            //To create pattern source directory if not exist
                                            File dir=new File(appContext.getRealPath("WEB-INF/")+"pattern-source");
                                            if(!dir.exists())
                                            {
                                                appContext.log("WEB-INF/pattern-source directory not found, Created Successfully");
                                            }
                                        
                                        
                                        FileWriter fw=new FileWriter(appContext.getRealPath("WEB-INF/pattern-source")+"/"+ob.get("name")+".java");
                                        String fields[]={"imports","code","function"};
                                        for(String t : fields)
                                        {
                                            if(ob.containsKey(t))
                                            {   if(t.equalsIgnoreCase("code"))
                                                {
                                                    String temp="\npublic class "+ob.get("name")+" {\npublic static boolean process(String input)\n{\n"+ob.get(t).toString()+"\nreturn true;\n}\n";
                                                    fw.write(temp);
                                                }
                                                else
                                                {
                                                    fw.write(ob.get(t).toString());
                                                }
                                            }
                                        }
                                        fw.write("\n}");
                                        fw.flush();
                                        fw.close();
                                        HashMap<String,String> pattern=new HashMap<>();
                                        pattern.put("name", ob.get("name").toString());
                                        pattern.put("code type", "custom");
                                        patterns.put(ob.get("name").toString(),pattern);
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
                                                            for(Method m: c.getDeclaredMethods())
                                                            {
                                                                if(m.getName().compareTo("process")==0 && m.getReturnType()==boolean.class)
                                                                {
                                                                    valid=true;
                                                                }
                                                            }
                                                            
                                                            if(valid)
                                                            {
                                                                HashMap<String,String> pattern=new HashMap<>();
                                                                pattern.put("name", ob.get("name").toString());
                                                                pattern.put("code type", "Classpath");
                                                                pattern.put("class", file_attr.get("class"));
                                                                patterns.put(ob.get("name").toString(),pattern);
                                                                appContext.log("Given class is valid");
                                                            }
                                                            else
                                                            {
                                                                appContext.log("Given class is Invalid");
                                                            }
                                                        }
                                                        catch(Exception ex)
                                                        {
                                                            appContext.log(file_attr.get("class")+" class is not found in the class path.");
                                                        }
                                                    }
                                                    else
                                                    {
                                                        appContext.log("Class not found in the file "+xmlJSONObj.toString(5)); 
                                                    }
                                                }
                                                else
                                                {
                                                    appContext.log("path_type is invalid in the file "+xmlJSONObj.toString(5)); 
                                                }
                                            }
                                            else
                                            {
                                                appContext.log("path_type or url attributes missing in the file "+xmlJSONObj.toString(5)); 
                                            }
                                        }
                                        catch(Exception ex)
                                        {
                                            appContext.log(ex.getMessage(),ex);
                                        }
                                    }
                                    else
                                    {
                                        appContext.log("Code && File Element is missing in "+xmlJSONObj.toString(5)); 
                                    }
                                }
                                else if(ob.get("type").toString().equalsIgnoreCase("regexp"))
                                {
                                    if(ob.containsKey("exp"))
                                    {
                                        String code="import java.util.regex.*;  \n" +
                                                    "public class "+ob.get("name")+"{  \n" +
                                                    "public static boolean process(String input){  \n" +

                                                    "return Pattern.matches(\""+ob.get("exp")+"\", input);  \n" +

                                                    "}}";
                                        //To create pattern source directory if not exist
                                            File dir=new File(appContext.getRealPath("WEB-INF/")+"pattern-source");
                                            if(!dir.exists())
                                            {
                                                appContext.log("WEB-INF/pattern-source directory not found, Created Successfully");
                                            }
                                        
                                        
                                        FileWriter fw=new FileWriter(appContext.getRealPath("WEB-INF/pattern-source")+"/"+ob.get("name")+".java");
                                        fw.write(code);
                                        fw.flush();
                                        fw.close();
                                        
                                        HashMap<String,String> pattern=new HashMap<>();
                                                                pattern.put("name", ob.get("name").toString());
                                                                pattern.put("code type", "custom");
                                                                patterns.put(ob.get("name").toString(),pattern);
                                    }
                                    else
                                    {
                                        appContext.log("<exp> tag for regular expression is missing. "+xmlJSONObj.toString(5));
                                    }
                                }
                            }
                            else
                            {
                               appContext.log("Invalid Type attribute in "+xmlJSONObj.toString(5)); 
                            }
                        }
                        else
                        {
                            appContext.log("Type attribute missing in "+xmlJSONObj.toString(5));
                        }
                    }
                    else
                    {
                        appContext.log("Name attribute missing in "+xmlJSONObj.toString(5));
                    }
    }
    
    static
    {
        pre=new Preparator();
    }
}
