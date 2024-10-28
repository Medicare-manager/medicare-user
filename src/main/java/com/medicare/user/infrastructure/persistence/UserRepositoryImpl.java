package com.medicare.user.infrastructure.persistence;

import com.medicare.user.adapters.out.UserRepository;
import com.medicare.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepositoryImpl extends JpaRepository<User, Long>, UserRepository {

    @Override
    UserDetails findByEmail(String username);

}
