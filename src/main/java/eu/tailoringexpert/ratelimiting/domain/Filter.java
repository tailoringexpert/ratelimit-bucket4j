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

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data object representing configuration for a filter containing a list of rate
 * limits.
 * 
 * @author Michael Bädorf
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Filter {

    @Default
    @Size(min = 1, message = "The ratelimits list must contain at least one element!")
    List<RateLimit> rateLimits = new ArrayList<>();
}
