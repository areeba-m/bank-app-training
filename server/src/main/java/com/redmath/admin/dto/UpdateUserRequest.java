package com.redmath.admin.dto;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateUserRequest {

    private String name;

    private String address;

    public UpdateUserRequest() {
    }

}
