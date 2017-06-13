package com.hcl.ai_bot.process;

import com.google.gson.Gson;
import com.sun.glass.ui.SystemClipboard;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ProcessMessage extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
           
            if(request.getParameterMap().containsKey("source") && request.getParameterMap().containsKey("sesid") && request.getParameterMap().containsKey("message"))
            {
                HttpSession ses=request.getSession(true);
                HashMap<String,String> res=new HashMap<String, String>();
                
                res.put("status", "success");
                res.put("sesid", ses.getId());
                res.put("message", new QueryForMatch().process(request.getParameter("message"), ses, request.getServletContext()).toString());
                out.print(new Gson().toJson(res));

            }
            else
            {
                HashMap<String,String> res=new HashMap<String, String>();
                res.put("status", "error");
                res.put("message", "Some thing went wrong in processing your message.");
                res.put("sesid", request.getSession(true).getId());
                out.print(new Gson().toJson(res));
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}