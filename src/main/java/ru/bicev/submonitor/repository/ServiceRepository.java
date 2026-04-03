package ru.bicev.submonitor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.bicev.submonitor.entity.Service;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    @Query("SELECT s FROM Service s WHERE s.owner.id IS NULL OR s.owner.id = :ownerId")
    List<Service> findAllAvailable(@Param("ownerId") Long ownerId);

    @Query("SELECT s FROM Service s WHERE s.id = :id AND (s.owner.id IS NULL OR s.owner.id = :ownerId)")
    Optional<Service> findByIdAndOwnerIdEqualsOrNull(@Param("id") Long id, @Param("ownerId") Long ownerId);

    Optional<Service> findByIdAndOwnerId(Long id, Long ownerId);

}
