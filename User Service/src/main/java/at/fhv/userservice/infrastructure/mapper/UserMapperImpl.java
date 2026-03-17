package at.fhv.userservice.infrastructure.mapper;


import at.fhv.userservice.application.mapper.UserMapper;
import at.fhv.userservice.domain.model.User;
import at.fhv.userservice.domain.model.UserStatus;
import at.fhv.userservice.infrastructure.persistence.model.UserEntity;
import at.fhv.userservice.infrastructure.persistence.model.UserStatusEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toDomain(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }
        return User.reconstitute(
            userEntity.getId(),
            userEntity.getFirstName(),
            userEntity.getLastName(),
            userEntity.getEmail(),
            UserStatus.valueOf(userEntity.getStatus().name())
        );
    }

    @Override
    public UserEntity toEntity(User user) {
        if (user == null) {
            return null;
        }
        UserStatusEntity statusEntity = UserStatusEntity.valueOf(user.getStatus().name());
        return new UserEntity(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            statusEntity
        );
    }
}
