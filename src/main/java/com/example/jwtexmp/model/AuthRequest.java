package com.example.jwtexmp.model;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthRequest {

    private String username;
    private String password;

}
