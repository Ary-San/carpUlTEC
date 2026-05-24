package com.dbp.democarpultec.storage;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
public class S3StorageService {

	private final S3Client s3Client;
	private final String bucketName;

	public S3StorageService(
			@Value("${BUCKET_NAME}") String bucketName,
			@Value("${AWS_REGION:us-east-1}") String awsRegion
	) {
		this.bucketName = bucketName;
		this.s3Client = S3Client.builder()
				.region(Region.of(awsRegion))
				.credentialsProvider(DefaultCredentialsProvider.create())
				.build();
	}

	public String upload(String key, byte[] content, String contentType) {
		s3Client.putObject(
				builder -> builder.bucket(bucketName).key(key).contentType(contentType),
				RequestBody.fromBytes(content)
		);
		return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(key)).toExternalForm();
	}

	public void deleteByKey(String key) {
		s3Client.deleteObject(builder -> builder.bucket(bucketName).key(key));
	}

	public void deleteByUrl(String url) {
		if (url == null || url.isBlank()) {
			return;
		}

		URI uri = URI.create(url);
		String rawPath = uri.getRawPath();
		if (rawPath == null || rawPath.isBlank() || "/".equals(rawPath)) {
			return;
		}

		String key = rawPath.startsWith("/") ? rawPath.substring(1) : rawPath;
		deleteByKey(URLDecoder.decode(key, StandardCharsets.UTF_8));
	}

	@PreDestroy
	public void close() {
		s3Client.close();
	}
}
