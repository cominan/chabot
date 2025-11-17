package com.hung.chatbot.DTO;

import com.hung.chatbot.model.User;
import com.hung.chatbot.model.UserInfo;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;
    
    @Pattern(regexp = "(^$|[0-9]{10,15})", message = "Phone number should be between 10-15 digits")
    private String phoneNumber;
    
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    private String address;

    public User toUser() {
        User user = new User();
        user.setUsername(this.username);
        user.setEmail(this.email);
        user.setRole(User.Role.USER);
        return user;
    }

    public UserInfo toUserInfo(User user) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUser(user);
        userInfo.setFirstName(this.firstName);
        userInfo.setLastName(this.lastName);
        userInfo.setPhoneNumber(this.phoneNumber);
        userInfo.setDateOfBirth(this.dateOfBirth);
        userInfo.setAddress(this.address);
        return userInfo;
    }
}
