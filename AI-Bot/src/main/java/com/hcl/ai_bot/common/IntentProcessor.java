/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hcl.ai_bot.common;

import java.util.HashMap;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;

public interface IntentProcessor
{
    public Object GetInstance();
    public String process(HttpSession ses,HashMap<String,Object> request);
}
