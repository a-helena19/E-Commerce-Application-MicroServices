package at.fhv.userservice.application.mapper.dtoMapper;

import at.fhv.userservice.domain.model.User;
import at.fhv.userservice.rest.dtos.GetUserDTO;

import java.util.UUID;

public interface UserDTOMapper {
    GetUserDTO toGetUserDTO(User user, UUID cartId);
    User toDomainFromGetDTO(GetUserDTO getUserDTO);
}
