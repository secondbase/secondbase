# Secondbase

[![CircleCI](https://circleci.com/gh/secondbase/secondbase/tree/master.svg?style=shield)](https://circleci.com/gh/secondbase/secondbase/tree/master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A collection of helper tools in the form of Java libraries optionally
controlled through command line arguments. The main aim of this project is to
provide lightweight integration behind a common flags implementation.

# Install

SecondBase is deployed through Maven in [The Central Repository](http://central.sonatype.org/).

## Maven 'all'

The `all` artifact has all the dependencies built into it and provides a convenience class for
instantiating everything.

```xml
<dependency>
    <groupId>com.github.secondbase</groupId>
    <artifactId>all</artifactId>
</dependency>
```

```java
import com.github.secondbase.core.SecondBase;
import com.github.secondbase.core.SecondBaseException;

public final class MyService {
    private MyService() {}
    public static void main(final String[] args) throws SecondBaseException {
        new SecondBase(args, new Flags().loadOpts(MyService.class));
    }
}
```

## Maven 'core'

The `core` artifact allows for choosing which modules you want. Any of which can be omitted if you
do not need the features it provides. This will also lessen the dependency tree and need of
dependency management.

```xml
<dependency>
    <groupId>com.github.secondbase</groupId>
    <artifactId>core</artifactId>
</dependency>
```

```java
import com.github.secondbase.all.SecondBase;
import com.github.secondbase.core.SecondBaseException;

public final class MyService {
    private MyService() {}
    public static void main(final String[] args) throws SecondBaseException, IOException {
        final SecondBaseModule[] modules = {};
        new SecondBase(args, modules, new Flags().loadOpts(MyService.class));
    }
}
```

This [Java application](example/src/main/java/com/github/secondbase/example/main/HelloAll.java) shows an
example http service running SecondBase configured manually with all modules activated. The
[pom.xml](example/pom.xml) file shows all the dependencies needed.

Below is the list of modules which can be used.

# Modules

* [consul](consul) classes for registering services in [Consul](https://www.consul.io/)
    * [consul-prometheus-widget](consul-prometheus-widget) register the
    [Prometheus](https://prometheus.io/) metrics endpoint in consul
* [flags](flags) for command line argument parsing and secret fetching
    * `secrets/s3-secrets` dynamically fetch flags from [S3](https://aws.amazon.com/s3/)
    * `secrets/vault-secrets` dynamically fetch flags from [Vault](https://www.vaultproject.io/)
* [jsonlogging](jsonlogging) to wrap setting up json logging to stdout
* [webconsole](webconsole) to start a standalone webserver
    * `prometheus-webconsole` host a [Prometheus](https://prometheus.io/) metrics endpoint in
    webconsole

# Examples

* Basic use of SecondBase with only Flags:
[HelloSecondBase](example/src/main/java/com/github/secondbase/example/main/HelloSecondBase.java)
* Using every module available:
[HelloAll](example/src/main/java/com/github/secondbase/example/main/HelloAll.java)
* Registering a service with Consul:
[HelloConsul](example/src/main/java/com/github/secondbase/example/main/HelloConsul.java)
* Parsing command line arguments through Flags:
[HelloFlags](example/src/main/java/com/github/secondbase/example/main/HelloFlags.java)
* Setting up and using json logging:
[HelloJsonLogging](example/src/main/java/com/github/secondbase/example/main/HelloJsonLogging.java)
