package com.dbp.democarpultec.user.controller;

import com.dbp.democarpultec.user.dto.UserResponseDto;
import com.dbp.democarpultec.user.dto.UserUpdateMeRequestDto;
import com.dbp.democarpultec.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MeController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponseDto findMe() {
        return userService.findByCurrentUserId(currentUserId());
    }

    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponseDto updateMe(@Valid @ModelAttribute UserUpdateMeRequestDto request) {
        return userService.updateProfileImage(currentUserId(), request.getProfileImage());
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new AccessDeniedException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof String principalAsString) {
            try {
                return Long.valueOf(principalAsString);
            } catch (NumberFormatException ex) {
                throw new AccessDeniedException("Invalid authentication principal", ex);
            }
        }
        if (principal instanceof Number principalAsNumber) {
            return principalAsNumber.longValue();
        }

        throw new AccessDeniedException("Invalid authentication principal");
    }
}