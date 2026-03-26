package ru.bicev.submonitor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.bicev.submonitor.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findAllBySubscriberIdAndIsDeletedFalse(Long subscriberId);

    Optional<Subscription> findByIdAndSubscriberId(Long id, Long subscriberId);

}
