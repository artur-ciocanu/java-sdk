package io.dapr.it.testcontainers.service.connection;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

public interface DaprConnectionDetails extends ConnectionDetails {
  String httpEndpoint();

  String grpcEndpoint();

  Integer httpPort();

  Integer grpcPort();
}
