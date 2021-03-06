/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hcl.ai_bot.process;

import java.util.HashMap;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Web application lifecycle listener.
 *
 * @author root
 */
public class NewClientSetUp implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        HttpSession ses=se.getSession();
        ses.setAttribute("contexts", new HashMap<String, HashMap<String,String>>());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        System.gc();
    }
}
