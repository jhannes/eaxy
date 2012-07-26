package org.eaxy;

class CanNeverHappenException extends RuntimeException {

    CanNeverHappenException(String message, Exception e) {
        super(message, e);
    }

}
