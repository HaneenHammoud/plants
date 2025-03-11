package com.proposal.Nature.Heaven.repository;
import com.proposal.Nature.Heaven.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
    public List<User> findByUsernameContaining(String username);
}
