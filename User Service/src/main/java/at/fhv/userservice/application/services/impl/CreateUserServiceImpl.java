package at.fhv.userservice.application.services.impl;


import at.fhv.userservice.application.mapper.dtoMapper.UserDTOMapper;
import at.fhv.userservice.application.services.CreateUserService;
import at.fhv.userservice.domain.exception.EmailAlreadyExistsException;
import at.fhv.userservice.domain.model.User;
import at.fhv.userservice.domain.model.UserRepository;
import at.fhv.userservice.rest.client.CartServiceClient;
import at.fhv.userservice.rest.dtos.GetUserDTO;
import org.springframework.stereotype.Service;

@Service
public class CreateUserServiceImpl implements CreateUserService {
    private final UserRepository userRepository;
    private final CartServiceClient cartServiceClient;
    private final UserDTOMapper userDTOMapper;

    public CreateUserServiceImpl(UserRepository userRepository, CartServiceClient cartServiceClient, UserDTOMapper userDTOMapper) {
        this.userRepository = userRepository;
        this.cartServiceClient = cartServiceClient;
        this.userDTOMapper = userDTOMapper;
    }

    @Override
    public GetUserDTO createUser(String firstName, String lastName, String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email '" + email + "' is already registered");
        }

        User user = User.create(firstName, lastName, email);
        User created = userRepository.save(user);

        var cartId = cartServiceClient.createCartForUser(created.getId());

        return userDTOMapper.toGetUserDTO(created, cartId);
    }

}
