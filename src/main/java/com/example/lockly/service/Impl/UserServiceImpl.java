package com.example.lockly.service.Impl;

import com.example.lockly.constant.ErrorMessage;
import com.example.lockly.domain.entity.User;
import com.example.lockly.repository.UserRepository;
import com.example.lockly.security.CustomUserDetails;
import com.example.lockly.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository
                .findUserDetailByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        ErrorMessage.User.ERR_USER_NOT_EXISTED + username));
        return new CustomUserDetails(user);
    }
}