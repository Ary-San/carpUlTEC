package com.dbp.democarpultec.user.dto;

import com.dbp.democarpultec.user.domain.Carreras;
import com.dbp.democarpultec.user.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private Long id;
    private String name;
    private String lastName;
    private String email;
    private String phone;
    private String studentCode;
    private Carreras career;
    private Integer cycle;
    private Double rating;
    private boolean verified;
    private Set<UserRole> roles;
}
