package com.dbp.democarpultec.storage;

import com.dbp.democarpultec.exception.ImageStorageException;
import com.dbp.democarpultec.storage.dto.ImageStorageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ValidationException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ImageStorageService {

	private static final String INVALID_IMAGE_MESSAGE = "Formato de imagen no valido. Solo se permiten gif, webp, png, jpg y jpeg.";
	private static final String INVALID_PROFILE_IMAGE_MESSAGE = "Formato de imagen no valido. Solo se permiten webp y gif para la foto de perfil.";

	private final S3StorageService s3StorageService;

	static {
		ImageIO.scanForPlugins();
	}

	public ImageStorageResponseDto storeImages(List<MultipartFile> images) {
		if (images == null || images.isEmpty()) {
			return ImageStorageResponseDto.builder()
					.imageUrls(List.of())
					.build();
		}

		List<String> uploadedUrls = new ArrayList<>();
		int imageIndex = 1;

		for (MultipartFile image : images) {
			if (image == null || image.isEmpty()) {
				continue;
			}
			uploadedUrls.add(storeSingleImage(image, imageIndex));
			imageIndex++;
		}

		return ImageStorageResponseDto.builder()
				.imageUrls(uploadedUrls)
				.build();
	}

	public String replaceUserProfileImage(Long userId, MultipartFile image, String previousImageUrl) {
		if (image == null || image.isEmpty()) {
			throw new ValidationException("Debe subir una imagen de perfil");
		}

		String contentType = normalizeContentType(image.getContentType());
		String extension = resolveExtension(image.getOriginalFilename());
		String format = resolveProfileFormat(contentType, extension);
		if (format == null) {
			throw new ValidationException(INVALID_PROFILE_IMAGE_MESSAGE);
		}

		String key = buildProfileImageKey(userId, format);
		String uploadedUrl;
		try {
			uploadedUrl = s3StorageService.upload(key, image.getBytes(), "image/" + format);
		} catch (IOException ex) {
			throw new ImageStorageException("Unable to read profile image content", ex);
		}

		if (previousImageUrl != null && !previousImageUrl.isBlank() && !previousImageUrl.equals(uploadedUrl)) {
			try {
				s3StorageService.deleteByUrl(previousImageUrl);
			} catch (RuntimeException ex) {
				throw new ImageStorageException("Unable to delete previous profile image", ex);
			}
		}

		return uploadedUrl;
	}

	private String storeSingleImage(MultipartFile image, int imageIndex) {
		String contentType = normalizeContentType(image.getContentType());
		String extension = resolveExtension(image.getOriginalFilename());
		String format = resolveFormat(contentType, extension);

		if (format == null) {
			throw new ValidationException(INVALID_IMAGE_MESSAGE);
		}

		return switch (format) {
			case "gif" -> uploadAsIs(image, imageIndex, "gif", "image/gif");
			case "webp" -> uploadAsIs(image, imageIndex, "webp", "image/webp");
			case "png", "jpeg" -> uploadAsWebp(image, imageIndex);
			default -> throw new ValidationException(INVALID_IMAGE_MESSAGE);
		};
	}

	private String uploadAsIs(MultipartFile image, int imageIndex, String extension, String contentType) {
		try {
			String key = buildKey(imageIndex, extension);
			return s3StorageService.upload(key, image.getBytes(), contentType);
		} catch (IOException ex) {
			throw new ImageStorageException("Unable to read image content", ex);
		}
	}

	private String uploadAsWebp(MultipartFile image, int imageIndex) {
		try {
			BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image.getBytes()));
			if (bufferedImage == null) {
				throw new ValidationException(INVALID_IMAGE_MESSAGE);
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			boolean written = ImageIO.write(bufferedImage, "webp", outputStream);
			if (!written) {
				throw new ImageStorageException("WEBP encoder is not available");
			}

			String key = buildKey(imageIndex, "webp");
			return s3StorageService.upload(key, outputStream.toByteArray(), "image/webp");
		} catch (IOException ex) {
			throw new ImageStorageException("Unable to transform image", ex);
		}
	}

	private String buildKey(int imageIndex, String extension) {
		return "images/imagen" + imageIndex + "." + extension;
	}

	private String buildProfileImageKey(Long userId, String extension) {
		return "user-" + userId + "-profile/" + System.currentTimeMillis() + "." + extension;
	}

	private String resolveFormat(String contentType, String extension) {
		if ("image/gif".equals(contentType) || "gif".equals(extension)) {
			return "gif";
		}
		if ("image/webp".equals(contentType) || "webp".equals(extension)) {
			return "webp";
		}
		if ("image/png".equals(contentType) || "image/x-png".equals(contentType) || "png".equals(extension)) {
			return "png";
		}
		if ("image/jpeg".equals(contentType) || "image/jpg".equals(contentType)
				|| "image/pjpeg".equals(contentType)
				|| "jpeg".equals(extension) || "jpg".equals(extension)) {
			return "jpeg";
		}
		if (contentType != null && contentType.startsWith("image/")) {
			return switch (extension) {
				case "gif", "webp", "png", "jpeg", "jpg" -> resolveFormat(null, extension);
				default -> null;
			};
		}
		return null;
	}

	private String resolveProfileFormat(String contentType, String extension) {
		if ("image/webp".equals(contentType) || "webp".equals(extension)) {
			return "webp";
		}
		if ("image/gif".equals(contentType) || "gif".equals(extension)) {
			return "gif";
		}
		return null;
	}

	private String normalizeContentType(String contentType) {
		if (contentType == null) {
			return null;
		}
		return contentType.toLowerCase(Locale.ROOT);
	}

	private String resolveExtension(String originalFilename) {
		if (originalFilename == null || originalFilename.isBlank()) {
			return null;
		}
		int lastDot = originalFilename.lastIndexOf('.');
		if (lastDot < 0 || lastDot == originalFilename.length() - 1) {
			return null;
		}
		return originalFilename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
	}
}

