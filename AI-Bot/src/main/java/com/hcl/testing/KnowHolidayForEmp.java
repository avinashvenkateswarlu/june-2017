/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hcl.testing;

import com.hcl.ai_bot.common.IntentProcessor;
import java.util.HashMap;
import javax.servlet.http.HttpSession;
import org.json.JSONObject;


public class KnowHolidayForEmp implements IntentProcessor{
@Override
    public KnowHolidayForEmp GetInstance() {
        return new KnowHolidayForEmp();
    }

    @Override
    public String process(HttpSession ses,HashMap<String,Object> request) {
        HashMap<String, HashMap<String,String>> contexts=(HashMap<String, HashMap<String,String>>)ses.getAttribute("context");
        return request.toString();
    }
    
    private String displayText()
    {
        return "processed";
    }
    
}
