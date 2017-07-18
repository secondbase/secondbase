# Secondbase

A collection of helper tools in the form of Java libraries optionally
controlled through command line arguments. The main aim of this project is to
provide lightweight integration behind a common flags implementation.

For now, it consists of the following components:

* [flags](flags) for command line argument parsing and secret fetching
    * [s3-secrets](secrets/s3) dynamically fetch flags from Amazon S3
    * [vault-secrets](secrets/vault) dynamically fetch flags from HashiCorp Vault
