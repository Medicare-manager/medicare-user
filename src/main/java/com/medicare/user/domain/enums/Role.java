package com.medicare.user.domain.enums;

public enum Role {
    MASTER("master"),
    USER("user");

    private String role;

    Role(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public static Role fromString(String text) {
        for (Role role : Role.values()) {
            if (role.role.equalsIgnoreCase(text)) {
                return role;
            }
        }
        throw new IllegalArgumentException("String '" + text + "' não corresponde a nenhuma enumeração Role.");
    }

}
