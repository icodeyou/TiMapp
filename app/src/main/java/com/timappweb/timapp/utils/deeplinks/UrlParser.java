package com.timappweb.timapp.utils.deeplinks;

import java.net.MalformedURLException;
import java.util.HashMap;

/**
 * Created by Stephane on 08/10/2016.
 */

public class UrlParser {

    private final String[] urlParts;
    private final String[] maskParts;
    private final HashMap<String, String> params;

    public UrlParser(String url, String mask) throws NotMatchingURLException {
        this.urlParts = url.split("/");
        this.maskParts = mask.split("/");
        this.params = new HashMap<>();

        if (this.maskParts.length != this.urlParts.length){
            throw new NotMatchingURLException("Invalid part number");
        }
        for (int i = 0; i < this.maskParts.length; i++){
            if (this.maskParts[i].startsWith(":")){
                String name = this.maskParts[i].substring(1);
                this.params.put(name, this.urlParts[i]);
            }
            else if (!this.maskParts[i].equals(this.urlParts[i])){
                throw new NotMatchingURLException("Path are not the same in position " + i + ": " + this.maskParts[i] + " != " + this.urlParts[i]);
            }
        }
    }

    public String getPart(String paramName) {
        if (this.params.containsKey(paramName)){
            return this.params.get(paramName);
        }
        else {
            return null;
        }
    }


    public class NotMatchingURLException extends MalformedURLException{

        public NotMatchingURLException(String detailMessage) {
            super(detailMessage);
        }
    }
}
