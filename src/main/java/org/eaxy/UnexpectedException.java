package org.eaxy;

class UnexpectedException extends RuntimeException {

    UnexpectedException(Exception e) {
        super(e);
    }

}
