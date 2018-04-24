package com.eduid.EduIdApp.controller;

/**
 * Created by Yann Cuttaz on 05.04.18.
 */

public class Config {

    public static final boolean debugOnConsole = true;


    public static final void debug(Object obj){
        if(debugOnConsole){
            System.out.println(obj);
        }
    }

    public static final void debug(String text){
        if(debugOnConsole){
            System.out.println(text);
        }
    }

}
