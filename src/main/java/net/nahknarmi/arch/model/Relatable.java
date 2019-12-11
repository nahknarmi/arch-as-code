package net.nahknarmi.arch.model;

import java.util.List;

public interface Relatable {
    String getName();

    List<Relatable> relations();
}
