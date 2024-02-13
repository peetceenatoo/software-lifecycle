package com.polimi.PPP.CodeKataBattle.Assemblers;

import com.polimi.PPP.CodeKataBattle.DTOs.UserCreationDTO;
import com.polimi.PPP.CodeKataBattle.DTOs.UserDTO;
import com.polimi.PPP.CodeKataBattle.Model.Role;
import com.polimi.PPP.CodeKataBattle.Model.RoleEnum;
import com.polimi.PPP.CodeKataBattle.Model.User;
import com.polimi.PPP.CodeKataBattle.Repositories.RoleRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserAssembler {

    private final RoleRepository roleRepository;

    private final ModelMapper modelMapper;

    @Autowired
    public UserAssembler(RoleRepository roleRepository, ModelMapper modelMapper) {
        this.roleRepository = roleRepository;
        this.modelMapper = modelMapper;
    }

    public User toEntity(UserCreationDTO dto) {
        User user = new User();
        // Set simple properties

        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setLinkBio(dto.getLinkBio());

        if (dto.getRoleName() != null) {
            Role role = null;
            try{
                role = roleRepository.findByName(RoleEnum.valueOf(dto.getRoleName())).orElseThrow(() -> new IllegalArgumentException("Invalid role provided"));
            }catch (IllegalArgumentException ex){
                throw new IllegalArgumentException("Invalid role provided");
            }

            user.setRole(role);
        }

        return user;
    }

    public UserDTO toDto(User user) {
        UserDTO dto = new UserDTO();
        // Map properties from user to dto

        modelMapper.map(user, dto);

        return dto;
    }
}

