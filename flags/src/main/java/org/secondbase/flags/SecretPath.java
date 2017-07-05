package org.secondbase.flags;

/**
 * Holds parts for a secret's location
 * @author espen
 */
public class SecretPath {
    private String path;
    private String key;

    public SecretPath(final String path, final String key) {
        this.path = path;
        this.key = key;
    }

    public String getPath() {
        return path;
    }

    public String getKey() {
        return key;
    }
}
