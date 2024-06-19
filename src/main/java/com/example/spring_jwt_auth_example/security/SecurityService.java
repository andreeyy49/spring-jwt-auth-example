package com.example.spring_jwt_auth_example.security;

import com.example.spring_jwt_auth_example.entity.RefreshToken;
import com.example.spring_jwt_auth_example.entity.User;
import com.example.spring_jwt_auth_example.exception.RefreshTokenException;
import com.example.spring_jwt_auth_example.repository.UserRepository;
import com.example.spring_jwt_auth_example.security.jwt.JwtUtils;
import com.example.spring_jwt_auth_example.service.RefreshTokenService;
import com.example.spring_jwt_auth_example.web.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final AuthenticationManager authenticationManager;  // сист.

    private final JwtUtils jwtUtils;                            // отвечает за генерацию Access Token

    private final RefreshTokenService refreshTokenService;      // отвечает за работу с Refresh Token

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;              // сист.

    /*
    Метод authenticateUser отвечает за логин пользователя,  Занесение его в контекст Spring Security, за выдачу Access- и Refresh-токенов клиенту.
    Метод .authenticate выполняется для аутентификации пользователя.
    Входные данные пользователя проверяются на соответствие введенным данным в системе.
    SecurityContextHolder - Управляет данными аутентификации текущего потока выполнения
    */
    public AuthResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        ));

        SecurityContextHolder.getContext().setAuthentication(authentication); // Если аутентификация успешна, то результат сохраняется в SecurityContextHolder

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal(); // Получаем детали аутентифицированного пользователя

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList(); // Получаем роли

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId()); // Создаём новый RefreshToken для пользователя

        return AuthResponse.builder()
                .id(userDetails.getId())
                .token(jwtUtils.generateJwtToken(userDetails))
                .refreshToken(refreshToken.getToken())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .roles(roles)
                .build(); // Содержит информацию об успешной аутентификации
    }

    /*
    Регистрация пользователя и сохранение его в БД
    */
    public void register(CreateUserRequest createUserRequest) {
        var user = User.builder()
                .username(createUserRequest.getUsername())
                .email(createUserRequest.getEmail())
                .password(passwordEncoder.encode(createUserRequest.getPassword()))
                .build();
        user.setRoles(createUserRequest.getRoles());

        userRepository.save(user);
    }

    /*
    Получаем от пользователя текущий RefreshToken и пытаемся найти его в Redis.
    Если находим, то проверяем поле на результат валидности.
    Если токен валиден, то получаем из него id пользователя.
    Ищем пользователя в БД postgres по его Id.
    Генерируем для него новые Access- и Refresh-токены.
    Возвращаем результат пользователю.
    */
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByRefreshToken(requestRefreshToken)
                .map(refreshTokenService::checkRefreshToken)
                .map(RefreshToken::getUserId)
                .map(userId -> {
                    User tokenOwner = userRepository.findById(userId).orElseThrow(() ->
                            new RefreshTokenException("Exception trying to get token for userId: " + userId));
                    String token = jwtUtils.generateTokenFromUsername(tokenOwner.getUsername());

                    return new RefreshTokenResponse(token, refreshTokenService.createRefreshToken(userId).getToken());
                }).orElseThrow(() -> new RefreshTokenException(requestRefreshToken, "Refresh token not found"));
    }

    /*
    Метод logout() удаляет из системы Refresh Token пользователя, чтобы он не смог сделать refresh и получить новый token.
    */
    public void logout() {
        var currentPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentPrincipal instanceof AppUserDetails userDetails) {
            Long userId = userDetails.getId();

            refreshTokenService.deleteByUserId(userId);
        }
    }

}
