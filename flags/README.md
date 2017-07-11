# Flags

This is a library to help developers build neatly formatted and easy to understand command line parsing.

Annotate variables to set them from command line arguments, or reference a location where Flags can get the variable from.

# Example

This example can be run with the command **java HelloFlags --help** which will print the command line help, or **java HelloFlags --text "World"** which will print "Hello, World!".

```java
import org.cloudname.flags.Flag;
import org.cloudname.flags.Flags;

public class HelloFlags {

    @Flag(name="text", description="Output text")
    private static String text = "default entity";

    public static void main(String[] args) {
        final Flags flags = new Flags()
                .loadOpts(HelloFlags.class)
                .parse(args);

        //print help and quit if help has been flagged
        if (flags.helpFlagged()) {
            flags.printHelp(System.out);
            return;
        }

        System.out.println("Hello, " + text + "!");
    }
}

```
# Supported types
```java
@Flag(name="string")
public static String string = "NA";

@Flag(name="int")
private static int integer = 1;

@Flag(name="boolean")
public static boolean bool = false;

@Flag(name="Integer")
public static Integer integer2 = new Integer(1);

@Flag(name="Boolean")
public static Boolean bool2 = false;

@Flag(name="long")
public static long longNum = 1L;

@Flag(name="Long")
public static long longNum2 = 1L;

public enum SimpleEnum {OPTION1, OPTION2};

@Flag(name="option", options=SimpleEnum.class)
public static SimpleEnum option = SimpleEnum.OPTION1;
```

# Static vs non static variables

Flags support static variables by loading the class

```java
new Flags().loadOpts(HelloFlags.class);
```

and non static variables by loading the instance

```java
final HelloFlags helloFlags = new HelloFlags();
new Flags().loadOpts(helloFlags);
```

# Variable ownership

Flags support both public and private variables.

# Secrets

Flags support fetching secrets or config on load. Currently supports HashiCorp Vault and Amazon S3.

NB! Secrets only work with String variables.

```
--mySecretVariableFromVault secret:vault://secret/foo:value
--mySecretVariableFromS3 secret:s3://bucket/foo/secret
```

By appending "secret:" before the path, flags will attempt to get the value for the variable from
the given location.

# Secret support - HashiCorp Vault

Flags support fetching values from HashiCorp Vault using the vault-java-driver. To use the feature, first make sure Flags can connect to Vault. Vault will use configuration from environment variables. Refer to https://github.com/BetterCloud/vault-java-driver for more about the specific variables.

Lets say a secret is written to Vault like this **vault write secret/foo text=Vault ttl=1y**, resulting in **vault read secret/foo**:
```
Key                 Value
---                 -----
refresh_interval    768h0m0s
ttl                 1y
value               Vault
```

To make Flags find the value "Vault" and print "Hello, Vault!", start the example program like this:

**java HelloFlags --text "vault://secret/foo:value"**

# Secret support - AWS S3

Flags support fetching values from S3 using the AWS Java SDK. To use the feature, first make sure Flags can connect to S3. The SDK will use credentials for accessing S3 in ~/.aws/credentials. Refer to http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html#cli-config-files for more about the specific variables.

## How the feature works:

First upload a file in S3 containing the value you want to to be set as the variable. Remember that newlines and such characters are treated as part of the value and will be put into the variable, so don't add newlines at the end of the file if you don't intend to have a newline in the variable.

Lets say we create a file called "helloflags-secret", containing only the word "S3", and upload it to the S3 bucket called "helloflags-bucket" under the folder "somesubfolder".

To make Flags find the value "S3" and print "Hello, S3!", start the example progam like this:

**java HelloFlags --text "secret:s3://helloflags-bucket/somesubfolder/helloflags-secret"**
