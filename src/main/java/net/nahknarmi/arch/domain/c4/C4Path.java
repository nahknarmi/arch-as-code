package net.nahknarmi.arch.domain.c4;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Optional.empty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4Path {
    private static final int COMPONENT_GROUP_NUMBER = 4;
    private static final int CONTAINER_GROUP_NUMBER = 3;
    private static final int SYSTEM_OR_PERSON_GROUP_NUMBER = 2;
    private static final String URI_PREFIX = "c4://";
    private static final String PERSON_PREFIX = "@";
    @NonNull
    private String path;

    private static final String regex = "(c4:\\/\\/|\\@)([\\w\\s\\-]+)\\/?([\\w\\s\\-]+)?\\/?([\\w\\s\\-]+)?";
    private final Pattern pattern = Pattern.compile(regex);
    private Matcher matcher;

    public C4Path(String path) {
        this.path = path;
        this.matcher = matcher();
    }

    private Matcher matcher() {
        if (this.matcher == null) {
            this.matcher = pattern.matcher(this.path);
            boolean found = matcher.find();
            checkArgument(found, "Path does not match expected pattern.");
        }
        return this.matcher;
    }

    public String getName() {
        return Arrays.stream(path.split("(/|//|\\@)"))
                .reduce((first, second) -> second)
                .orElse(null);
    }

    public C4Type getType() {
        if (this.getPersonName() != null) {
            return C4Type.person;
        }

        if (this.getComponentName().isPresent()) {
            return C4Type.component;
        }

        if (this.getContainerName().isPresent()) {
            return C4Type.container;
        }

        if (this.getSystemName() != null) {
            return C4Type.system;
        }

        return null;
    }

    public String getPersonName() {
        if (this.path.startsWith(PERSON_PREFIX)) {
            return matcher().group(SYSTEM_OR_PERSON_GROUP_NUMBER);
        }

        return null;
    }

    public String getSystemName() {
        if (this.path.startsWith(URI_PREFIX)) {
            return matcher().group(SYSTEM_OR_PERSON_GROUP_NUMBER);
        }

        return null;
    }

    public Optional<String> getContainerName() {
        if (this.path.startsWith(URI_PREFIX)) {
            return Optional.ofNullable(matcher().group(CONTAINER_GROUP_NUMBER));
        }

        return empty();
    }

    public Optional<String> getComponentName() {
        if (this.path.startsWith(URI_PREFIX)) {
            return Optional.ofNullable(matcher().group(COMPONENT_GROUP_NUMBER));
        }

        return empty();
    }
}
