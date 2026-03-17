package at.fhv.userservice.application.services.impl;


import at.fhv.userservice.application.mapper.dtoMapper.UserDTOMapper;
import at.fhv.userservice.application.services.CreateUserService;
import at.fhv.userservice.domain.exception.EmailAlreadyExistsException;
import at.fhv.userservice.domain.model.User;
import at.fhv.userservice.domain.model.UserRepository;
import at.fhv.userservice.rest.dtos.GetUserDTO;
import org.springframework.stereotype.Service;

@Service
public class CreateUserServiceImpl implements CreateUserService {
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final UserDTOMapper userDTOMapper;

    public CreateUserServiceImpl(UserRepository userRepository, CartRepository cartRepository, UserDTOMapper userDTOMapper) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.userDTOMapper = userDTOMapper;
    }

    @Override
    public GetUserDTO createUser(String firstName, String lastName, String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email '" + email + "' is already registered");
        }

        User user = User.create(firstName, lastName, email);
        User created = userRepository.save(user);

        Cart newCart = Cart.create(created.getId());
        Cart savedCart = cartRepository.save(newCart);

        return userDTOMapper.toGetUserDTO(created, savedCart.getId());
    }

}
