package at.fhv.userservice.application.mapper.dtoMapper;

import at.fhv.userservice.domain.model.User;
import at.fhv.userservice.domain.model.UserStatus;
import at.fhv.userservice.rest.dtos.GetUserDTO;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserDTOMapperImpl implements UserDTOMapper {

    @Override
    public GetUserDTO toGetUserDTO(User user, UUID cartId) {
        return new GetUserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getStatus().name(),
                cartId
        );
    }

    @Override
    public User toDomainFromGetDTO(GetUserDTO getUserDTO) {
        return User.reconstitute(
                getUserDTO.id(),
                getUserDTO.firstName(),
                getUserDTO.lastName(),
                getUserDTO.email(),
                UserStatus.valueOf(getUserDTO.status())
        );
    }
}
