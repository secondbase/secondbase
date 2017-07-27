package org.secondbase.example.main;

import org.secondbase.flags.Flag;
import org.secondbase.flags.Flags;

public final class HelloFlags {

    private HelloFlags() {
    }

    @Flag(name = "int")
    private static int integer = 1;

    @Flag(name = "boolean")
    public static boolean bool = false;

    @Flag(name = "Boolean")
    public static Boolean bool2 = false;

    @Flag(name = "Integer")
    public static Integer integer2 = new Integer(1);

    @Flag(name = "long")
    public static long longNum = 1L;

    @Flag(name = "Long")
    public static long longNum2 = 1L;

    public enum SimpleEnum {
        OPTION1, OPTION2
    };

    @Flag(name = "option", options = SimpleEnum.class)
    public static SimpleEnum option = SimpleEnum.OPTION1;

    @Flag(name = "text", description = "Output text")
    private static String text = "default text";

    public static void main(final String[] args) {
        final Flags flags = new Flags().loadOpts(HelloFlags.class).parse(args);

        // print help and quit if help has been flagged
        if (flags.helpFlagged()) {
            flags.printHelp(System.out);
            return;
        }
        System.out.println("integer:, " + integer + "!");
        System.out.println("Integer:, " + integer2 + "!");
        System.out.println("bool:, " + bool + "!");
        System.out.println("Boolean:, " + bool2 + "!");
        System.out.println("long:, " + longNum + "!");
        System.out.println("Long:, " + longNum2 + "!");
        System.out.println("text, " + text + "!");
    }
}
