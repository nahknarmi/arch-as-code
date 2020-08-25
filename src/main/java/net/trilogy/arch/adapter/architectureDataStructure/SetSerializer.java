package net.trilogy.arch.adapter.architectureDataStructure;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @todo Modern Jackson does this out of box.  What is this class for?
 */
public class SetSerializer extends StdSerializer<Set> {
    public SetSerializer(Class<Set> t) {
        super(t);
    }

    @Override
    public void serialize(Set set, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (set == null) {
            gen.writeNull();
            return;
        }

        gen.writeStartArray();
        if (!set.isEmpty()) {
            // Create a SortedSet if it is not already one
            if (!SortedSet.class.isAssignableFrom(set.getClass())) {
                Object item = set.iterator().next();

                // SortedSet elements must implement the Comparable interface
                if (Comparable.class.isAssignableFrom(item.getClass())) {
                    set = new TreeSet<>(set);
                }
            }

            // Iterate through and write elements
            for (Object item : set) {
                gen.writeObject(item);
            }
        }
        gen.writeEndArray();
    }
}
