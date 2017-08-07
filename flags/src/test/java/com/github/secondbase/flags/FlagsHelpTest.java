package com.github.secondbase.flags;

/**
 * Helper class for testing flags help.
 */
public class FlagsHelpTest {

    @Flag(name = "no-default-value-int")
    public static int noDefaultValue;

    @Flag(name = "no-default-value-string")
    public static String noDefaultValue2;
}
