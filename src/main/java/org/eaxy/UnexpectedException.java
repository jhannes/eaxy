package org.eaxy;

public class UnexpectedException extends RuntimeException {

    public UnexpectedException(Exception e) {
        super(e);
    }

}
