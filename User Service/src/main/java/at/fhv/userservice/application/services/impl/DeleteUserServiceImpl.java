package at.fhv.userservice.application.services.impl;

import at.fhv.userservice.application.services.DeleteUserService;
import at.fhv.userservice.domain.exception.UserNotFoundException;
import at.fhv.userservice.domain.model.User;
import at.fhv.userservice.domain.model.UserRepository;
import at.fhv.userservice.rest.client.CartServiceClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteUserServiceImpl implements DeleteUserService {
    private final UserRepository userRepository;
    private final CartServiceClient cartServiceClient;

    public DeleteUserServiceImpl(UserRepository userRepository, CartServiceClient cartServiceClient) {
        this.userRepository = userRepository;
        this.cartServiceClient = cartServiceClient;
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User existingUser = userRepository.findById(id);
        if (existingUser == null) {
            throw new UserNotFoundException("User with ID " + id + " not found");
        }

        existingUser.deactivate();
        userRepository.save(existingUser);

        // REST Call to Cart Service to delete cart for this user
        cartServiceClient.deleteCartByUserId(id);
    }
}
