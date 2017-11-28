package org.eaxy;

import java.io.Reader;
import java.net.URL;
import java.util.List;

import javax.annotation.Nonnull;

public interface ElementQuery {

	@Nonnull
    ElementSet search(@Nonnull ElementSet elements);

    boolean matches(List<Element> path, int position);

    @Nonnull
    Iterable<Element> iterate(@Nonnull Reader reader);

    @Nonnull
    Iterable<Element> iterate(@Nonnull URL url);


}
