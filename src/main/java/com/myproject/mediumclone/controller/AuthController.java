package com.myproject.mediumclone.controller;

import com.myproject.mediumclone.constant.ERole;
import com.myproject.mediumclone.dto.JwtResponse;
import com.myproject.mediumclone.dto.LoginRequestDTO;
import com.myproject.mediumclone.dto.RefreshTokenRequest;
import com.myproject.mediumclone.dto.SignupRequestDTO;
import com.myproject.mediumclone.dto.TokenRefreshResponse;
import com.myproject.mediumclone.exception.TokenRefreshException;
import com.myproject.mediumclone.model.RefreshToken;
import com.myproject.mediumclone.model.Role;
import com.myproject.mediumclone.model.User;
import com.myproject.mediumclone.repository.RoleRepository;
import com.myproject.mediumclone.repository.UserRepository;
import com.myproject.mediumclone.security.RefreshTokenService;
import com.myproject.mediumclone.security.UserDetailsImpl;
import com.myproject.mediumclone.security.UserDetailsServiceImpl;
import com.myproject.mediumclone.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @NotNull @RequestBody LoginRequestDTO loginRequestDTO) throws Exception {

        try {
            authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequestDTO.getUsername(), loginRequestDTO.getPassword()));

        } catch (BadCredentialsException e) {
            throw new Exception("Incorrect username or password", e);
        }

        final UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(loginRequestDTO.getUsername());
        String jwt = jwtUtil.generateJwtToken(userDetails);

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDetails.getId(),
                loginRequestDTO.getUsername(),
                userDetails.getEmail(),
                roles,
                refreshToken.getToken()
        ));
    }


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequestDTO signupRequestDTO) {

        if (userRepository.existsByUsername(signupRequestDTO.getUsername())) {
            return ResponseEntity.badRequest()
                    .body("Error : Username is already taken!");
        }

        if (userRepository.existsByEmail(signupRequestDTO.getEmail())) {
            return ResponseEntity.badRequest()
                    .body("Error: Email is already in use!");
        }

        User user = new User();
        user.setUsername(signupRequestDTO.getUsername());
        user.setEmail(signupRequestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequestDTO.getPassword()));

        Set<String> strRoles = signupRequestDTO.getRole();
        Set<Role> roles = new HashSet<>();

        if (CollectionUtils.isEmpty(strRoles)) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(
                            () -> new RuntimeException("Error: Role is not found")
                    );
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }
        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @NotNull @RequestBody RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtil.generateTokenFromUsername(user.getUsername());
                    return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
                })
                .orElseThrow(
                        () -> new TokenRefreshException(requestRefreshToken, "Refresh token is invalid!")
                );
    }

}
