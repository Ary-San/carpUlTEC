package com.dbp.democarpultec.review.service;

import com.dbp.democarpultec.exception.ReviewConflictException;
import com.dbp.democarpultec.exception.ReviewForbiddenException;
import com.dbp.democarpultec.exception.ReviewValidationException;
import com.dbp.democarpultec.pickup.domain.PickUp;
import com.dbp.democarpultec.pickup.domain.PickUpStatus;
import com.dbp.democarpultec.pickup.repository.PickUpRepository;
import com.dbp.democarpultec.review.domain.Review;
import com.dbp.democarpultec.review.dto.ReviewRequestDto;
import com.dbp.democarpultec.review.dto.ReviewResponseDto;
import com.dbp.democarpultec.review.repository.ReviewRepository;
import com.dbp.democarpultec.ride.domain.Ride;
import com.dbp.democarpultec.ride.domain.RideStatus;
import com.dbp.democarpultec.ride.service.RideService;
import com.dbp.democarpultec.user.domain.User;
import com.dbp.democarpultec.user.service.UserService;
import com.dbp.democarpultec.vehicle.domain.Vehicle;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RideService rideService;
    private final UserService userService;
    private final PickUpRepository pickUpRepository;

    public List<ReviewResponseDto> findAll() {
        return reviewRepository.findAll().stream().map(this::toResponseDto).toList();
    }

    public ReviewResponseDto findById(@NonNull Long id) {
        return toResponseDto(findEntityById(id));
    }

    @Transactional
    public ReviewResponseDto create(@NonNull ReviewRequestDto dto) {
        validateRating(dto.getRating());
        validateComment(dto.getComment());

        User reviewer = userService.findVerifiedEntityById(dto.getReviewerId());
        Ride ride = rideService.findEntityById(dto.getRideId());

        if (ride.getStatus() != RideStatus.COMPLETED) {
            throw new ReviewConflictException("Reviews can only be created when the ride is completed");
        }

        if (ride.getDriver() != null && reviewer.getId().equals(ride.getDriver().getId())) {
            throw new ReviewForbiddenException("Drivers cannot review their own rides or vehicles");
        }

        PickUp pickUp = pickUpRepository.findByRide_IdAndPassenger_Id(ride.getId(), reviewer.getId())
                .orElseThrow(() -> new ReviewForbiddenException("The reviewer must have participated in the ride"));

        if (pickUp.getStatus() != PickUpStatus.DROPPED_OFF) {
            throw new ReviewForbiddenException("Only passengers who completed the ride can leave a review");
        }

        Vehicle vehicle = ride.getVehicle();
        if (vehicle == null) {
            throw new ReviewValidationException("Ride must have an associated vehicle");
        }

        if (reviewRepository.existsByReviewer_IdAndRide_Id(reviewer.getId(), ride.getId())
                || reviewRepository.existsByReviewer_IdAndRide_IdAndVehicle_Id(reviewer.getId(), ride.getId(), vehicle.getId())) {
            throw new ReviewConflictException("A review for this ride and vehicle already exists for the reviewer");
        }

        Review review = Review.builder()
                .reviewer(reviewer)
                .ride(ride)
                .vehicle(vehicle)
                .rating(dto.getRating())
                .comment(normalizeComment(dto.getComment()))
                .build();

        return toResponseDto(reviewRepository.save(Objects.requireNonNull(review)));
    }

    @Transactional
    public void delete(@NonNull Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new EntityNotFoundException("Review not found with id " + id);
        }
        reviewRepository.deleteById(id);
    }

    public Review findEntityById(@NonNull Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with id " + id));
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new ReviewValidationException("Rating must be between 1 and 5");
        }
    }

    private void validateComment(String comment) {
        if (!StringUtils.hasText(comment)) {
            return;
        }

        long words = Arrays.stream(comment.trim().split("\\s+"))
                .filter(StringUtils::hasText)
                .count();

        if (words > 50) {
            throw new ReviewValidationException("Comment cannot exceed 50 words");
        }
    }

    private String normalizeComment(String comment) {
        return StringUtils.hasText(comment) ? comment.trim() : null;
    }

    private ReviewResponseDto toResponseDto(Review review) {
        return ReviewResponseDto.builder()
                .id(review.getId())
                .rideId(review.getRide().getId())
                .vehicleId(review.getVehicle().getId())
                .reviewerId(review.getReviewer().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}