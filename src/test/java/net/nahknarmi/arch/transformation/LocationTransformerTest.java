package net.nahknarmi.arch.transformation;

import com.structurizr.model.Location;
import net.nahknarmi.arch.domain.c4.C4Location;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class LocationTransformerTest {

    @Test
    public void internal_c4LocationToLocation() {
        Location actual = LocationTransformer.c4LocationToLocation(C4Location.INTERNAL);
        Location expected = Location.Internal;

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void external_c4LocationToLocation() {
        Location actual = LocationTransformer.c4LocationToLocation(C4Location.EXTERNAL);
        Location expected = Location.External;

        assertThat(actual, equalTo(expected));
    }

    @Test
    public void unspecified_c4LocationToLocation() {
        Location actual = LocationTransformer.c4LocationToLocation(null);
        Location expected = Location.Unspecified;

        assertThat(actual, equalTo(expected));
    }
}
