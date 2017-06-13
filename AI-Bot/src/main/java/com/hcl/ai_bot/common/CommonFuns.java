/**
 * Author: Venkateswarlu Avinash
 * Date : 30-05-2017
 * Description: Common Functionality for AI-Bot
 */

package com.hcl.ai_bot.common;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author root
 */
public class CommonFuns {
    
    public static HashMap<Integer,Object> requestHandler(HashMap<String,String> headers,String url,String method,String parameters) {
        //System.out.println("Url: " + url + "\nparameters: "+parameters+"\nMethod: "+method+"\nHeaders: "+headers);
        try
        {
            HttpURLConnection con=(HttpURLConnection) new URL(url).openConnection();
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setRequestMethod(method);
            
            for(Map.Entry<String,String> header : headers.entrySet())
            {
                con.setRequestProperty(header.getKey(), header.getValue());
            }
            
            if(parameters!=null)
            {
                con.getOutputStream().write(parameters.getBytes());
            }
            
            for(Map.Entry<String,List<String>> entry : con.getHeaderFields().entrySet())
            {
                //System.out.println(entry.getKey()+"\t:\t"+entry.getValue());
            }
            
            
            Scanner sc=null;
            if(con.getErrorStream()!=null)
            {
                String temp="";
                sc=new Scanner(con.getErrorStream());
                while(sc.hasNext())
                {
                    temp=temp+sc.nextLine();
                }
                HashMap<Integer,Object> res=new HashMap<Integer,Object>();
                res.put(con.getResponseCode(),temp);
                return res;
            }
            
            
            if(sc==null)
            {
                String response="";
                sc=new Scanner(con.getInputStream());
                while(sc.hasNext())
                {
                    response=response+sc.nextLine();
                }
                HashMap<Integer,Object> res=new HashMap<Integer,Object>();
                res.put(con.getResponseCode(),response);
                return res;
            }
            
            //This lines will never be called
            HashMap<Integer,Object> res=new HashMap<Integer,Object>();
            res.put(con.getResponseCode(),"");
            return res;
            
        }
        catch(Exception ex)
        {
            //ex.printStackTrace();
            HashMap<Integer,Object> res=new HashMap<Integer,Object>();
            res.put(10000,ex.toString());
            return res;
        }
    }
    
    static public HashMap<String, Object> parsejson(String name,Object ob)
    {
            HashMap<String, Object> result=new HashMap<>();
            
            if(ob instanceof String)
            {
                result.put(name, ob);
                
            }
            else if(ob instanceof JSONObject)
            {
                
                HashMap<String,Object> temp=new HashMap<>();
                for(String key : ((JSONObject) ob).keySet())
                {
                    temp.putAll(parsejson(key, ((JSONObject) ob).get(key)));
                 }
                result.put(name, temp);
            }
            else if (ob instanceof JSONArray)
            {
                
                JSONArray jr=(JSONArray)ob;
                for(int i=0;i<jr.length();i++)
                {
                    result.putAll(parsejson(name+"-"+i, jr.get(i)));
                }
            }
            
            return result;
    }
    
    
    static public Object penetrate(HashMap<String,Object> data,String search,String seperator)
    {
        StringTokenizer st=new StringTokenizer(search, seperator);
        ArrayList<String> sequence=new ArrayList<>();
        while(st.hasMoreTokens())
        {
            sequence.add(st.nextToken().trim());
        }
        
        Object ob=null;
        //System.out.println("\n\n******************************");
        //System.out.println("Actual data:");
        //System.out.println(data);
        //System.out.println("seraching for: "+search);
        for(int i=0;i<sequence.size();i++)
        {
            if(!data.containsKey(sequence.get(i)))
            {
                return "Not found";
            }
            if(data.get(sequence.get(i)) instanceof HashMap)
            {
                data=(HashMap<String, Object>)data.get(sequence.get(i));
                ob=data;
            }
            else
            {
                ob=data.get(sequence.get(i));
            }
        }
        return ob;
    }
    
    static public String trimHtml(String htmltext)
    {
        while(htmltext.contains("<"))
        {
            htmltext=htmltext.replace(htmltext.substring(htmltext.indexOf("<"),htmltext.indexOf(">")+1),"");
        }
        return htmltext;
    }
    
    static public void emptyFolder(File f)
    {
        if(f.exists())
        {
            for(File t : f.listFiles())
            {
                if(t.isDirectory())
                {
                    if(t.listFiles().length==0)
                    {
                        t.delete();
                    }
                    else
                    {
                        emptyFolder(f);
                        t.delete();
                    }
                }
                else if(t.isFile())
                {
                    t.delete();
                }
            }
        }
    }
}
