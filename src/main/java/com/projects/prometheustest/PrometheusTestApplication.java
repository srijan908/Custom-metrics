package com.projects.prometheustest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.metrics.web.servlet.DefaultWebMvcTagsProvider;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTagsProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PrometheusTestApplication {

    @Bean
    public WebMvcTagsProvider webMvcTagsProvider() {
        return new DefaultWebMvcTagsProvider() {
            @Override
            public Iterable<Tag> getTags(
                    HttpServletRequest request, HttpServletResponse response,
                    Object handler, Throwable exception) {
                return Tags.concat(
                        super.getTags(request, response, handler, exception),
                        Tags.of(Tag.of("uri", request.getRequestURI()))
                );
            }
        };
    }

    @Bean
    public CustomMemoryGauge customMemoryGauge(MeterRegistry meterRegistry) {
        return new CustomMemoryGauge(meterRegistry);
    }

    public static void main(String[] args) {
        SpringApplication.run(PrometheusTestApplication.class, args);
    }
}
