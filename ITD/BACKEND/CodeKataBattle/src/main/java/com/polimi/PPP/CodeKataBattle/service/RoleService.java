package com.polimi.PPP.CodeKataBattle.service;

import com.polimi.PPP.CodeKataBattle.DTOs.RoleDTO;
import com.polimi.PPP.CodeKataBattle.Model.Role;
import com.polimi.PPP.CodeKataBattle.Model.RoleEnum;
import com.polimi.PPP.CodeKataBattle.Repositories.RoleRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ModelMapper modelMapper;

    public RoleDTO findByName(RoleEnum name) {
        Role role = roleRepository.findByName(name).orElse(null);
        if (role != null) {
            return modelMapper.map(role, RoleDTO.class);
        }
        return null;
    }

    public RoleDTO findById(Long id) {
        Role role = roleRepository.findById(id).orElse(null);
        if (role != null) {
            return modelMapper.map(role, RoleDTO.class);
        }
        return null;
    }

    // Other service methods
}
