package com.project.booking_platform.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterOwnerInfo {
    String firstName;
    String lastName;
    String email;
    String username;
    String password;
    String idNumber;
    String phoneNumber;
    String taxNumber;
}
