package net.nahknarmi.arch.adapter;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Data
public class WorkspaceConfig {
    @Setter(AccessLevel.PRIVATE)
    private Long workspaceId;
    @Setter(AccessLevel.PRIVATE)
    private String apiKey;
    @Setter(AccessLevel.PRIVATE)
    private String apiSecret;

    @Builder
    public WorkspaceConfig(Long workspaceId, String apiKey, String apiSecret) {
        this.workspaceId = workspaceId;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }
}
