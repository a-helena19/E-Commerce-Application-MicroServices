package at.fhv.userservice.application.services;


import at.fhv.userservice.rest.dtos.GetUserDTO;

import java.util.List;
import java.util.UUID;

public interface GetUserService {
    GetUserDTO getUser(UUID uuid);
    List<GetUserDTO> getUsers();
}
