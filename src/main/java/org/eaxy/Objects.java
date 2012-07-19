package org.eaxy;

import java.util.Arrays;

abstract class Objects {

    static<T> boolean equals(T a, T b) {
        return a != null ? a.equals(b) : b == null;
    }

    public static int hashCode(Object...o) {
        return Arrays.hashCode(o);
    }

}
