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

import static java.lang.Thread.sleep;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import eu.tailoringexpert.ratelimiting.domain.Bucket4JConfigProperties;

@SpringBootTest(classes = Bucket4jRateLimitingConfigurationTest.TestConfig.class)
@AutoConfigureMockMvc
@EnableConfigurationProperties(Bucket4JConfigProperties.class)
@ActiveProfiles("bucket4j")
class Bucket4jRateLimitingConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void afterEach() throws Exception {
        // wait for buckets to be refilled
        sleep(20000);
    }

    @Test
    void rateLimitFilter_useDefaultParametersMaxCalls_State200Returned() throws Exception {
        // arrange
        String testUrl = "/api/echo/{number}";
        int maxRequestsBeforeBlock = 10;

        // act and assert
        for (int i = 0; i < maxRequestsBeforeBlock; i++) {
            mockMvc.perform(get(testUrl, i))
                    .andExpect(status().isOk());
        }

    }

    @Test
    void rateLimitFilter_useDefaultParametersExceedBuckets_State429Returned() throws Exception {
        // arrange
        String testUrl = "/api/echo/{number}";
        int maxRequestsBeforeBlock = 10;

        for (int i = 0; i < maxRequestsBeforeBlock; i++) {
            mockMvc.perform(get(testUrl, i))
                    .andExpect(status().isOk());
        }

        // act and assert
        mockMvc.perform(get(testUrl, maxRequestsBeforeBlock))
                .andExpect(status().is(TOO_MANY_REQUESTS.value()));
    }

    @Test
    void rateLimitFilter_useDefaultParametersBucketsRefilled_State200Returned() throws Exception {
        // arrange
        String testUrl = "/api/echo/{number}";
        int maxRequestsBeforeBlock = 10;

        for (int i = 0; i < maxRequestsBeforeBlock; i++) {
            mockMvc.perform(get(testUrl, i))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get(testUrl, maxRequestsBeforeBlock))
                .andExpect(status().is(TOO_MANY_REQUESTS.value()));

        sleep(20000); // wait for buckets to be refilled

        // act and assert
        mockMvc.perform(get(testUrl, maxRequestsBeforeBlock + 1))
                .andExpect(status().isOk());
    }

    @Configuration
    @ConfigurationPropertiesScan
    @Import({
            Bucket4jRateLimitingConfiguration.class,
            EchoController.class
    })
    static class TestConfig {
    }

    @RestController
    static class EchoController {
        @GetMapping("/api/echo/{number}")
        public ResponseEntity<String> echo(@PathVariable(name = "number") int number) {
            return ResponseEntity.ok("Call number " + number);
        }
    }
}
