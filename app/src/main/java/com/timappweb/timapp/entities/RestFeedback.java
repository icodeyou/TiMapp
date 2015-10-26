package com.timappweb.timapp.entities;

import java.util.HashMap;

/**
 * Created by stephane on 8/21/2015.
 */
public class RestFeedback {

    public HashMap<String, String> data = null;
    public String message = "";
    public Integer returnCode = -1;
    public boolean success = false;

    public String toString(){
        return "ServerObject[Success: " + success + "; message=" + message + "; Data="+this.data+"]";
    }


}
