package com.redmath.admin.dto;

import com.redmath.account.Role;
import lombok.Getter;

@Getter
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String address;
    private Role role;

    public UserResponse() {
    }

    public UserResponse(Long id,
                        String name,
                        String email,
                        String address,
                        Role role) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.address = address;
        this.role = role;
    }

}
