package org.secondbase.flags;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;


/**
 * This class can load command line arguments based of Flag annotations.
 *
 * Fields must be static, and public, and defined as a String, Long, long, Integer,
 * int, Boolean or boolean.
 *
 * Typical use:
 *
 * @Flag(name="text", description="Output text")
 * public static String text = "DefaultString";
 *
 * Flags flags = new Flags()
 *              .loadOpts(MyClass.class)
 *              .parse(args);
 *
 * System.out.print(text);
 *
 * The class supports the use of --help. If --help is given, parse will just print the help
 * and not attempt to set any values.
 *
 * If the class type contains methods annotated with @PostConstruct annotation they will be
 * automatically called after parsing the arguments.
 *
 * @author acidmoose
 *
 */
public class Flags {

    private static final Logger LOG = Logger.getLogger(Flags.class.getName());

    private VaultConfig vaultConfig;
    private AWSCredentialsProvider awsCredentialsProvider;
    private AmazonS3 amazonS3;

    /**
     * The supported field types. Determined in fieldTypeOf(Field field).
     *
     * @author acidmoose
     *
     */
    private enum FieldType {ENUM, STRING, INTEGER, LONG, BOOLEAN, UNKNOWN}

    //The option set builder.
    private final OptionParser optionParser = new OptionParser();

    // Help option
    private final OptionSpec<Void> HELP = optionParser
            .accepts("help", "Show this help");

    // Version option
    private final OptionSpec<Void> VERSION = optionParser
        .accepts("version", "Show version");

    private final OptionSpec<String> PROPERTIES_FILE = optionParser.accepts("properties-file",
        "Load properties from a given file").withRequiredArg().ofType(String.class).withValuesSeparatedBy(';');

    // Version text
    private String versionString = "NA";

    // Helper list for loaded options.
    private final Map<String, OptionHolder> options = new HashMap<String, OptionHolder>();

    // OptionSet used by option parser implementation
    private OptionSet optionSet;

    private List<?> nonOptionArguments;

    // Helper map to store enum options.
    private final Map<Class<? extends Enum<?>>, List<String>> enumOptions = new HashMap<Class<? extends Enum<?>>, List<String>>();

    private final List<Object> objects = new ArrayList<Object>();
    private final List<Class<?>> classes = new ArrayList<Class<?>>();


    /**
     * Load a class that contains Flag annotations.
     *
     * @param c - the class to load options from.
     * @return this
     */
    public Flags loadOpts(final Class<?> c) {
        classes.add(c);
        return loadOpts(c, false);
    }

    /**
     * Load an instanced class that contains Flag annotations.
     *
     * @param o - the class to load options from.
     * @return this
     */
    public Flags loadOpts(final Object o) {
        objects.add(o);
        return loadOpts(o, true);
    }

