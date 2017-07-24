# Secondbase

[![CircleCI](https://circleci.com/gh/secondbase/secondbase/tree/master.svg?style=shield)](https://circleci.com/gh/secondbase/secondbase/tree/master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A collection of helper tools in the form of Java libraries optionally
controlled through command line arguments. The main aim of this project is to
provide lightweight integration behind a common flags implementation.

For now, it consists of the following components:

* [core](core) Core classes for loading secondbase and managing its components
* [flags](flags) for command line argument parsing and secret fetching
    * [s3-secrets](secrets/s3) dynamically fetch flags from Amazon S3
    * [vault-secrets](secrets/vault) dynamically fetch flags from HashiCorp Vault
