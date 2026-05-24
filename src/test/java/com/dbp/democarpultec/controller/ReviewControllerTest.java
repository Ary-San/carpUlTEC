package com.dbp.democarpultec.controller;

import com.dbp.democarpultec.config.jwt.JwtAuthenticationFilter;
import com.dbp.democarpultec.review.controller.ReviewController;
import com.dbp.democarpultec.review.dto.ReviewResponseDto;
import com.dbp.democarpultec.review.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private ReviewResponseDto buildResponse() {
        return ReviewResponseDto.builder()
                .id(1L)
                .rideId(10L)
                .vehicleId(20L)
                .reviewerId(30L)
                .rating(5)
                .comment("Muy buen viaje")
                .createdAt(LocalDateTime.of(2026, 5, 24, 10, 30))
                .build();
    }

    @Test
    void shouldReturnPaginatedReviews() throws Exception {
        when(reviewService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(buildResponse())));

        mockMvc.perform(get("/api/reviews?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].rideId").value(10))
                .andExpect(jsonPath("$.content[0].comment").value("Muy buen viaje"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(reviewService).findAll(any(Pageable.class));
    }
}