package ru.bicev.submonitor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ru.bicev.submonitor.entity.Service;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    @Query("SELECT s FROM Service s WHERE s.owner.id IS NULL OR s.owner.id =: ownerId")
    List<Service> findAllAvailable(Long ownerId);

}
