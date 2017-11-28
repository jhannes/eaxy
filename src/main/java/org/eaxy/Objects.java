package org.eaxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

    @SuppressWarnings("unchecked") @Nonnull
    public static<T,U> List<U> list(Iterable<T> fullList, Class<U> filteredType) {
        List<U> list = new ArrayList<U>();
        for (T o : fullList) {
            if (filteredType.isInstance(o)) list.add((U)o);
        }
        return list;
    }

    @Nonnull
    public static String validatePresent(@Nullable String string, @Nonnull String name) {
        if (string == null || string.isEmpty()) {
            throw new IllegalArgumentException(name + " can't be empty");
        }
        return string;
    }

    @Nonnull
	public static <T> T nonnull(@Nullable T o, String name) {
		if (o == null) {
			throw new IllegalArgumentException(name + " should not be null");
		}
		return o;
	}

}
