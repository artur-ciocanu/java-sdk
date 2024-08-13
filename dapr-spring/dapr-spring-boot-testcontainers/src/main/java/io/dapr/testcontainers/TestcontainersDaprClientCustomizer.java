package io.dapr.testcontainers;

import io.dapr.client.DaprClientBuilder;
import io.dapr.config.Properties;
import io.dapr.spring.core.client.DaprClientCustomizer;
import org.springframework.beans.factory.annotation.Value;

public class TestcontainersDaprClientCustomizer implements DaprClientCustomizer {

  @Value("${dapr.http.port}")
  private String daprHttpPort;

  @Value("${dapr.grpc.port}")
  private String daprGrpcPort;

  @Override
  public void customize(DaprClientBuilder daprClientBuilder) {
    daprClientBuilder.withPropertyOverride(Properties.HTTP_PORT, daprHttpPort);
    daprClientBuilder.withPropertyOverride(Properties.GRPC_PORT, daprGrpcPort);
  }
}
