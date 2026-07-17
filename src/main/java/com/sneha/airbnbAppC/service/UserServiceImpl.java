package com.sneha.airbnbAppC.service;


import com.sneha.airbnbAppC.entity.User;
import com.sneha.airbnbAppC.exception.ResourceNotFoundException;
import com.sneha.airbnbAppC.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    @Override
    public User getUserById(Long userId) {
        log.info("Getting the user by id {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User with id doesn't exists : " +userId));
    }

}
