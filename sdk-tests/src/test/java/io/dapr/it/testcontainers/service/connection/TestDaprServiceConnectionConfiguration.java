package io.dapr.it.testcontainers.service.connection;

import io.dapr.client.DaprClient;
import io.dapr.spring.boot.autoconfigure.client.DaprClientAutoConfiguration;
import io.dapr.spring.boot.autoconfigure.pubsub.DaprPubSubProperties;
import io.dapr.spring.core.client.DaprClientCustomizer;
import io.dapr.spring.messaging.DaprMessagingTemplate;
import io.dapr.testcontainers.TestcontainersDaprClientCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DaprClientAutoConfiguration.class)
@EnableConfigurationProperties(DaprPubSubProperties.class)
public class TestDaprServiceConnectionConfiguration {

  @Bean
  DaprClientCustomizer daprClientCustomizer(DaprConnectionDetails connectionDetails) {
    return new TestcontainersDaprClientCustomizer(
        connectionDetails.httpEndpoint(),
        connectionDetails.grpcEndpoint(),
        String.valueOf(connectionDetails.httpPort()),
        String.valueOf(connectionDetails.grpcPort())
    );
  }

  @Bean
  public DaprMessagingTemplate<String> messagingTemplate(DaprClient daprClient,
                                                         DaprPubSubProperties daprPubSubProperties) {
    return new DaprMessagingTemplate<>(daprClient, daprPubSubProperties.getName());
  }

}