    private Flags loadOpts(final Object o, final boolean instanced) {
        final Field[] declaredFields;
        Class<?> c = null;
        if (instanced) {
            declaredFields = o.getClass().getDeclaredFields();
        } else {
            c = ((Class<?>)o);
            declaredFields = c.getDeclaredFields();
        }

        for (final Field field : declaredFields) {
            final Flag flag = field.getAnnotation(Flag.class);
            // Check if we found a flag annotation for this field.
            if (null == flag) {
                continue;
            }

            // Flag fields must be static if you are initializing the flags through a Class instance.
            if ( ! instanced && ! Modifier.isStatic(field.getModifiers())) {
                throw new IllegalArgumentException("Field "+field.toGenericString()+" is not static. Flag fields " +
                    "must be static when initializing through a Class instance.");
            }

            final String name = flag.name();
            final String description = flag.description();

            // Determine the type of field
            final FieldType type = fieldTypeOf(field, flag);

            switch (type) {

            case INTEGER:
                final OptionSpec<Integer> intOption;
                if (flag.required()) {
                    intOption = optionParser
                            .accepts(name, description)
                            .withRequiredArg()
                            .ofType(Integer.class);
                } else {
                    intOption = optionParser
                            .accepts(name, description)
                            .withOptionalArg()
                            .ofType(Integer.class);
                }
                if (instanced) {
                    addInstancedOption(type, flag, field, intOption, o);
                } else {
                    addOption(type, flag, field, intOption, c);
                }
                break;

            case STRING:
                final OptionSpec<String> stringOption;
                if (flag.required()) {
                    stringOption = optionParser
                            .accepts(name, description)
                            .withRequiredArg()
                            .ofType(String.class);
                } else {
                    stringOption = optionParser
                            .accepts(name, description)
                            .withOptionalArg()
                            .ofType(String.class);
                }
                if (instanced) {
                    addInstancedOption(type, flag, field, stringOption, o);
                } else {
                    addOption(type, flag, field, stringOption, c);
                }
                break;

            case BOOLEAN:
                final OptionSpec<Boolean> booleanOption;
                if (flag.required()) {
                    booleanOption = optionParser
                            .accepts(name, description)
                            .withOptionalArg()
                            .ofType(Boolean.class);
                } else {
                    booleanOption = optionParser
                            .accepts(name, description)
                            .withOptionalArg()
                            .ofType(Boolean.class);
                }
                if (instanced) {
                    addInstancedOption(type, flag, field, booleanOption, o);
                } else {
                    addOption(type, flag, field, booleanOption, c);
                }
                break;

            case LONG:
                final OptionSpec<Long> longOption;
                if (flag.required()) {
                    longOption = optionParser
                            .accepts(name, description)
                            .withRequiredArg()
                            .ofType(Long.class);
                } else {
                    longOption = optionParser
                            .accepts(name, description)
                            .withOptionalArg()
                            .ofType(Long.class);
                }
                if (instanced) {
                    addInstancedOption(type, flag, field, longOption, o);
                } else {
                    addOption(type, flag, field, longOption, c);
                }
                break;

            case ENUM:
                final Class<? extends Enum<?>> enumClass = flag.options();
                final Object[] enumConstants = enumClass.getEnumConstants();
                if (enumConstants == null) {
                    throw new IllegalArgumentException("Field "+field.toGenericString()+" is not an enum type.");
                }
                for (final Object object : enumConstants) {
                    addEnumOption(enumClass, object.toString());
                }
                final OptionSpec<?> enumOption;
                if (flag.required()) {
                    enumOption = optionParser
                            .accepts(name, description)
                            .withRequiredArg()
                            .ofType(enumClass);
                } else {
                    enumOption = optionParser
                            .accepts(name, description)
                            .withOptionalArg()
                            .ofType(enumClass);
                }
                if (instanced) {
                    addInstancedOption(type, flag, field, enumOption, o);
                } else {
                    addOption(type, flag, field, enumOption, c);
                }
                break;

            case UNKNOWN:
            default:
                throw new IllegalArgumentException("Field "+field.toGenericString()+" is not of a supported type.");
            }
        }
        return this;
    }

    /**
     * Set the string to show when "--version" is used.
     * @param versionString
     */
    public Flags setVersionString(final String versionString) {
        this.versionString = versionString;
        return this;
    }

    /**
     * Returns all arguments given to parse() that are not Flagged arguments.
     * @return List<?> - list of all arguments given to parse() that are not Flagged arguments.
     */
    public List<?> getNonOptionArguments() {
        return nonOptionArguments;
    }

    /**
     * If a field is found to be of type ENUM, this method is used to store
     * valid input for that specific flagged option.
     * @param enumClass
     * @param validOption
     */
    private void addEnumOption(final Class<? extends Enum<?>> enumClass, final String validOption) {
        List<String> optionsForClass = enumOptions.get(enumClass);
        if (optionsForClass == null) {
            optionsForClass = new ArrayList<String>();
        }
        optionsForClass.add(validOption);
        enumOptions.put(enumClass, optionsForClass);
    }

    /**
     * Private helper method to add an option. Will check that an option
     * with the same name has not previously been added.
     *
     * @param type
     * @param flag
     * @param field
     * @param option
     * @param c
     * @throws IllegalArgumentException
     */
    private void addOption(
            final FieldType type,
            final Flag flag,
            final Field field,
            final OptionSpec<?> option, Class<?> c)
            throws IllegalArgumentException {
        if (options.containsKey(flag.name())) {
            throw new IllegalArgumentException("Flag named "+flag.name()+" is defined more than once.");
        }
        options.put(flag.name(), new OptionHolder(type, flag, field, option, c));
    }

