package at.fhv.userservice.application.mapper;

import at.fhv.userservice.domain.model.User;
import at.fhv.userservice.infrastructure.persistence.model.UserEntity;

public interface UserMapper {
    User toDomain(UserEntity userEntity);
    UserEntity toEntity(User user);
}
