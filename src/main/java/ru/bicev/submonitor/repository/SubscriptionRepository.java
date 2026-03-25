package ru.bicev.submonitor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.bicev.submonitor.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findAllBySubscriberId(Long subscriberId);

}
