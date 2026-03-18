package at.fhv.userservice.application.services.impl;


import at.fhv.userservice.application.mapper.dtoMapper.UserDTOMapper;
import at.fhv.userservice.application.services.UpdateUserService;
import at.fhv.userservice.domain.exception.EmailAlreadyExistsException;
import at.fhv.userservice.domain.exception.UserNotFoundException;
import at.fhv.userservice.domain.model.User;
import at.fhv.userservice.domain.model.UserRepository;
import at.fhv.userservice.rest.client.CartServiceClient;
import at.fhv.userservice.rest.dtos.GetUserDTO;
import at.fhv.userservice.rest.dtos.UpdateUserDTO;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UpdateUserServiceImpl implements UpdateUserService {
    private final UserRepository userRepository;
    private final CartServiceClient cartServiceClient;
    private final UserDTOMapper userDTOMapper;

    public UpdateUserServiceImpl(UserRepository userRepository, CartServiceClient cartServiceClient, UserDTOMapper userDTOMapper) {
        this.userRepository = userRepository;
        this.cartServiceClient = cartServiceClient;
        this.userDTOMapper = userDTOMapper;
    }

    @Override
    public GetUserDTO updateUser(UpdateUserDTO userDTO) {
        User existingUser = userRepository.findById(userDTO.getId());
        if (existingUser == null) {
            throw new UserNotFoundException("User with ID " + userDTO.getId() + " not found");
        }

        if (!existingUser.getEmail().equalsIgnoreCase(userDTO.getEmail().trim()) &&
            userRepository.existsByEmail(userDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Email '" + userDTO.getEmail() + "' is already registered");
        }

        existingUser.update(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail());

        User updatedUser = userRepository.save(existingUser);

        UUID cartId = cartServiceClient.getCartByUserId(updatedUser.getId());

        return userDTOMapper.toGetUserDTO(updatedUser, cartId);
    }
}
