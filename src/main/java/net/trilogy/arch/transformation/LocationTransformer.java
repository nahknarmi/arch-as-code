package net.trilogy.arch.transformation;

import com.structurizr.model.Location;
import net.trilogy.arch.domain.c4.C4Location;

public class LocationTransformer {
    public static Location c4LocationToLocation(C4Location c4Location) {
        if (c4Location == null) {
            return Location.Unspecified;
        } else {
            switch (c4Location) {
                case INTERNAL:
                    return Location.Internal;
                case EXTERNAL:
                    return Location.External;
                case UNSPECIFIED:
                    return Location.Unspecified;
                default:
                    throw new IllegalStateException("Unsupported Location type: " + c4Location);
            }
        }
    }
}
