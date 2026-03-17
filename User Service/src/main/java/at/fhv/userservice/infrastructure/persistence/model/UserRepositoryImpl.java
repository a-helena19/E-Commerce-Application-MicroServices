package at.fhv.userservice.infrastructure.persistence.model;


import at.fhv.userservice.application.mapper.UserMapper;
import at.fhv.userservice.domain.model.User;
import at.fhv.userservice.domain.model.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final UserJPARepository userJPARepository;
    private final UserMapper userMapper;

    public UserRepositoryImpl(UserJPARepository userJPARepository, UserMapper userMapper) {
        this.userJPARepository = userJPARepository;
        this.userMapper = userMapper;
    }

    @Override
    public User save(User user) {
        UserEntity userEntity = userMapper.toEntity(user);
        UserEntity savedEntity = userJPARepository.save(userEntity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public User findById(UUID id) {
        return userJPARepository.findById(id)
                .map(userMapper::toDomain)
                .orElse(null);
    }

    @Override
    public List<User> findAll() {
        return userJPARepository.findAll()
                .stream()
                .map(userMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJPARepository.existsByEmail(email);
    }
}

