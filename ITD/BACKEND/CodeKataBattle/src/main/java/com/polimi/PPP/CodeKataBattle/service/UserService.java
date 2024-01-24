package com.polimi.PPP.CodeKataBattle.service;

import com.polimi.PPP.CodeKataBattle.Assemblers.UserAssembler;
import com.polimi.PPP.CodeKataBattle.DTOs.RoleDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.UserCreationDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.UserDTO;
import com.polimi.PPP.CodeKataBattle.Exceptions.EmailAlreadyExistsException;
import com.polimi.PPP.CodeKataBattle.Exceptions.UsernameAlreadyExistsException;
import com.polimi.PPP.CodeKataBattle.Model.Role;
import com.polimi.PPP.CodeKataBattle.Model.User;
import com.polimi.PPP.CodeKataBattle.Repositories.RoleRepository;
import com.polimi.PPP.CodeKataBattle.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UserAssembler userAssembler;

    @Transactional
    public UserDTO createUser(UserCreationDTO userDTO) {

        String email = userDTO.getEmail();
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new EmailAlreadyExistsException(String.format("User with the email address '%s' already exists.", email));
        }

        existingUser = userRepository.findByUsername(userDTO.getUsername());
        if (existingUser.isPresent()) {
            throw new UsernameAlreadyExistsException(String.format("User with the username '%s' already exists.", userDTO.getUsername()));
        }

        User user = userAssembler.toEntity(userDTO); //modelMapper not able to map Role, thus using an assembler
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDTO.class);
    }

    public UserDTO findByUsername(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            return modelMapper.map(user, UserDTO.class);
        }
        return null;
    }

    public UserDTO findByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            return modelMapper.map(user, UserDTO.class);
        }
        return null;
    }

    public UserDTO findById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            return modelMapper.map(user, UserDTO.class);
        }
        return null;
    }

    public RoleDTO findRoleByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            return modelMapper.map(user.getRole(), RoleDTO.class);
        }
        return null;
    }

    // Other service methods
}
