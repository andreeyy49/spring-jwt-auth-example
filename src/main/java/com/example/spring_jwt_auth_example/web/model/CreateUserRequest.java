package com.example.spring_jwt_auth_example.web.model;

import com.example.spring_jwt_auth_example.entity.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    private String username;

    private String email;

    private Set<RoleType> roles;

    private String password;

}
