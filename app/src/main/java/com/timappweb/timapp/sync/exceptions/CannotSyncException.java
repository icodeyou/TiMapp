package com.timappweb.timapp.sync.exceptions;

/**
 * Created by Stephane on 17/08/2016.
 */
public class CannotSyncException extends Exception {

    public int type;

    public CannotSyncException(String detailMessage, int type) {
        super(detailMessage);
        this.type = type;
    }
}
