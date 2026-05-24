package com.dbp.democarpultec.storage.controller;

import com.dbp.democarpultec.storage.ImageStorageService;
import com.dbp.democarpultec.storage.dto.ImageStorageRequestDto;
import com.dbp.democarpultec.storage.dto.ImageStorageResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class ImageStorageController {

	private final ImageStorageService imageStorageService;

	@PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ImageStorageResponseDto> storeImages(@Valid @ModelAttribute ImageStorageRequestDto request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(imageStorageService.storeImages(request.getImages()));
	}
}