    /**
     * Private helper method to add an instanced option. Will check that an option
     * with the same name has not previously been added.
     *
     * @param type
     * @param flag
     * @param field
     * @param option
     * @param c
     * @throws IllegalArgumentException
     */
    private void addInstancedOption(
            final FieldType type,
            final Flag flag,
            final Field field,
            final OptionSpec<?> option, Object c)
            throws IllegalArgumentException {
        if (options.containsKey(flag.name())) {
            throw new IllegalArgumentException("Flag named "+flag.name()+" is defined more than once.");
        }
        options.put(flag.name(), new OptionHolder(type, flag, field, option, c));
    }

    /**
     * Try to set the arguments from main method on the fields loaded by loadOpts(Class<?> c).
     *
     * @param args - Arguments passed from main method.
     * @return this
     */
    public Flags parse(final String[] args) {
        optionSet = optionParser.parse(args);

        //Store non option arguments
        nonOptionArguments = optionSet.nonOptionArguments();
        if (nonOptionArguments == null) {
            nonOptionArguments = new ArrayList<String>();
        }

        //do not parse options if "help" is a part of the arguments given
        if (helpFlagged()) {
            return this;
        }
        if (versionFlagged()) {
            return this;
        }
        if (propertiesFlagged()) {
            final List<String> files = optionSet.valuesOf(PROPERTIES_FILE);
            final ArrayList<String> newArgs = new ArrayList<String>();
            for (final String filename : files) {
                final Properties props = new Properties();
                try {
                    final FileInputStream stream = new FileInputStream(filename);
                    props.load(stream);
                    for (final Enumeration<?> keys = props.propertyNames(); keys.hasMoreElements();) {
                        final String flagName = (String) keys.nextElement();
                        if (optionSet.hasArgument(flagName)) {
                            //Properties contains something already set by commandline argument
                            //Command line argument takes precedence over properties file
                            continue;
                        }
                        newArgs.add("--" + flagName);
                        final String value = props.getProperty(flagName);
                        if (value != null && ! value.isEmpty()) {
                            newArgs.add(value);
                        }
                    }

                    stream.close();
                } catch (final IOException e) {
                    throw new RuntimeException("Could not parse property-file", e);
                }
            }
            Collections.addAll(newArgs, args);
            optionSet = optionParser.parse(newArgs.toArray(new String[newArgs.size()]));
        }

        for (final OptionHolder holder : options.values()) {
            try {
                final OptionSpec<?> optionSpec = holder.getOptionSpec();

                // Deal with the flags that were given on the command line.
                if (optionSet.has(optionSpec)) {
                    Object value = optionSet.valueOf(optionSpec);
                    final SecretPath vaultPath = getVaultPath(value);
                    if (vaultPath != null) {
                        try {
                            value = getVaultSecret(vaultPath);
                        } catch (final VaultException e) {
                            throw new RuntimeException("Could not fetch value from Vault: " + value, e);
                        }
                    }
                    final SecretPath s3path = getS3Path(value);
                    if (s3path != null) {
                        try {
                            value = getS3Value(s3path);
                        } catch (final IOException e) {
                            throw new RuntimeException("Could not fetch value from S3: " + value, e);
                        }
                    }
                    switch(holder.getType()) {
                        case INTEGER:
                            if (holder.isInstanced()) {
                                holder.getField().set(holder.getObjectSource(), value);
                            } else {
                                holder.getField().set(holder.getField().getClass(), value);
                            }
                            break;

                        case LONG:
                            if (holder.isInstanced()) {
                                holder.getField().set(holder.getObjectSource(), value);
                            } else {
                                holder.getField().set(holder.getField().getClass(), value);
                            }
                            break;

                        case STRING:
                            if (holder.isInstanced()) {
                                holder.getField().set(holder.getObjectSource(), value);
                            } else {
                                holder.getField().set(holder.getField().getClass(), value);
                            }
                            break;

                        case BOOLEAN:
                            if (holder.isInstanced()) {
                                holder.getField().set(holder.getObjectSource(),
                                        (value == null) ? true : value);
                            } else {
                                holder.getField().set(holder.getField().getClass(),
                                        (value == null) ? true : value);
                            }
                            break;

                        case ENUM:
                            if (holder.isInstanced()) {
                                try {
                                    holder.getField().set(holder.getObjectSource(), value);
                                } catch (final Exception e) {
                                    throw new IllegalArgumentException("Option given is not a valid option. Valid options are: "+enumOptions.get(holder.flag.options()).toString()+".");
                                }
                            } else {
                                try {
                                    holder.getField().set(holder.getField().getClass(), value);
                                } catch (final Exception e) {
                                    throw new IllegalArgumentException("Option given is not a valid option. Valid options are: "+enumOptions.get(holder.flag.options()).toString()+".");
                                }
                            }
                            break;
                    }

                    // No further action needed for this field.
                    continue;
                }

                // Check if flag that does not occur in command line was required.
                if (holder.getFlag().required()) {
                    throw new IllegalArgumentException("Required argument missing: " + holder.getFlag().name());
                }
            } catch (final IllegalAccessException e) {
                throw new RuntimeException("Programming error, illegal access for " + holder.getField().toGenericString());
            }
        }
        try {
            callPostConstructMethods();
        } catch (final InvocationTargetException e) {
            throw new RuntimeException("Post construct method thrown exception", e.getCause());
        } catch (final IllegalAccessException e) {
            // this probably should never happen
            throw new RuntimeException(
                    "Programming error, illegal access to a post construct method",
                    e);
        }
        return this;
    }

