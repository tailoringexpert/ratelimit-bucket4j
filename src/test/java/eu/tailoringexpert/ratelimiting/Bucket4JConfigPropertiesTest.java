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

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import eu.tailoringexpert.ratelimiting.domain.Bandwidth;
import eu.tailoringexpert.ratelimiting.domain.Bucket4JConfigProperties;
import eu.tailoringexpert.ratelimiting.domain.Filter;
import eu.tailoringexpert.ratelimiting.domain.RateLimit;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.log4j.Log4j2;

@Log4j2
class Bucket4JConfigPropertiesTest {

    Validator validator;

    @BeforeEach
    void beforeEach() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    void validatRootProperty_NoFilterDefined_ValidationError() {
        // arrange
        Bucket4JConfigProperties properties = Bucket4JConfigProperties.builder()
                .filters(List.of())
                .build();

        // act
        Set<ConstraintViolation<Bucket4JConfigProperties>> actual = validator.validate(properties);

        // assert
        assertThat(actual).hasSize(1);
        assertThat(actual.iterator().next().getMessage())
                .isEqualTo("The filter list must contain exactly one element!");
    }

    @Test
    void validatRootProperty_2FilterDefined_ValidationError() {
        // arrange
        Bucket4JConfigProperties properties = Bucket4JConfigProperties.builder()
                .filters(List.of(
                        Filter.builder().build(),
                        Filter.builder().build()))
                .build();

        // act
        Set<ConstraintViolation<Bucket4JConfigProperties>> actual = validator.validate(properties);

        // assert
        assertThat(actual).hasSize(1);
        assertThat(actual.iterator().next().getMessage())
                .isEqualTo("The filter list must contain exactly one element!");
    }

    @Test
    void validatRootProperty_1FilterDefined_NoValidationError() {
        // arrange
        Bucket4JConfigProperties properties = Bucket4JConfigProperties.builder()
                .filters(List.of(
                        Filter.builder().build()))
                .build();

        // act
        Set<ConstraintViolation<Bucket4JConfigProperties>> actual = validator.validate(properties);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void validatFilterProperty_NoRateLimitsDefined_ValidationError() {
        // arrange
        Filter filter = Filter.builder()
                .rateLimits(List.of())
                .build();

        // act
        Set<ConstraintViolation<Filter>> actual = validator.validate(filter);

        // assert
        assertThat(actual).hasSize(1);
        assertThat(actual.iterator().next().getMessage())
                .isEqualTo("The ratelimits list must contain at least one element!");
    }

    @Test
    void validatFilterProperty_1RateLimitsDefined_NoValidationError() {
        // arrange
        Filter filter = Filter.builder()
                .rateLimits(List.of(
                        RateLimit.builder().build()))
                .build();

        // act
        Set<ConstraintViolation<Filter>> actual = validator.validate(filter);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void validatBandwithUnitProperty_InvalidUnit_ValidationError() {
        // arrange
        Bandwidth bandwidth = Bandwidth.builder()
                .unit(ChronoUnit.HALF_DAYS)
                .build();

        // act
        Set<ConstraintViolation<Bandwidth>> actual = validator.validate(bandwidth);

        // assert
        assertThat(actual).hasSize(1);
        assertThat(actual.iterator().next().getMessage())
                .isEqualTo("Invalid value 'HALF_DAYS'. Allowed Values: [SECONDS, MINUTES, HOURS, DAYS]");
    }

    @ParameterizedTest(name = "unit {0} is valid")
    @MethodSource("getBandwithUnits")
    void validatBandwithUnitProperty_ValidUnit_NoValidationError(ChronoUnit unit) {
        // arrange
        Bandwidth bandwidth = Bandwidth.builder()
                .unit(unit)
                .build();
        // act
        Set<ConstraintViolation<Bandwidth>> actual = validator.validate(bandwidth);

        // assert
        assertThat(actual).isEmpty();

    }

    @SuppressWarnings({ "java:S1144" })
    private static Stream<Arguments> getBandwithUnits() { // NOPMD - suppressed UnusedPrivateMethod - Used by
                                                          // parameterized test
                                                          // getRequirement_NullParameter_ExceptionThrown
        return Stream.of(
                Arguments.of(SECONDS),
                Arguments.of(MINUTES),
                Arguments.of(HOURS),
                Arguments.of(DAYS));
    }

    @Test
    void validatBandwithRefillSpeedProperty_InvalidUnit_ValidationError() {
        // arrange
        Bandwidth bandwidth = Bandwidth.builder()
                .refillSpeed("seconds")
                .build();

        // act
        Set<ConstraintViolation<Bandwidth>> actual = validator.validate(bandwidth);

        // assert
        assertThat(actual).hasSize(1);
        assertThat(actual.iterator().next().getMessage())
                .isEqualTo("refillSpeed must either be 'greedy' or 'interval'");
    }

    @ParameterizedTest(name = "refillSpeed {0} is valid")
    @MethodSource("getBandwithRefillSpeeds")
    void validatBandwithRefillSpeedProperty_ValidRefillSpeed_NoValidationError(String refillSpeed) {
        // arrange
        Bandwidth bandwidth = Bandwidth.builder()
                .refillSpeed(refillSpeed)
                .build();
        // act
        Set<ConstraintViolation<Bandwidth>> actual = validator.validate(bandwidth);

        // assert
        assertThat(actual).isEmpty();

    }

    @SuppressWarnings({ "java:S1144" })
    private static Stream<Arguments> getBandwithRefillSpeeds() { // NOPMD - suppressed UnusedPrivateMethod - Used by
        // parameterized test
        // getRequirement_NullParameter_ExceptionThrown
        return Stream.of(
                Arguments.of("greedy"),
                Arguments.of("interval"));
    }
}
