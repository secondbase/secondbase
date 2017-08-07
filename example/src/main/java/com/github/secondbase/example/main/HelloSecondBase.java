package com.github.secondbase.example.main;

import com.github.secondbase.core.SecondBase;
import com.github.secondbase.core.SecondBaseException;
import com.github.secondbase.core.config.SecondBaseModule;
import com.github.secondbase.flags.Flag;
import com.github.secondbase.flags.Flags;

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
