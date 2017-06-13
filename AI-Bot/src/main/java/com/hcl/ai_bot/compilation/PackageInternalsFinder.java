package com.hcl.ai_bot.compilation;

import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URI;
import java.util.HashMap;
import java.util.jar.JarInputStream;
import javax.servlet.jsp.JspWriter;
import javax.tools.JavaFileObject;

public class PackageInternalsFinder {
    private ClassLoader classLoader;
 private static final String CLASS_FILE_EXTENSION = ".class";
 
 public PackageInternalsFinder(ClassLoader classLoader) {
  this.classLoader = classLoader;
 }
 
 public List<JavaFileObject> find(JspWriter out, String packageName) throws IOException {
     out.print("<br/>package Name:" + packageName+"<br/>");
  String javaPackageName = packageName.replaceAll("\\.", "/");
 
  List<JavaFileObject> result = new ArrayList<JavaFileObject>();
 
  Enumeration<URL> urlEnumeration = classLoader.getResources(javaPackageName);
  while (urlEnumeration.hasMoreElements()) { // one URL for each jar on the classpath that has the given package
      URL packageFolderURL = urlEnumeration.nextElement();
      out.print("<br/>Find:" + packageFolderURL+"<br/>");
   
   result.addAll(listUnder(packageName, packageFolderURL));
  }
 
  return result;
 }
 
 private Collection<JavaFileObject> listUnder(String packageName, URL packageFolderURL) {
  File directory = new File(packageFolderURL.getFile());
  if (directory.isDirectory()) { // browse local .class files - useful for local execution
   return processDir(packageName, directory);
  } else { // browse a jar file
   return processJar(packageFolderURL);
  } // maybe there can be something else for more involved class loaders
 }
 
 private List<JavaFileObject> processJar(URL packageFolderURL) {
     String jaruri="";
  List<JavaFileObject> result = new ArrayList<JavaFileObject>();
  try {
   jaruri = packageFolderURL.toURI().toString().replace("!/", "/").replace("jar:file:", "");
   
      HashMap<String,Object> ob=getCrunchifyClassNamesFromJar(jaruri);
      
      ArrayList<String> cls=(ArrayList<String>) ob.get("List of Class");
      
      for(String s: cls)
      {
          try
          {
            result.add(new CustomJavaFileObject(s.lastIndexOf(".class")!=-1? s.substring(0, s.lastIndexOf(".class")) : s, new URI(packageFolderURL.toString()+(s.replace(".", "/").concat(".class").replace("/class.class", ".class")))));
          }
          catch(Exception ex){
              throw ex;
          }
          //Exception ex=new IndexOutOfBoundsException(s.lastIndexOf(".class")!=-1? s.substring(0, s.lastIndexOf(".class")) : s);
          //throw ex;
      }
   /*JarURLConnection jarConn = (JarURLConnection) packageFolderURL.openConnection();
   String rootEntryName = jarConn.getEntryName();
   int rootEnd = rootEntryName.length()+1;
 
   Enumeration<JarEntry> entryEnum = jarConn.getJarFile().entries();
   while (entryEnum.hasMoreElements()) {
    JarEntry jarEntry = entryEnum.nextElement();
    String name = jarEntry.getName();
    if (name.startsWith(rootEntryName) && name.indexOf('/', rootEnd) == -1 && name.endsWith(CLASS_FILE_EXTENSION)) {
     URI uri = URI.create(jarUri + "!/" + name);
     String binaryName = name.replaceAll("/", ".");
     binaryName = binaryName.replaceAll(CLASS_FILE_EXTENSION + "$", "");*/
 
     //result.add(new CustomJavaFileObject(binaryName, uri));
    //}
   //}
  } catch (Exception e) {
   //throw new RuntimeException("Wasn't able to open " + packageFolderURL + " as a jar file,<br/>" + e.getMessage(), e);
  }
  return result;
 }
 
 private List<JavaFileObject> processDir(String packageName, File directory) {
  List<JavaFileObject> result = new ArrayList<JavaFileObject>();
 
  File[] childFiles = directory.listFiles();
  for (File childFile : childFiles) {
   if (childFile.isFile()) {
    // We only want the .class files.
    if (childFile.getName().endsWith(CLASS_FILE_EXTENSION)) {
     String binaryName = packageName + "." + childFile.getName();
     binaryName = binaryName.replaceAll(CLASS_FILE_EXTENSION + "$", "");
 
     result.add(new CustomJavaFileObject(binaryName, childFile.toURI()));
    }
   }
  }
 
  return result;
 }
 
 public static HashMap getCrunchifyClassNamesFromJar(String crunchifyJarName) throws Exception{
        ArrayList<String> listofClasses = new ArrayList<>();
        HashMap<String, Object> crunchifyObject = new HashMap<>();
        try {
            JarInputStream crunchifyJarFile = new JarInputStream(new FileInputStream(crunchifyJarName));
            JarEntry crunchifyJar;

            while (true) {
                crunchifyJar = crunchifyJarFile.getNextJarEntry();
                if (crunchifyJar == null) {
                    break;
                }
                if ((crunchifyJar.getName().endsWith(".class"))) {
                    String className = crunchifyJar.getName().replaceAll("/", "/.");
                    String myClass = className; //.substring(0, className.lastIndexOf('.'));
                    listofClasses.add(myClass.replace("/", ""));
                }
            }
            crunchifyObject.put("Jar File Name", crunchifyJarName);
            crunchifyObject.put("List of Class", listofClasses);
        } catch (Exception e) {
            throw e;
        }
        return crunchifyObject;
    }
}
