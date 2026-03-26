package ru.bicev.submonitor.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import ru.bicev.submonitor.entity.PaymentLog;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {

    @EntityGraph(attributePaths = { "subscription", "subscription.service" })
    Page<PaymentLog> findAllBySubscriberId(Long subscriberId, Pageable pageable);

    @EntityGraph(attributePaths = { "subscription", "subscription.service" })
    Optional<PaymentLog> findByIdAndSubscriberId(Long id, Long subscriberId);

}
