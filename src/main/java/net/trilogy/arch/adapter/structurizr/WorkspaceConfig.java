package net.trilogy.arch.adapter.structurizr;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Builder
@Data
@RequiredArgsConstructor
public class WorkspaceConfig {
    private final Long workspaceId;
    private final String apiKey;
    private final String apiSecret;
}
