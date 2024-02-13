package com.polimi.PPP.CodeKataBattle.Controller;

import com.polimi.PPP.CodeKataBattle.DTOs.*;
import com.polimi.PPP.CodeKataBattle.Security.JwtHelper;
import com.polimi.PPP.CodeKataBattle.Utilities.NotificationProvider;
import com.polimi.PPP.CodeKataBattle.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;


@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtHelper jwtHelper;

    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtHelper jwtHelper) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtHelper = jwtHelper;
    }

    @PostMapping(value = "/login")
    public ResponseEntity<UserLoggedDTO> login(@RequestBody @Valid UserLoginDTO request) {
        log.warn("User {} logging in with password {}", request.getEmail(), request.getPassword());
        
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        UserDTO userDTO = userService.findByEmail(request.getEmail());
        String token = this.jwtHelper.generateToken(userDTO.getId(), userDTO.getRole().getName());
        return ResponseEntity.ok(new UserLoggedDTO(request.getEmail(), token, userDTO.getRole().getName()));
    }

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody @Valid UserCreationDTO requestDto) {
        userService.createUser(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
