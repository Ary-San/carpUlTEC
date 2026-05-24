package com.dbp.democarpultec.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageStorageResponseDto {

	private String petPid;
	private List<String> imageUrls;
}