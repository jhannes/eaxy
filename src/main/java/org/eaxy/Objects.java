package org.eaxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract class Objects {

    static<T> boolean equals(T a, T b) {
        return a != null ? a.equals(b) : b == null;
    }

    public static int hashCode(Object...o) {
        return Arrays.hashCode(o);
    }

    @SuppressWarnings("unchecked")
    public static<T,U> List<U> list(T[] fullList, Class<U> filteredType) {
        List<U> list = new ArrayList<U>();
        for (T o : fullList) {
            if (filteredType.isInstance(o)) list.add((U)o);
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public static<T,U> List<U> list(Iterable<T> fullList, Class<U> filteredType) {
        List<U> list = new ArrayList<U>();
        for (T o : fullList) {
            if (filteredType.isInstance(o)) list.add((U)o);
        }
        return list;
    }

    public static String validatePresent(String string, String name) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException(name + " can't be empty");
        }
        return string;
    }

}
