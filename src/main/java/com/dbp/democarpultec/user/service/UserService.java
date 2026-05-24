package com.dbp.democarpultec.user.service;

import com.dbp.democarpultec.exception.UserVerificationRequiredException;
import com.dbp.democarpultec.storage.ImageStorageService;
import com.dbp.democarpultec.user.domain.User;
import com.dbp.democarpultec.user.domain.UserRole;
import com.dbp.democarpultec.user.dto.UserRequestDto;
import com.dbp.democarpultec.user.dto.UserResponseDto;
import com.dbp.democarpultec.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class UserService {

    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;

    public Page<UserResponseDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponseDto);
    }

    public UserResponseDto findById(@NonNull Long id) {
        return toResponseDto(findEntityById(id));
    }

    public UserResponseDto findByEmail(@NonNull String email) {
        return toResponseDto(findEntityByEmail(email));
    }

    public UserResponseDto findByCurrentUserId(@NonNull Long currentUserId) {
        return toResponseDto(findEntityById(currentUserId));
    }

    @Transactional
    public UserResponseDto create(UserRequestDto dto) {
        User user = new User();
        updateEntity(user, dto);
        user.setVerified(false);
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(defaultRoles());
        }
        User savedUser = userRepository.save(user);
        return toResponseDto(savedUser);
    }

    @Transactional
    public UserResponseDto update(@NonNull Long id, UserRequestDto dto) {
        User user = findEntityById(id);
        updateEntity(user, dto);
        User savedUser = userRepository.save(user);
        return toResponseDto(savedUser);
    }

    @Transactional
    public void delete(@NonNull Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id " + id);
        }
        userRepository.deleteById(id);
    }

    public @NonNull User findEntityById(@NonNull Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));
    }

    public @NonNull User findEntityByEmail(@NonNull String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email " + email));
    }

    public @NonNull User findEntityByVerificationCode(@NonNull String verificationCode) {
        return userRepository.findByVerificationCode(verificationCode)
                .orElseThrow(() -> new EntityNotFoundException("User not found with verification code " + verificationCode));
    }

    @Transactional
    public User assignVerificationCode(@NonNull Long id, @NonNull String verificationCode) {
        User user = findEntityById(id);
        user.setVerificationCode(verificationCode);
        return userRepository.save(user);
    }

    public @NonNull User findVerifiedEntityById(@NonNull Long id) {
        User user = findEntityById(id);
        if (!user.isVerified()) {
            throw new UserVerificationRequiredException("User must be verified to access ride features");
        }
        return user;
    }

    @Transactional
    public UserResponseDto verifyUser(@NonNull Long id) {
        User user = findEntityById(id);
        user.setVerified(true);
        user.setVerificationCode(null);
        User savedUser = userRepository.save(user);
        return toResponseDto(savedUser);
    }

    @Transactional
    public UserResponseDto verifyUserByCode(@NonNull String verificationCode) {
        User user = findEntityByVerificationCode(verificationCode);
        user.setVerified(true);
        user.setVerificationCode(null);
        User savedUser = userRepository.save(user);
        return toResponseDto(savedUser);
    }

    @Transactional
    public UserResponseDto updateProfileImage(@NonNull Long currentUserId, MultipartFile profileImage) {
        User user = findEntityById(currentUserId);
        String uploadedUrl = imageStorageService.replaceUserProfileImage(currentUserId, profileImage, user.getProfileImageUrl());
        user.setProfileImageUrl(uploadedUrl);
        User savedUser = userRepository.save(user);
        return toResponseDto(savedUser);
    }

    private void updateEntity(User user, UserRequestDto dto) {
        user.setName(dto.getName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStudentCode(dto.getStudentCode());
        user.setCareer(dto.getCareer());
        user.setCycle(dto.getCycle());
        user.setRating(dto.getRating());
        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            user.setRoles(new HashSet<>(dto.getRoles()));
        } else if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(defaultRoles());
        }
    }

    private Set<UserRole> defaultRoles() {
        Set<UserRole> roles = new HashSet<>();
        roles.add(UserRole.PASSENGER);
        roles.add(UserRole.DRIVER);
        return roles;
    }

    private UserResponseDto toResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .studentCode(user.getStudentCode())
                .career(user.getCareer())
                .cycle(user.getCycle())
                .rating(user.getRating())
                .profileImageUrl(user.getProfileImageUrl())
                .verified(user.isVerified())
                .roles(user.getRoles())
                .build();
    }

}
