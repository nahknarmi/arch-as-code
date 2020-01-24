package net.nahknarmi.arch.domain.c4;

import lombok.Data;
import lombok.NonNull;

import java.util.Arrays;

@Data
public class C4Path {
    @NonNull
    private String path;
    private String systemName;
    private String containerName;
    private String componentName;

    public static final C4Path NONE = new C4Path("c4://");

    C4Path(String path) {
        this.path = path;
        String[] splits = path.split("/", 3);

        switch (splits.length) {
            case 2:
                this.systemName = splits[0];
                this.containerName = splits[1];
                break;
            case 3:
                this.systemName = splits[0];
                this.containerName = splits[1];
                this.componentName = splits[2];
                break;
        }
    }

    public String getName() {
        return Arrays.stream(path.split("//"))
                .reduce((first, second) -> second)
                .orElse(null);
    }

    public String getType() {
        return null;
    }
}
