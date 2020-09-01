package net.trilogy.arch.domain.c4;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.structurizr.model.Element;
import com.structurizr.model.Person;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static net.trilogy.arch.domain.c4.C4Type.COMPONENT;
import static net.trilogy.arch.domain.c4.C4Type.CONTAINER;
import static net.trilogy.arch.domain.c4.C4Type.PERSON;
import static net.trilogy.arch.domain.c4.C4Type.SYSTEM;

@EqualsAndHashCode
@Data
@AllArgsConstructor
@NoArgsConstructor
public class C4Path {
    private static final int COMPONENT_GROUP_NUMBER = 8;
    private static final int CONTAINER_GROUP_NUMBER = 5;
    private static final int SYSTEM_OR_PERSON_GROUP_NUMBER = 2;
    private static final String ENTITY_PREFIX = "c4://";
    private static final String PERSON_PREFIX = "@";

    private static final String regex = "(c4:\\/\\/|\\@)(([\\w\\s\\-\\.\\-]+(\\\\\\/[\\w\\s\\-\\.\\-])*)+)\\/?(([\\w\\s\\-\\.\\-]+(\\\\\\/[\\w\\s\\-\\.\\-])*)+)?\\/?(([\\w\\s\\-\\.\\-]+(\\\\\\/[\\w\\s\\-\\.\\-])*)+)?";

    private static final Pattern pattern = Pattern.compile(regex);

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Getter(AccessLevel.PRIVATE)
    private Matcher matcher;

    @NonNull
    private String path;
    private String id;

    C4Path(String path) {
        this.path = path;
        this.matcher = matcher();
    }

    public static C4Path path(String path) {
        Matcher matcher = pattern.matcher(path);
        checkArgument(matcher.matches(), String.format("Path does not match expected pattern. (%s)", path));
        return new C4Path(path);
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

            String path = prefix + element.getName().replaceAll("/", "\\\\/");
            return new C4Path(path);
        }

        @NonNull String c4Path = buildPath(element.getParent()).getPath();
        String fullPath = c4Path + "/" + element.getName().replaceAll("/", "\\\\/");
        return new C4Path(fullPath);
    }

    public String name() {
        String result = null;
        switch (type()) {
            case PERSON:
                result = personName();
                break;

            case SYSTEM:
                result = systemName();
                break;

            case CONTAINER:
                result = containerName().get();
                break;

            case COMPONENT:
                result = componentName().get();
                break;
        }
        return result;
    }

    public C4Type type() {
        if (this.personName() != null) {
            return PERSON;
        }

        if (this.componentName().isPresent()) {
            return COMPONENT;
        }

        if (this.containerName().isPresent()) {
            return CONTAINER;
        }

        if (this.systemName() != null) {
            return SYSTEM;
        }

        return null;
    }

    public String personName() {
        if (this.path.startsWith(PERSON_PREFIX)) {
            return matcher().group(SYSTEM_OR_PERSON_GROUP_NUMBER).replaceAll("\\\\/", "/");
        }

        return null;
    }

    public String systemName() {
        if (this.path.startsWith(ENTITY_PREFIX)) {
            return matcher().group(SYSTEM_OR_PERSON_GROUP_NUMBER).replaceAll("\\\\/", "/");
        }

        return null;
    }

    public Optional<String> containerName() {
        if (this.path.startsWith(ENTITY_PREFIX)) {
            return ofNullable(matcher().group(CONTAINER_GROUP_NUMBER)).map(name -> name.replaceAll("\\\\/", "/"));
        }

        return empty();
    }

    public Optional<String> componentName() {
        if (this.path.startsWith(ENTITY_PREFIX)) {
            return ofNullable(matcher().group(COMPONENT_GROUP_NUMBER)).map(name -> name.replaceAll("\\\\/", "/"));
        }

        return empty();
    }

    public C4Path containerPath() {
        if (containerName().isEmpty()) {
            throw new IllegalStateException("Container path does not exist on this path - " + this.getPath());
        }
        return new C4Path(systemPath().getPath() + "/" + containerName().get().replaceAll("/", "\\\\/"));
    }

    public C4Path componentPath() {
        if (componentName().isEmpty()) {
            throw new IllegalStateException("Component path does not exist on this path - " + this.getPath());
        }
        return new C4Path(containerPath().getPath() + "/" + componentName().get().replaceAll("/", "\\\\/"));
    }

    public C4Path systemPath() {
        if (systemName() == null) {
            throw new IllegalStateException("Accessing system path on non-system path - " + getPath());
        }
        return new C4Path("c4://" + systemName().replaceAll("/", "\\\\/"));
    }

    public C4Path personPath() {
        if (personName() == null) {
            throw new IllegalStateException("Accessing person path on non-person path - " + getPath());
        }
        return new C4Path("@" + personName());
    }

    @Override
    public String toString() {
        return "C4Path{" +
                "path='" + path + '\'' +
                '}';
    }
}
