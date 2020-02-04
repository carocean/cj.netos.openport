package cj.studio.openport;

import java.util.*;

public class DefaultSecuritySession implements ISecuritySession {
    String principal;
    List<String> roles;
    Map<String, Object> properties;

    public DefaultSecuritySession() {
        properties = new HashMap<>();
        roles = new ArrayList<>();
    }

    /**
     * 以当事人构建
     * @param principal person或appid
     */
    public DefaultSecuritySession(String principal) {
        this();
        this.principal = principal;
    }

    public DefaultSecuritySession(String principal, List<String> roles, Map<String, Object> properties) {
        this.principal = principal;
        this.roles = roles;
        this.properties = properties;
        if (roles == null) {
            this.roles = new ArrayList<>();
        }
        if (properties == null) {
            this.properties = new HashMap<>();
        }

    }

    @Override
    public void removeProperty(String key) {
        properties.remove(key);
    }

    @Override
    public Object property(String key) {
        return properties.get(key);
    }

    @Override
    public void property(String key, String value) {
        properties.put(key, value);
    }

    @Override
    public int propertyCount() {
        return properties.size();
    }

    @Override
    public Set<String> propertyKeys() {
        return properties.keySet();
    }

    @Override
    public boolean propertyIn(String key) {
        return properties.containsKey(key);
    }

    @Override
    public String role(int index) {
        return roles.get(index);
    }

    @Override
    public boolean roleIn(String role) {
        return roles.contains(role);
    }

    @Override
    public int roleCount() {
        return roles.size();
    }

    @Override
    public void addRole(String role) {
        roles.add(role);
    }

    @Override
    public void removeRole(String role) {
        roles.remove(role);
    }

    @Override
    public String principal() {
        return principal;
    }
}
