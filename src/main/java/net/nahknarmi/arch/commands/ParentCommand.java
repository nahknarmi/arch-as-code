package net.nahknarmi.arch.commands;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "arch-as-code", description = "Architecture as code", mixinStandardHelpOptions = true, version = "1.0.0")
public class ParentCommand implements Callable<Integer> {

    @Override
    public Integer call() {
        System.out.println("Arch as code");
        return 0;
    }
}
