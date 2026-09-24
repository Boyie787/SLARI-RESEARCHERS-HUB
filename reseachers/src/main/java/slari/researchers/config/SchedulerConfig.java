package slari.researchers.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulerConfig {
    // Empty - just enables scheduling
    // Scheduled tasks are defined in services with @Scheduled
}