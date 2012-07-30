package org.eaxy;

public class CanNeverHappenException extends RuntimeException {

    public CanNeverHappenException(String message, Exception e) {
        super(message, e);
    }

}
