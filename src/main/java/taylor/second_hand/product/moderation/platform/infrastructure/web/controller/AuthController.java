package taylor.second_hand.product.moderation.platform.infrastructure.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import taylor.second_hand.product.moderation.platform.domain.model.User;
import taylor.second_hand.product.moderation.platform.domain.port.out.UserRepository;
import taylor.second_hand.product.moderation.platform.infrastructure.web.dto.request.LoginRequest;
import taylor.second_hand.product.moderation.platform.infrastructure.web.dto.response.LoginResponse;
import taylor.second_hand.product.moderation.platform.infrastructure.web.dto.response.UserDto;
import taylor.second_hand.product.moderation.platform.infrastructure.web.security.JwtService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalStateException("User not found after successful authentication"));

        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(new LoginResponse(
                token,
                new UserDto(user.getId(), user.getUsername(), user.getRole().name())
        ));
    }
}
