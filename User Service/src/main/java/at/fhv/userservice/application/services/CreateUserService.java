package at.fhv.userservice.application.services;

import at.fhv.userservice.rest.dtos.GetUserDTO;

public interface CreateUserService {
    GetUserDTO createUser(String firstName, String lastName, String email);
}
