package net.nahknarmi.arch.domain.c4;

public interface Entity extends Relatable, Tagable {
    String getDescription();

    C4Path getPath();
}
