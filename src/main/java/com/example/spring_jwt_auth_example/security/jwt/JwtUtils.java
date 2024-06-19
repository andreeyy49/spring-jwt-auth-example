package com.example.spring_jwt_auth_example.security.jwt;

import com.example.spring_jwt_auth_example.security.AppUserDetails;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;

@Component
@Slf4j
public class JwtUtils { // отвечает за генерацию Access Token

    @Value("${app.jwt.secret}")
    private String jwtSecret; // Будет использовано для подписи

    @Value("${app.jwt.tokenExpiration}")
    private Duration tokenExpiration;

    public String generateJwtToken(AppUserDetails userDetails) {
        return generateTokenFromUsername(userDetails.getUsername());
    }

    /*
    Данный код генерирует JSON Web-Token JWT на основе переданного имени пользователя.
    Jwts.builder() - создаёт объект для построения Jwt
    Этот токен может быть передан клиенту и использован для аутентификации и авторизации.
    */
    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .setSubject(username) // Устанавливает субъект токена (обычно это идентификатор пользователя)
                .setIssuedAt(new Date()) // Указываем текущую дату - время выдачи токена
                .setExpiration(new Date(new Date().getTime() + tokenExpiration.toMillis())) // Устанавливаем срок действия, время истечения
                .signWith(SignatureAlgorithm.HS512, jwtSecret) // Создаём подпись по определённому алгоритму, подписываем токен
                .compact();
    }

    /*
    Этот метод выполняет обратную операцию по сравнению с предыдущим методом.
    Данный метод извлекает имя Username из Jwt (токена), который был подписан с использованием того же секретного ключа.
    Если Jwt подписан некорректно или ключ подписей не совпадает, здесь может возникнуть исключение.
    .getSubject() - получает из тела субъект, который в данном контексте представляет имя пользователя.
    */
    public String getUsername(String token) {
        return Jwts.parser().setSigningKey(jwtSecret)
                .parseClaimsJws(token).getBody().getSubject();
    }

    /*
    Если Jwt подписан корректно и содержит допустимые данные, то эта операция будет успешной.
    Если валидация не удалась, код перехватывает различные исключения, которые могут возникнуть в процессе валидации.
    UnsupportedJwtException - например, Если получаем Jwt, который подписан с использованием алгоритма,
    не поддерживаемого этой библиотекой, то при попытке его валидации будет выброшено исключение.
    */
    public boolean validate(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) { // Неправильный ключ (подписть Jwt некорректна, подписи не совпадают)
            log.error("Invalid signature: {}", e.getMessage());
        } catch (MalformedJwtException e) { // Невалидный токен
            log.error("Invalid token: {}", e.getMessage());
        } catch (ExpiredJwtException e) { // Закончился срок действия токена
            log.error("Token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) { // Jwt использует неподдерживаемую функциональность
            log.error("Token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) { // строка с клаймсами пуста или некорректна
            log.error("Claims string is empty: {}", e.getMessage());
        }
        return false;
    }

}