    /**
     * Set a custom client config for connecting to AWS S3. Will attempt to use credentials in
     * ~/.aws/credentials (or legacy ~/.aws/config) if not set.
     *
     * @param awsCredentialsProvider the custom s3 client configuration
     */
    public Flags setS3CredentialsProvider(final AWSCredentialsProvider awsCredentialsProvider) {
        this.awsCredentialsProvider = awsCredentialsProvider;
        return this;
    }

    /**
     * Attempt to fetch a secret from S3.
     *
     * @param s3path where to fetch it from
     * @return the content of the file found on S3
     * @throws IOException on problems streaming the content of the file
     * @throws AmazonS3Exception on problems communicating with amazon
     */
    private Object getS3Value(final SecretPath s3path) throws IOException, AmazonS3Exception {
        LOG.log(Level.INFO, "Fetching secret from S3");
        final AmazonS3 s3Client;
        if (awsCredentialsProvider != null) {
            s3Client = AmazonS3ClientBuilder.standard().withCredentials(awsCredentialsProvider).build();
        } else {
            s3Client = AmazonS3ClientBuilder.standard().build();
        }
        final S3Object s3object
                = s3Client.getObject(new GetObjectRequest(s3path.getPath(), s3path.getKey()));
        final BufferedReader reader
                = new BufferedReader(new InputStreamReader(s3object.getObjectContent()));
        final StringBuilder b = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null) {
            b.append(line);
        }
        LOG.log(Level.INFO, "Found secret");
        reader.close();
        return b.toString();
    }

    /**
     * Returns S3 path based on the syntax: secret:s3://bucket/path/to/file
     */
    protected SecretPath getS3Path(final Object value) {
        if (value == null) {
            return null;
        }
        final String path = value.toString();
        final Pattern p = Pattern.compile("(secret:s3:\\/\\/)([a-z-_]*)(\\/)(.*)");
        final Matcher m = p.matcher(path);
        if (! m.matches())
            return null;
        return new SecretPath(m.group(2), m.group(4));
    }

    /**
     * Provide a custom config for Vault. If not provided, vault will attempt to find these values
     * from system environment:
     *
     * VAULT_ADDR
     * VAULT_TOKEN
     * VAULT_OPEN_TIMEOUT
     * VAULT_READ_TIMEOUT
     * VAULT_SSL_CERT
     * VAULT_SSL_VERIFY
     *
     * @link: https://github.com/BetterCloud/vault-java-driver
     * @param vaultConfig custom vault configuration
     */
    public Flags setVaultConfig(final VaultConfig vaultConfig) {
        this.vaultConfig = vaultConfig;
        return this;
    }

    /**
     * Returns vault path and key based on the syntax: vaultflag:path:key
     */
    protected SecretPath getVaultPath(final Object value) {
        if (value == null) {
            return null;
        }
        final String path = value.toString();
        final Pattern p = Pattern.compile("(vaultflag:)(.*)(:)(.*)");
        final Matcher m = p.matcher(path);
        if (! m.matches())
            return null;
        return new SecretPath(m.group(2), m.group(4));
    }

    /**
     * Get a value from vault based on a VaultPath
     *
     * @param vaultPath the details of where to find the value
     * @return the value found in vault
     * @throws VaultException if there were problems getting the value
     */
    private Object getVaultSecret(final SecretPath vaultPath) throws VaultException {
        LOG.log(Level.INFO, "Fetching secret from Vault");
        final Vault vault = new Vault((vaultConfig == null) ? new VaultConfig().build() : vaultConfig);
        final String secret = vault
                .logical()
                .read(vaultPath.getPath())
                .getData()
                .get(vaultPath.getKey());
        LOG.log(Level.INFO, "Found secret");
        return secret;
    }

    /**
     * Call all the methods annotated with the @PostConstruct annotation and have no parameters.
     * For the flagged objects all the instance methods are called (including private).
     * Inherited methods are not recognised (we may add this later). For the flagged classes all
     * the static method are called. The instance methods are ignored.
     *
     * @throws InvocationTargetException if the underlying post construct method throws an
     * exception.
     * @throws IllegalAccessException if there is no access to the method.
     */
    private void callPostConstructMethods() throws InvocationTargetException, IllegalAccessException {
        for (final Object o : objects) {
            for (final Method method : findPostConstructMethod(o.getClass(), true)) {
                method.invoke(o);
            }
        }
        for (final Class<?> cls : classes) {
            for (final Method method : findPostConstructMethod(cls, false)) {
                method.invoke(false);
            }
        }
    }

    private List<Method> findPostConstructMethod(final Class<?> type, final boolean instanced) {
        final List<Method> result = new ArrayList<Method>();
        for (final Method method : type.getDeclaredMethods()) {
            if (method.getAnnotation(PostConstruct.class) != null) {
                final boolean isStatic = Modifier.isStatic(method.getModifiers());
                if ((instanced && !isStatic) || (!instanced && isStatic)) {
                    checkNoMethodArguments(method);
                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }
                    result.add(method);
                }
            }
        }
        return result;
    }

    private void checkNoMethodArguments(final Method method) {
        if (method.getParameterTypes().length != 0) {
            final String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
            throw new IllegalArgumentException(
                    "Post construct method " + methodName + " must not have parameters");
        }
    }

    /**
     * Prints the help to the specified output stream.
     *
     * @param out the OutputStream we wish to print the help output to.
     */
    public void printHelp(final OutputStream out) {
        final PrintWriter w = new PrintWriter(out);

        final Map<String, List<OptionHolder>> holdersByClass = new TreeMap<String, List<OptionHolder>>();

        // Iterate over all the options we have gathered and stash them by class.
        for (final OptionHolder holder : options.values()) {
            // Fetch list corresponding to source class name
            final String className;
            if (holder.isInstanced()) {
                className = holder.getObjectSource().getClass().getName();
            } else {
                className = holder.getClassSource().getName();
            }
            List<OptionHolder> holderList = holdersByClass.get(className);
            if (null == holderList) {
                // The list did not exist.  Create it.
                holderList = new LinkedList<OptionHolder>();
                holdersByClass.put(className, holderList);
            }

            holderList.add(holder);
        }

        // Output options by class
        for (final Map.Entry<String, List<OptionHolder>> ent : holdersByClass.entrySet()) {
            final String className = ent.getKey();
            final List<OptionHolder> holderList = ent.getValue();

            // Sort the options. In Java, sorting collections is worse
            // than watching Pandas fuck.
            Collections.sort(holderList, new Comparator<OptionHolder>() {
                public int compare(OptionHolder a, OptionHolder b) {
                    return a.getFlag().name().toLowerCase().compareTo(b.getFlag().name().toLowerCase());
                }
            });

            final StringBuffer buff = new StringBuffer();

            buff.append("\n\n")
            .append(className)
            .append("\n")
            .append("------------------------------------------------------------------------")
            .append("\n");

            for (final OptionHolder holder : holderList) {
                // Mark required flags with a "*"
                buff.append(holder.getFlag().required() ? "* " : "  ");

                String s;
                try {
                    s = "  --" + holder.getFlag().name() + " <" + holder.getType() + "> default: "
                            + (holder.isInstanced()
                                ? holder.getField().get(holder.getObjectSource())
                                : holder.getField().get(holder.getClassSource()));
                } catch (final IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                //TODO: handle enum options
                if (holder.getFlag().options() != NoOption.class) {
                    s = s + " options: "+enumOptions.get(holder.getFlag().options()).toString();
                }

                // Avert your eyes.
                int spaces = 50 - s.length();
                spaces = spaces < 0 ? 0 : spaces;
                buff.append(s)
                .append("  . . . . . . . . . . . . . . . . . . . . . . . . ".substring(0, spaces))
                .append("| " + holder.getFlag().description())
                .append("\n");
            }
            w.println(buff.toString());
        }
        w.flush();
    }

    /**
     * Prints the version to the specified output stream.
     *
     * @param out the OutputStream we wish to print the version output to.
     */
    public void printVersion(final OutputStream out) {
        final PrintWriter w = new PrintWriter(out);
        w.println(versionString);
        w.flush();
    }

    /**
     * @return {@code true} if a "--help" flag was passed on the command line.
     */
    public boolean helpFlagged() {
        return optionSet.has(HELP);
    }

    /**
     * @return {@code true} if a "--version" flag was passed on the command line.
     */
    public boolean versionFlagged() {
        return optionSet.has(VERSION);
    }

    /**
     *
     * @return {@code true} if a "--properties-file" flag was passed on the command line.
     */
    public boolean propertiesFlagged() {
        return optionSet.hasArgument(PROPERTIES_FILE);
    }

    /**
     * Debugging method. Prints the Flags found and the corresponding Fields.
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public void printFlags() {
        try {
            for (final OptionHolder holder : options.values()) {
                System.out.println("Field: "+holder.getField().toGenericString()+"\nFlag: name:"+holder.getFlag().name()
                        +", description:"+holder.getFlag().description()+", type:"+holder.getType()
                        +", default:"+(holder.isInstanced()
                            ? holder.getField().get(holder.getObjectSource())
                            : holder.getField().get(holder.getClassSource())));
            }
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the field type of a Field instance.
     *
     * @param field the field instance we want the type for.
     * @return the type of the {@code field} in question.
     */
    private static FieldType fieldTypeOf(final Field field, final Flag flag) {
        if (field.getType().isAssignableFrom(Long.TYPE)
                || field.getType().isAssignableFrom(Long.class)) {
            return FieldType.LONG;
        }

        if (field.getType().isAssignableFrom(Boolean.TYPE)
                || field.getType().isAssignableFrom(Boolean.class)) {
            return FieldType.BOOLEAN;
        }

        if (field.getType().isAssignableFrom(String.class)) {
            return FieldType.STRING;
        }

        if (field.getType().isAssignableFrom(Integer.TYPE)
                || field.getType().isAssignableFrom(Integer.class)) {
            return FieldType.INTEGER;
        }

        if (flag.options() != NoOption.class
                && field.getType().isAssignableFrom(flag.options())) {
            return FieldType.ENUM;
        }

        return FieldType.UNKNOWN;
    }

    /**
     * Get all Flag instances parsed from class (via the loadOpts(Class<?> c) method) as a List.
     * @return List containing Flag instances.
     */
    public List<Flag> getFlagsAsList() {
        final List<Flag> list = new ArrayList<Flag>();
        for(final OptionHolder holder : options.values()) {
            list.add(holder.getFlag());
        }
        return list;
    }

    /**
     * Internal class that holds an option's corresponding FieldType, Field, Flag and OptionSpec.
     *
     * @author acidmoose
     *
     */
    private static class OptionHolder {
        private final Flag flag;
        private final Field field;
        private final OptionSpec<?> optionSpec;
        private final FieldType type;
        private final Class<?> classSource;
        private final Object objectSource;

        public OptionHolder(
                final FieldType type,
                final Flag flag,
                final Field field,
                final OptionSpec<?> optionSpec,
                final Class<?> classSource) {
            this.type = type;
            this.flag = flag;
            this.field = field;
            this.optionSpec = optionSpec;
            this.classSource = classSource;
            objectSource = null;
        }

        public OptionHolder(
                final FieldType type,
                final Flag flag,
                final Field field,
                final OptionSpec<?> optionSpec,
                final Object objectSource) {
            this.type = type;
            this.flag = flag;
            this.field = field;
            this.optionSpec = optionSpec;
            this.objectSource = objectSource;
            classSource = null;
        }

        public boolean isInstanced() {
            return objectSource != null;
        }

        public Flag getFlag() {
            return flag;
        }

        public Field getField() {
            // To support private variables we simply make the field accessible.
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            return field;
        }

        public OptionSpec<?> getOptionSpec() {
            return optionSpec;
        }

        public FieldType getType() {
            return type;
        }

        public Class<?> getClassSource() {
            return classSource;
        }

        public Object getObjectSource() {
            return objectSource;
        }
    }

}
