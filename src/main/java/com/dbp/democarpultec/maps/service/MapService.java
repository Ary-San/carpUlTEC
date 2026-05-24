package com.dbp.democarpultec.maps.service;

import com.dbp.democarpultec.maps.domain.MapRideMatchType;
import com.dbp.democarpultec.maps.dto.MapLocationRequestDto;
import com.dbp.democarpultec.maps.dto.NearbyRideDto;
import com.dbp.democarpultec.maps.dto.NearbyRidesResponseDto;
import com.dbp.democarpultec.ride.domain.Ride;
import com.dbp.democarpultec.ride.domain.RideDirection;
import com.dbp.democarpultec.ride.domain.RideStatus;
import com.dbp.democarpultec.ride.repository.RideRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.Normalizer;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class MapService {

    private static final String REFERENCE_NAME = "UTEC";
    private static final Set<String> UTEC_KEYWORDS = Set.of(
            "utec",
            "universidad de ingenieria y tecnologia",
            "universidad de ingeniería y tecnología"
    );
    private static final Pattern NON_ALPHA_NUMERIC = Pattern.compile("[^\\p{IsAlphabetic}\\p{IsDigit}]+");

    private final RideRepository rideRepository;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(5))
            .connectTimeout(Duration.ofSeconds(3))
            .readTimeout(Duration.ofSeconds(5))
            .build();

    @Value("${osmr.url}")
    private String osrmUrl;

    @Value("${maps.utec.latitude:-12.1352}")
    private double utecLatitude;

    @Value("${maps.utec.longitude:-77.0222}")
    private double utecLongitude;

    @Value("${maps.utec.search-radius-meters:3000}")
    private int defaultRadiusMeters;

    @Value("${maps.utec.max-results:20}")
    private int defaultMaxResults;

    public NearbyRidesResponseDto findNearbyRides(MapLocationRequestDto request) {
        int radiusMeters = request.getRadiusMeters() != null ? request.getRadiusMeters() : defaultRadiusMeters;
        int maxResults = request.getMaxResults() != null ? request.getMaxResults() : defaultMaxResults;
        double distanceToReferenceMeters = resolveDistanceToUtecMeters(request.getLat(), request.getLng());
        boolean withinRadius = distanceToReferenceMeters <= radiusMeters;

        List<NearbyRideDto> rides = withinRadius
                ? rideRepository.findAll().stream()
                .filter(this::isJoinableUtecRide)
                .sorted(Comparator
                        .comparing(Ride::getScheduledAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Ride::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(maxResults)
                .map(ride -> toNearbyRideDto(ride, distanceToReferenceMeters))
                .toList()
                : List.of();

        return NearbyRidesResponseDto.builder()
                .referenceName(REFERENCE_NAME)
                .userLat(request.getLat())
                .userLng(request.getLng())
                .referenceLat(utecLatitude)
                .referenceLng(utecLongitude)
                .distanceToReferenceMeters(distanceToReferenceMeters)
                .radiusMeters(radiusMeters)
                .withinRadius(withinRadius)
                .rides(rides)
                .build();
    }

    private boolean isJoinableUtecRide(Ride ride) {
        if (ride == null) {
            return false;
        }
        if (ride.getStatus() == RideStatus.COMPLETED) {
            return false;
        }
        if (ride.getAvailableSeats() == null || ride.getAvailableSeats() <= 0) {
            return false;
        }
        return isUtecLocation(ride.getOrigin()) || isUtecLocation(ride.getDestination());
    }

    private NearbyRideDto toNearbyRideDto(Ride ride, double distanceToReferenceMeters) {
        return NearbyRideDto.builder()
                .rideId(ride.getId())
                .driverId(ride.getDriver().getId())
                .vehicleId(ride.getVehicle().getId())
                .origin(ride.getOrigin())
                .destination(ride.getDestination())
                .scheduledAt(ride.getScheduledAt())
                .status(ride.getStatus())
                .direction(ride.getDirection())
                .availableSeats(ride.getAvailableSeats())
                .matchType(resolveMatchType(ride))
                .distanceToUtecMeters(distanceToReferenceMeters)
                .build();
    }

    private MapRideMatchType resolveMatchType(Ride ride) {
        boolean originUtec = isUtecLocation(ride.getOrigin());
        boolean destinationUtec = isUtecLocation(ride.getDestination());

        if (originUtec && destinationUtec) {
            return MapRideMatchType.ROUND_TRIP_UTEC;
        }
        if (ride.getDirection() == RideDirection.TO_UNIVERSITY || destinationUtec) {
            return MapRideMatchType.TO_UTEC;
        }
        if (ride.getDirection() == RideDirection.FROM_UNIVERSITY || originUtec) {
            return MapRideMatchType.FROM_UTEC;
        }
        return MapRideMatchType.UTEC_KEYWORD_MATCH;
    }

    private boolean isUtecLocation(String location) {
        if (location == null || location.isBlank()) {
            return false;
        }
        String normalizedLocation = normalize(location);
        return UTEC_KEYWORDS.stream().anyMatch(normalizedLocation::contains);
    }

    private String normalize(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
        return NON_ALPHA_NUMERIC.matcher(normalized).replaceAll(" ").trim();
    }

    private double resolveDistanceToUtecMeters(double userLat, double userLng) {
        try {
            String routeUrl = buildRouteUrl(userLat, userLng);
            Request request = new Request.Builder()
                    .url(routeUrl)
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    log.debug("OSRM route request failed, using haversine fallback. status={}", response.code());
                    return haversineDistanceMeters(userLat, userLng, utecLatitude, utecLongitude);
                }

                String responseBody = response.body().string();
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode routes = root.path("routes");
                if (routes.isArray() && !routes.isEmpty()) {
                    return routes.get(0).path("distance").asDouble(haversineDistanceMeters(userLat, userLng, utecLatitude, utecLongitude));
                }
            }
        } catch (IOException | IllegalArgumentException ex) {
            log.debug("Unable to resolve OSRM route distance, using haversine fallback", ex);
        }

        return haversineDistanceMeters(userLat, userLng, utecLatitude, utecLongitude);
    }

    private String buildRouteUrl(double userLat, double userLng) {
        String baseUrl = osrmUrl == null ? "" : osrmUrl.trim();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return String.format(
                Locale.ROOT,
                "%s/route/v1/driving/%f,%f;%f,%f?overview=false&alternatives=false&steps=false",
                baseUrl,
                userLng,
                userLat,
                utecLongitude,
                utecLatitude
        );
    }

    private double haversineDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double earthRadiusMeters = 6_371_000.0;
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusMeters * c;
    }
}