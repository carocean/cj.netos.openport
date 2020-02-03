package cj.studio.openport;

import java.util.Set;

public interface ISecuritySession {
    void removeProperty(String key);

    Object property(String key);

    void property(String key, String value);

    int propertyCount();

    Set<String> propertyKeys();

    boolean propertyIn(String key);

    String role(int index);

    boolean roleIn(String role);

    int roleCount();

    void addRole(String role);

    void removeRole(String role);

    String principal();

    void principal(String principal);
}
