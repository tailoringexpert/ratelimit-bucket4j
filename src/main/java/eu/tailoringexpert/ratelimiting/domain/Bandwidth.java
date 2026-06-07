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
package eu.tailoringexpert.ratelimiting.domain;

import java.time.temporal.ChronoUnit;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data object representing configuration for a
 * {@link io.github.bucket4j.Bandwidth}.
 * 
 * @author Michael Bädorf
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bandwidth {

    int capacity;
    long time;

    @AllowedEnums({ "SECONDS", "MINUTES", "HOURS", "DAYS" })
    // @Pattern(regexp = "SECONDS|MINUTES|HOURS", message = "unit must either be
    // 'SECONDS', 'MINUTES', 'HOURS' or 'DAYS'")
    ChronoUnit unit;

    @Pattern(regexp = "^(greedy|interval)$", message = "refillSpeed must either be 'greedy' or 'interval'")
    String refillSpeed;
}
