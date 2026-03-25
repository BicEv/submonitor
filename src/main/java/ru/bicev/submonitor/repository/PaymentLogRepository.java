package ru.bicev.submonitor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.bicev.submonitor.entity.PaymentLog;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {

    List<PaymentLog> findAllBySubscriberId(Long subscriberId);

}
