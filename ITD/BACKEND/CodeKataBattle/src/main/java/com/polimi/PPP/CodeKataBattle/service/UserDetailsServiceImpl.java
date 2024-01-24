package com.polimi.PPP.CodeKataBattle.service;

import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidUserEmailException;
import com.polimi.PPP.CodeKataBattle.Exceptions.InvalidUserIdException;
import com.polimi.PPP.CodeKataBattle.Model.RoleEnunm;
import com.polimi.PPP.CodeKataBattle.Model.User;
import com.polimi.PPP.CodeKataBattle.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
                new InvalidUserEmailException("User with the email address " + email + " not found."));

        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().getName().name());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(authority)
                .build();
    }

    public UserDetails loadUserByUserId(Long id) {

        User user = repository.findById(id).orElseThrow(() ->
                new InvalidUserIdException("User with id " + id + " not found."));

        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().getName().name());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(authority)
                .build();
    }
}
