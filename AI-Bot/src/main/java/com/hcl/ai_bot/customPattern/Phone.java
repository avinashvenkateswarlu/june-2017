/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hcl.ai_bot.customPattern;

import java.util.regex.Pattern;

/**
 *
 * @author root
 */
public class Phone {
    
    public static boolean process(String s)
    {
        return Pattern.matches("\\d{10}", s);
    }
}
