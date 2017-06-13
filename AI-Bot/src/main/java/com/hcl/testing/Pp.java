package com.hcl.testing;

import com.hcl.ai_bot.compilation.CustomClassloaderJavaFileManager;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import javax.servlet.ServletContext;
import javax.servlet.jsp.JspWriter;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import sun.tools.jar.resources.jar;

/**
 *
 * @author root
 */
public class Pp {
 
    public void Pp(ServletContext app,JspWriter out) throws Exception
    {
        
        try
        {
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
            JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
            URL u=new URL("file:///private/var/root/Desktop/repo/sample/AI-Bot/target/AI-Bot-1/WEB-INF/intent-source/HolidayFinder.java");
            //CustomJavaFileObject diagnostics=new CustomJavaFileObject("EmployeeFinder.java", u.toURI());
            StandardJavaFileManager standardJavaFileManager 
      = compiler.getStandardFileManager(diagnostics, null, null);
    final JavaFileManager fileManager
      = new CustomClassloaderJavaFileManager(this.getClass().getClassLoader(), standardJavaFileManager,out);
            
            

    if(compiler.getTask(out, fileManager, diagnostics, null, null, standardJavaFileManager.getJavaFileObjects(new File(u.toURI()))).call())
    {
        out.print("Compilation Success");
    }
    else
    {
        out.print("Compilation failure<br/>");
    }
            for (Diagnostic diagnostic : diagnostics.getDiagnostics())
            {
                       out.print(diagnostic.toString()) ;//"Error on line "+diagnostic.getLineNumber()+" in "+diagnostic.getSource().toString()+" at column: "+diagnostic.getColumnNumber());
            }
        }
        catch(Exception ex)
        {
            
            out.print(Arrays.asList(ex.getStackTrace()).toString());
        }
        catch(Error e)
        {
            out.print(e.toString());
        }
        }
}
