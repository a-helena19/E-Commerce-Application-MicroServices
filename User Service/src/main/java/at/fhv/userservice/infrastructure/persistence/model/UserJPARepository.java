package at.fhv.userservice.infrastructure.persistence.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserJPARepository extends JpaRepository<UserEntity, UUID> {
    boolean existsByEmail(String email);
}
