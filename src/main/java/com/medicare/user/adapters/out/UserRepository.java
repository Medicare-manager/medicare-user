package com.medicare.user.adapters.out;

import com.medicare.user.domain.model.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserRepository {
    UserDetails findByEmail(String username);
    User saveUser(User user);
}

