package com.dbp.democarpultec.auth.dto;

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
public class AuthResponseDto {

	private String token;
	private Long userId;
	private String email;
	private boolean verified;
	private Set<UserRole> roles;
}
