package org.secondbase.example.main;

import org.secondbase.core.SecondBase;
import org.secondbase.core.SecondBaseException;
import org.secondbase.core.config.SecondBaseModule;
import org.secondbase.flags.Flag;
import org.secondbase.flags.Flags;

public final class HelloSecondBase {

    @Flag(name = "var")
    private String variable = "value";

    private void printVariable() {
        System.out.println(variable);
    }

    public static void main(final String[] args) throws SecondBaseException {
        final HelloSecondBase helloSecondBase = new HelloSecondBase();
        final SecondBaseModule[] modules = new SecondBaseModule[] {};
        new SecondBase(args, modules, new Flags().loadOpts(helloSecondBase));
        helloSecondBase.printVariable();
    }
}
