package com.dbp.democarpultec.auth.dto;

import com.dbp.democarpultec.user.domain.Carreras;
import com.dbp.democarpultec.user.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistreRequestDto {

	@NotBlank
	private String name;

	@NotBlank
	private String lastName;

	@NotBlank
	@Email
	private String email;

	private String phone;
	private String studentCode;
	private Carreras career;
	private Integer cycle;
	private Double rating;
	private Set<UserRole> roles;
}
