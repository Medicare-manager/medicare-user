package com.medicare.user.adapters.out;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserRepository {
    UserDetails findByEmail(String username);
}

