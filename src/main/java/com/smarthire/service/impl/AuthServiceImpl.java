package com.smarthire.service.impl;

import com.smarthire.dto.AuthResponseDto;
import com.smarthire.dto.LoginDto;
import com.smarthire.dto.RegisterDto;
import com.smarthire.dto.UserDto;
import com.smarthire.entity.Role;
import com.smarthire.entity.User;
import com.smarthire.exception.ValidationException;
import com.smarthire.repository.RoleRepository;
import com.smarthire.repository.UserRepository;
import com.smarthire.security.JwtTokenProvider;
import com.smarthire.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public AuthResponseDto login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(loginDto.getEmail()).orElseThrow(
                () -> new RuntimeException("User not found")
        );

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        userDto.setPhone(user.getPhone());
        userDto.setTitle(user.getTitle());
        userDto.setBio(user.getBio());
        userDto.setRoles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));

        return new AuthResponseDto(token, userDto);
    }

    @Override
    public String register(RegisterDto registerDto) {
        // check if email exists
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new ValidationException("Email is already registered!");
        }

        User user = new User();
        user.setName(registerDto.getName());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Set<Role> roles = new HashSet<>();
        
        String roleName = registerDto.getRole().toUpperCase();
        if (!roleName.equals("CANDIDATE") && !roleName.equals("RECRUITER")) {
            throw new ValidationException("Invalid role. Must be CANDIDATE or RECRUITER");
        }
        
        Role userRole = roleRepository.findByName("ROLE_" + roleName)
                .orElseGet(() -> {
                    Role newRole = new Role("ROLE_" + roleName);
                    return roleRepository.save(newRole);
                });
        
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);

        return "User registered successfully!";
    }
}
