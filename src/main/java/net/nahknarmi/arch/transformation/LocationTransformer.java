package net.nahknarmi.arch.transformation;

import com.structurizr.model.Location;
import net.nahknarmi.arch.domain.c4.C4Location;

public class LocationTransformer {
    static public Location c4LocationToLocation(C4Location c4Location) {
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
