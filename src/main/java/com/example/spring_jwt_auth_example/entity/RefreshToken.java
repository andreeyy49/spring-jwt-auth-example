package com.example.spring_jwt_auth_example.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.Instant;

@RedisHash("refresh_tokens") // Аннотация говорит spring-data-redis, что сущность RefreshToken д.б. сохранена в Redis как хэш-структура. refresh_tokens - это имя хэша, в котором будут храниться данные сущности.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    /*
    @Indexed - поле, помеченное данной аннотацией, будет использоваться для поиска и индексирования данных в Redis.
    Т.е. поля будут индексированными, что позволит быстро и эффективно искать и получать записи по этим полям.
     */

    @Id // см. импорт - идентификатор сущности (уникален)
    @Indexed // см. импорт.
    private Long id;

    @Indexed
    private Long userId;

    @Indexed
    private String token;

    @Indexed
    private Instant expiryDate;

}
