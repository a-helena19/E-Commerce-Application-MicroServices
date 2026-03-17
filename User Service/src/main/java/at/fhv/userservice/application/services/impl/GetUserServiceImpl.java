package at.fhv.userservice.application.services.impl;


import at.fhv.userservice.application.mapper.dtoMapper.UserDTOMapper;
import at.fhv.userservice.application.services.GetUserService;
import at.fhv.userservice.domain.exception.UserNotFoundException;
import at.fhv.userservice.domain.model.User;
import at.fhv.userservice.domain.model.UserRepository;
import at.fhv.userservice.rest.dtos.GetUserDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GetUserServiceImpl implements GetUserService {
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final UserDTOMapper userDTOMapper;

    public GetUserServiceImpl(UserRepository userRepository, CartRepository cartRepository, UserDTOMapper userDTOMapper) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.userDTOMapper = userDTOMapper;
    }

    @Override
    public GetUserDTO getUser(UUID id) {
        User user = userRepository.findById(id);
        if (user == null) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        }

        UUID cartId = null;
        try {
            Cart cart = cartRepository.findByUserId(id);
            cartId = cart.getId();
        } catch (CartNotFoundException e) {
        }

        return userDTOMapper.toGetUserDTO(user, cartId);
    }


        @Override
        public List<GetUserDTO> getUsers() {
            List<User> users = userRepository.findAll();
            List<GetUserDTO> result = new ArrayList<>();

            for (User user : users) {
                UUID cartId = null;
                try {
                    Cart cart = cartRepository.findByUserId(user.getId());
                    cartId = cart.getId();
                } catch (CartNotFoundException e) {
                }
                result.add(userDTOMapper.toGetUserDTO(user, cartId));
            }

            return result;
        }
}