package com.dbp.democarpultec.review.repository;

import com.dbp.democarpultec.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRide_IdOrderByCreatedAtDesc(Long rideId);

    List<Review> findByReviewer_IdOrderByCreatedAtDesc(Long reviewerId);

    List<Review> findByVehicle_IdOrderByCreatedAtDesc(Long vehicleId);

    boolean existsByReviewer_IdAndRide_Id(Long reviewerId, Long rideId);

    boolean existsByReviewer_IdAndRide_IdAndVehicle_Id(Long reviewerId, Long rideId, Long vehicleId);
}