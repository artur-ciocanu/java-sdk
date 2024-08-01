/*
 * Copyright 2024 The Dapr Authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
limitations under the License.
*/

package io.dapr.it.spring.boot.autoconfigure;

import io.dapr.testcontainers.Component;
import io.dapr.testcontainers.DaprContainer;
import io.dapr.testcontainers.DaprLogLevel;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;

@Testcontainers
public abstract class AbstractDaprSpringBootAutoconfigureBaseIT {

  public static Network daprNetwork = Network.newNetwork();

  @Container
  public static DaprContainer daprContainer = new DaprContainer("daprio/daprd:1.13.2")
      .withAppName("local-dapr-app")
      .withNetwork(daprNetwork)
      .withComponent(new Component("pubsub", "pubsub.in-memory", "v1", Collections.emptyMap()))
      .withAppPort(8080)
      .withDaprLogLevel(DaprLogLevel.DEBUG)
      .withAppChannelAddress("host.testcontainers.internal");

  @BeforeAll
  static void beforeAll() {
    org.testcontainers.Testcontainers.exposeHostPorts(8080);
    System.setProperty("dapr.grpc.port", Integer.toString(daprContainer.getGrpcPort()));
    System.setProperty("dapr.http.port", Integer.toString(daprContainer.getHttpPort()));
  }

}
