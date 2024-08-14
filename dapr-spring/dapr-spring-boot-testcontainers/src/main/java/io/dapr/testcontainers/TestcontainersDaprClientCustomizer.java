package io.dapr.testcontainers;

import io.dapr.client.DaprClientBuilder;
import io.dapr.config.Properties;
import io.dapr.spring.core.client.DaprClientCustomizer;
import org.springframework.beans.factory.annotation.Value;

public class TestcontainersDaprClientCustomizer implements DaprClientCustomizer {

  private String daprHttpPort;
  private String daprGrpcPort;

  public TestcontainersDaprClientCustomizer(String daprHttpPort, String daprGrpcPort) {
    this.daprHttpPort = daprHttpPort;
    this.daprGrpcPort = daprGrpcPort;
  }

  @Override
  public void customize(DaprClientBuilder daprClientBuilder) {
    daprClientBuilder.withPropertyOverride(Properties.HTTP_PORT, daprHttpPort);
    daprClientBuilder.withPropertyOverride(Properties.GRPC_PORT, daprGrpcPort);
  }
}
