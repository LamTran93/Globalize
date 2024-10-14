package com.project.booking_platform.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OwnerDTO {
    String id;
    String firstName;
    String lastName;
    String email;
    String userName;
    String password;
    String idNumber;
    String phoneNumber;
    String taxNumber;
    Boolean isVerified;
}
