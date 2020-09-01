package net.trilogy.arch.domain;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Date;

@EqualsAndHashCode
@Data
@NoArgsConstructor
public class ImportantTechnicalDecision {
    @NonNull
    private String id;
    @NonNull
    private Date date;
    @NonNull
    private String title;
    @NonNull
    private String status;
    @NonNull
    private String content;

    @Builder
    public ImportantTechnicalDecision(@NonNull String id, @NonNull Date date, @NonNull String title, @NonNull String status, @NonNull String content) {
        this.id = id;
        this.date = date;
        this.title = title;
        this.status = status;
        this.content = content;
    }
}
