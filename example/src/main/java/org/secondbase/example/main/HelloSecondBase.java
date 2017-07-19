package org.secondbase.example.main;

import org.secondbase.core.SecondBase;
import org.secondbase.flags.Flag;
import org.secondbase.flags.Flags;

public class HelloSecondBase {
    @Flag(name = "var")
    private String variable = "";

    private void printVariable() {
        System.out.println(variable);
    }

    public static void main(final String[] args) {
        final HelloSecondBase helloSecondBase = new HelloSecondBase();
        new SecondBase(args, new Flags().loadOpts(helloSecondBase));
        helloSecondBase.printVariable();
    }
}
