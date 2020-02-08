package net.nahknarmi.arch.domain.c4;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.structurizr.model.Element;
import com.structurizr.model.Person;
import lombok.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@EqualsAndHashCode
@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4Path {
    private static final int COMPONENT_GROUP_NUMBER = 4;
    private static final int CONTAINER_GROUP_NUMBER = 3;
    private static final int SYSTEM_OR_PERSON_GROUP_NUMBER = 2;
    private static final String ENTITY_PREFIX = "c4://";
    private static final String PERSON_PREFIX = "@";

    private static final String regex = "(c4:\\/\\/|\\@)([\\w\\s\\-]+)\\/?([\\w\\s\\-]+)?\\/?([\\w\\s\\-]+)?";
    private static final Pattern pattern = Pattern.compile(regex);

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Matcher matcher;

    @NonNull
    private String path;

    public C4Path(String path) {
        this.path = path;
        this.matcher = matcher();
    }

    private Matcher matcher() {
        if (this.matcher == null) {
            this.matcher = pattern.matcher(this.path);
            boolean found = matcher.find();
            checkArgument(found, String.format("Path does not match expected pattern. (%s)", this.path));
        }
        return this.matcher;
    }

    public static C4Path buildPath(Element element) {
        if (element.getParent() == null) {
            String prefix = "c4://";
            if (element instanceof Person) {
                prefix = "@";
            }

            String path = prefix + element.getName().replaceAll("/", "-");
            return new C4Path(path);
        }

        @NonNull String c4Path = buildPath(element.getParent()).getPath();
        String fullPath = c4Path + "/" + element.getName().replaceAll("/", "-");
        return new C4Path(fullPath);
    }

    @JsonIgnore
    public String getName() {
        return Arrays.stream(path.split("(/|//|\\@)"))
                .reduce((first, second) -> second)
                .orElse(null);
    }

    @JsonIgnore
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

    @JsonIgnore
    public String getPersonName() {
        if (this.path.startsWith(PERSON_PREFIX)) {
            return matcher().group(SYSTEM_OR_PERSON_GROUP_NUMBER);
        }

        return null;
    }

    @JsonIgnore
    public String getSystemName() {
        if (this.path.startsWith(ENTITY_PREFIX)) {
            return matcher().group(SYSTEM_OR_PERSON_GROUP_NUMBER);
        }

        return null;
    }

    @JsonIgnore
    public Optional<String> getContainerName() {
        if (this.path.startsWith(ENTITY_PREFIX)) {
            return ofNullable(matcher().group(CONTAINER_GROUP_NUMBER));
        }

        return empty();
    }

    @JsonIgnore
    public Optional<String> getComponentName() {
        if (this.path.startsWith(ENTITY_PREFIX)) {
            return ofNullable(matcher().group(COMPONENT_GROUP_NUMBER));
        }

        return empty();
    }

    @Override
    public String toString() {
        return "C4Path{" +
                "path='" + path + '\'' +
                '}';
    }
}
