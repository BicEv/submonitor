package ru.bicev.submonitor.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.bicev.submonitor.entity.Subscriber;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    Optional<Subscriber> findByUsername(String username);

    Optional<Subscriber> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

}
