package com.dbp.democarpultec.storage;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

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

	@PreDestroy
	public void close() {
		s3Client.close();
	}
}
