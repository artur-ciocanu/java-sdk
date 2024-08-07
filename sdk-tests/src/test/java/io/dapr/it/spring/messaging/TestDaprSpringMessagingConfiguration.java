package io.dapr.it.spring.messaging;

import io.dapr.client.DaprClient;
import io.dapr.spring.boot.autoconfigure.pubsub.DaprPubSubProperties;
import io.dapr.spring.messaging.DaprMessagingTemplate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DaprPubSubProperties.class)
public class TestDaprSpringMessagingConfiguration {
  @Bean
  public DaprMessagingTemplate<String> daprMessagingTemplate(DaprClient daprClient, DaprPubSubProperties properties) {
    return new DaprMessagingTemplate<>(daprClient, properties.getName());
  }
}
