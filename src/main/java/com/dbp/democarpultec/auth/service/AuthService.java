package com.dbp.democarpultec.auth.service;

import com.dbp.democarpultec.auth.dto.AuthRequestDto;
import com.dbp.democarpultec.auth.dto.AuthResponseDto;
import com.dbp.democarpultec.auth.dto.RegistreRequestDto;
import com.dbp.democarpultec.config.jwt.JwtService;
import com.dbp.democarpultec.exception.UserVerificationRequiredException;
import com.dbp.democarpultec.user.domain.User;
import com.dbp.democarpultec.user.dto.UserRequestDto;
import com.dbp.democarpultec.user.dto.UserResponseDto;
import com.dbp.democarpultec.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserService userService;
	private final JwtService jwtService;

	@Transactional
	public AuthResponseDto register(RegistreRequestDto request) {
		UserRequestDto userRequest = UserRequestDto.builder()
				.name(request.getName())
				.lastName(request.getLastName())
				.email(request.getEmail())
				.phone(request.getPhone())
				.studentCode(request.getStudentCode())
				.career(request.getCareer())
				.cycle(request.getCycle())
				.rating(request.getRating())
				.roles(request.getRoles())
				.build();

		UserResponseDto response = userService.create(userRequest);
		User user = userService.findEntityById(response.getId());
		return buildResponse(user);
	}

	public AuthResponseDto login(AuthRequestDto request) {
		User user = userService.findEntityByEmail(request.getEmail());
		if (!user.isVerified()) {
			throw new UserVerificationRequiredException("User must be verified before using ride features");
		}
		return buildResponse(user);
	}

	private AuthResponseDto buildResponse(User user) {
		return AuthResponseDto.builder()
				.token(jwtService.generateToken(user))
				.userId(user.getId())
				.email(user.getEmail())
				.verified(user.isVerified())
				.roles(user.getRoles())
				.build();
	}
}
