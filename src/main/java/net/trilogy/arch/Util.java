package net.trilogy.arch;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.NoSuchElementException;

/** @todo What could be used from a library instead of here? */
@UtilityClass
public class Util {
    public static <T> T first(final Collection<T> c) {
        final var it = c.iterator();
        if (!it.hasNext())
            throw new NoSuchElementException();
        return it.next();
    }

    public static JsonNode first(final JsonNode c) {
        final var it = c.iterator();
        if (!it.hasNext())
            throw new NoSuchElementException();
        return it.next();
    }
}
