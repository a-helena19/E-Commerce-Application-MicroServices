package at.fhv.userservice.application.services;

import at.fhv.userservice.rest.dtos.GetUserDTO;
import at.fhv.userservice.rest.dtos.UpdateUserDTO;

public interface UpdateUserService {
    GetUserDTO updateUser(UpdateUserDTO userDTO);
}
