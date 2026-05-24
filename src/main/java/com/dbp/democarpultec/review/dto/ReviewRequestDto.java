package com.dbp.democarpultec.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequestDto {

    @NotNull
    private Long rideId;

    @NotNull
    private Long reviewerId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    private String comment;
}