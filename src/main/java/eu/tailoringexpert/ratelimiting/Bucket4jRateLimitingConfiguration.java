/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2025 - 2026 Michael Bädorf and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package eu.tailoringexpert.ratelimiting;

import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.HOURS;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import eu.tailoringexpert.ratelimiting.domain.Bucket4JConfigProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.local.LocalBucketBuilder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Profile("bucket4j")
@Configuration
public class Bucket4jRateLimitingConfiguration {

    @Bean 
    Supplier<Bucket> bucketSupplier(Bucket4JConfigProperties config) {
        Function<String, ChronoUnit> chronoUnit = unit -> switch (unit.toLowerCase()) {
            case "seconds" -> ChronoUnit.SECONDS;
            case "minutes" -> ChronoUnit.MINUTES;
            case "hours" -> ChronoUnit.HOURS;
            case "days" -> ChronoUnit.DAYS;
            default -> throw new IllegalArgumentException("Unsupported unit: " + unit);
        };

        Function<eu.tailoringexpert.ratelimiting.domain.Bandwidth, Bandwidth> bandwidthConverter = bandwidthConfig -> {
            long time = bandwidthConfig.getTime();
            //ChronoUnit unit = chronoUnit.apply(bandwidthConfig.getUnit());
            ChronoUnit unit = bandwidthConfig.getUnit();
            Duration duration = Duration.of(time, unit);

            return switch (bandwidthConfig.getRefillSpeed().toLowerCase()) {
                case "interval" -> Bandwidth.builder()
                        .capacity(bandwidthConfig.getCapacity())
                        .refillIntervally(bandwidthConfig.getCapacity(), duration)
                        .build();

                case "greedy" -> Bandwidth.builder()
                        .capacity(bandwidthConfig.getCapacity())
                        .refillGreedy(bandwidthConfig.getCapacity(), duration)
                        .build();

                default ->
                    throw new IllegalArgumentException("Unsupported refill speed: " + bandwidthConfig.getRefillSpeed());
            };
        };

        return () -> {
            LocalBucketBuilder builder = Bucket.builder();

            config.getFilters().stream()
                    .flatMap(filter -> filter.getRateLimits().stream())
                    .flatMap(rateLimit -> rateLimit.getBandwidths().stream())
                    .forEach(bandwidth -> builder.addLimit(bandwidthConverter.apply(bandwidth)));

            return builder.build();
        };
    }

    @Bean("rateLimitFilter")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    OncePerRequestFilter rateLimitFilter(Bucket4JConfigProperties configuration,
            Supplier<Bucket> bucketSupplier) {

        return new OncePerRequestFilter() {

            Cache<String, Bucket> buckets = Caffeine.newBuilder()
                    .expireAfterAccess(1, HOURS)
                    .maximumSize(50_000)
                    .build();

            Function<HttpServletRequest, String> ip = request -> {
                String forwarded = request.getHeader("X-Forwarded-For");
                if (nonNull(forwarded) && !forwarded.isBlank()) {
                    return forwarded.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            };

            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {
                String clientKey = ip.apply(request);
                Bucket bucket = buckets.get(clientKey, k -> bucketSupplier.get());
                ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
                if (probe.isConsumed()) {
                    chain.doFilter(request, response);
                } else {
                    response.setContentType("text/plain");
                    response.setStatus(429);
                    response.getWriter().append("Too many requests");
                }
            }
        };
    }
}
