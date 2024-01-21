package com.polimi.PPP.CodeKataBattle.service;

import com.polimi.PPP.CodeKataBattle.Model.User;
import com.polimi.PPP.CodeKataBattle.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) {

        User user = repository.findByEmail(email).orElseThrow(() ->
                new RuntimeException(String.format("User with the email address '%s' not found.")));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .build();
    }
}
