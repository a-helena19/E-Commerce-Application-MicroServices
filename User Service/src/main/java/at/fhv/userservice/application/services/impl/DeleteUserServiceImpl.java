package at.fhv.userservice.application.services.impl;


import at.fhv.userservice.application.services.DeleteUserService;
import at.fhv.userservice.domain.exception.UserNotFoundException;
import at.fhv.userservice.domain.model.User;
import at.fhv.userservice.domain.model.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteUserServiceImpl implements DeleteUserService {
    private final UserRepository userRepository;
    private final DeleteCartService deleteCartService;

    public DeleteUserServiceImpl(UserRepository userRepository, DeleteCartService deleteCartService) {
        this.userRepository = userRepository;
        this.deleteCartService = deleteCartService;
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

        deleteCartService.deleteCartByUserId(id);
    }
}
