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

package io.dapr.testcontainers;

import io.dapr.testcontainers.converter.ComponentYamlConverter;
import io.dapr.testcontainers.converter.ConfigurationYamlConverter;
import io.dapr.testcontainers.converter.SubscriptionYamlConverter;
import io.dapr.testcontainers.converter.YamlConverter;
import io.dapr.testcontainers.converter.YamlMapperFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitStrategy;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DaprContainer extends GenericContainer<DaprContainer> {
  private static final int DAPRD_DEFAULT_HTTP_PORT = 3500;
  private static final int DAPRD_DEFAULT_GRPC_PORT = 50001;
  private static final DaprProtocol DAPR_PROTOCOL = DaprProtocol.HTTP;
  private static final DockerImageName DEFAULT_IMAGE_NAME = DockerImageName.parse("daprio/daprd");
  private static final Yaml YAML_MAPPER = YamlMapperFactory.create();
  private static final YamlConverter<Component> COMPONENT_CONVERTER = new ComponentYamlConverter(YAML_MAPPER);
  private static final YamlConverter<Subscription> SUBSCRIPTION_CONVERTER = new SubscriptionYamlConverter(YAML_MAPPER);
  private static final YamlConverter<Configuration> CONFIGURATION_CONVERTER = new ConfigurationYamlConverter(
      YAML_MAPPER);
  private static final WaitStrategy WAIT_STRATEGY = Wait.forHttp("/v1.0/healthz/outbound")
      .forPort(DAPRD_DEFAULT_HTTP_PORT)
      .forStatusCodeMatching(statusCode -> statusCode >= 200 && statusCode <= 399);

  private final Set<Component> components = new HashSet<>();
  private final Set<Subscription> subscriptions = new HashSet<>();
  private DaprLogLevel daprLogLevel = DaprLogLevel.INFO;
  private String appChannelAddress = "localhost";
  private String placementService = "placement";
  private String placementDockerImageName = "daprio/placement";

  private Configuration configuration;
  private DaprPlacementContainer placementContainer;
  private String appName;
  private Integer appPort;
  private boolean shouldReusePlacement;

  /**
   * Creates a new Dapr container.
   * @param dockerImageName Docker image name.
   */
  public DaprContainer(DockerImageName dockerImageName) {
    super(dockerImageName);
    dockerImageName.assertCompatibleWith(DEFAULT_IMAGE_NAME);
    withAccessToHost(true);
    withExposedPorts(DAPRD_DEFAULT_HTTP_PORT, DAPRD_DEFAULT_GRPC_PORT);
    setWaitStrategy(WAIT_STRATEGY);
  }

  /**
   * Creates a new Dapr container.
   * @param image Docker image name.
   */
  public DaprContainer(String image) {
    this(DockerImageName.parse(image));
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public Set<Component> getComponents() {
    return components;
  }

  public Set<Subscription> getSubscriptions() {
    return subscriptions;
  }

  public DaprContainer withAppPort(Integer port) {
    this.appPort = port;
    return this;
  }

  public DaprContainer withAppChannelAddress(String appChannelAddress) {
    this.appChannelAddress = appChannelAddress;
    return this;
  }

  public DaprContainer withConfiguration(Configuration configuration) {
    this.configuration = configuration;
    return this;
  }

  public DaprContainer withPlacementService(String placementService) {
    this.placementService = placementService;
    return this;
  }

  public DaprContainer withAppName(String appName) {
    this.appName = appName;
    return this;
  }

  public DaprContainer withDaprLogLevel(DaprLogLevel daprLogLevel) {
    this.daprLogLevel = daprLogLevel;
    return this;
  }

  public DaprContainer withSubscription(Subscription subscription) {
    subscriptions.add(subscription);
    return this;
  }

  public DaprContainer withPlacementImage(String placementDockerImageName) {
    this.placementDockerImageName = placementDockerImageName;
    return this;
  }

  public DaprContainer withReusablePlacement(boolean reuse) {
    this.shouldReusePlacement = reuse;
    return this;
  }

  public DaprContainer withPlacementContainer(DaprPlacementContainer placementContainer) {
    this.placementContainer = placementContainer;
    return this;
  }

  public DaprContainer withComponent(Component component) {
    components.add(component);
    return this;
  }

  /**
   * Adds a Dapr component from a YAML file.
   * @param path Path to the YAML file.
   * @return This container.
   */
  public DaprContainer withComponent(Path path) {
    try {
      Map<String, Object> component = this.YAML_MAPPER.loadAs(Files.newInputStream(path), Map.class);

      String type = (String) component.get("type");
      Map<String, Object> metadata = (Map<String, Object>) component.get("metadata");
      String name = (String) metadata.get("name");

      Map<String, Object> spec = (Map<String, Object>) component.get("spec");
      String version = (String) spec.get("version");
      List<Map<String, String>> specMetadata =
          (List<Map<String, String>>) spec.getOrDefault("metadata", Collections.emptyMap());

      ArrayList<MetadataEntry> metadataEntries = new ArrayList<>();

      for (Map<String, String> specMetadataItem : specMetadata) {
        for (Map.Entry<String, String> metadataItem : specMetadataItem.entrySet()) {
          metadataEntries.add(new MetadataEntry(metadataItem.getKey(), metadataItem.getValue()));
        }
      }

      return withComponent(new Component(name, type, version, metadataEntries));
    } catch (IOException e) {
      logger().warn("Error while reading component from {}", path.toAbsolutePath());
    }
    return this;
  }

  public int getHttpPort() {
    return getMappedPort(DAPRD_DEFAULT_HTTP_PORT);
  }

  public String getHttpEndpoint() {
    return "http://" + getHost() + ":" + getMappedPort(DAPRD_DEFAULT_HTTP_PORT);
  }

  public int getGrpcPort() {
    return getMappedPort(DAPRD_DEFAULT_GRPC_PORT);
  }

  public String getGrpcEndpoint() {
    return ":" + getMappedPort(DAPRD_DEFAULT_GRPC_PORT);
  }

  @Override
  protected void configure() {
    super.configure();

    if (getNetwork() == null) {
      withNetwork(Network.newNetwork());
    }

    if (this.placementContainer == null) {
      this.placementContainer = new DaprPlacementContainer(this.placementDockerImageName)
          .withNetwork(getNetwork())
          .withNetworkAliases(placementService)
          .withReuse(this.shouldReusePlacement);
      this.placementContainer.start();
    }

    List<String> cmds = new ArrayList<>();
    cmds.add("./daprd");
    cmds.add("-app-id");
    cmds.add(appName);
    cmds.add("--dapr-listen-addresses=0.0.0.0");
    cmds.add("--app-protocol");
    cmds.add(DAPR_PROTOCOL.getName());
    cmds.add("-placement-host-address");
    cmds.add(placementService + ":50005");

    if (appChannelAddress != null && !appChannelAddress.isEmpty()) {
      cmds.add("--app-channel-address");
      cmds.add(appChannelAddress);
    }

    if (appPort != null) {
      cmds.add("--app-port");
      cmds.add(Integer.toString(appPort));
    }

    cmds.add("--log-level");
    cmds.add(daprLogLevel.toString());
    cmds.add("--resources-path");
    cmds.add("/dapr-resources");
    withCommand(cmds.toArray(new String[]{}));

    if (configuration != null) {
      String configurationYaml = CONFIGURATION_CONVERTER.convert(configuration);
      withCopyToContainer(Transferable.of(configurationYaml), "/dapr-resources/" + configuration.getName() + ".yaml");
    }

    if (components.isEmpty()) {
      components.add(new Component("kvstore", "state.in-memory", "v1", Collections.emptyMap()));
      components.add(new Component("pubsub", "pubsub.in-memory", "v1", Collections.emptyMap()));
    }

    if (subscriptions.isEmpty() && !components.isEmpty()) {
      subscriptions.add(new Subscription("local", "pubsub", "topic", "/events"));
    }

    for (Component component : components) {
      String componentYaml = COMPONENT_CONVERTER.convert(component);
      withCopyToContainer(Transferable.of(componentYaml), "/dapr-resources/" + component.getName() + ".yaml");
    }

    for (Subscription subscription : subscriptions) {
      String subscriptionYaml = SUBSCRIPTION_CONVERTER.convert(subscription);
      withCopyToContainer(Transferable.of(subscriptionYaml), "/dapr-resources/" + subscription.getName() + ".yaml");
    }

    dependsOn(placementContainer);
  }

  public String getAppName() {
    return appName;
  }

  public Integer getAppPort() {
    return appPort;
  }

  public String getAppChannelAddress() {
    return appChannelAddress;
  }

  public String getPlacementService() {
    return placementService;
  }

  public static DockerImageName getDefaultImageName() {
    return DEFAULT_IMAGE_NAME;
  }

  // Required by spotbugs plugin
  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
