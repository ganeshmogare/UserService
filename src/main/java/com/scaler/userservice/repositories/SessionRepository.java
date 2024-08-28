package com.scaler.userservice.repositories;

import com.scaler.userservice.models.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Session save(Session session);

    Optional<Session> findByToken(String token);
}
