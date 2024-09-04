package io.dapr.it.testcontainers.service.connection;

import io.dapr.client.domain.CloudEvent;
import io.dapr.spring.messaging.DaprMessagingTemplate;
import io.dapr.testcontainers.Component;
import io.dapr.testcontainers.DaprContainer;
import io.dapr.testcontainers.DaprLogLevel;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    classes = {
        TestDaprServiceConnectionConfiguration.class,
        TestApplication.class
    },
    properties = {"dapr.pubsub.name=pubsub"}
)
@Testcontainers
public class DaprServiceConnectionIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(DaprServiceConnectionIT.class);
  private static final String TOPIC = "mockTopic";
  private static final Network DAPR_NETWORK = Network.newNetwork();

  @Container
  @ServiceConnection(name = "dapr")
  private static final DaprContainer DAPR_CONTAINER = new DaprContainer("daprio/daprd:1.13.2")
      .withAppName("sc-dapr-app")
      .withNetwork(DAPR_NETWORK)
      .withComponent(new Component("pubsub", "pubsub.in-memory", "v1", Collections.emptyMap()))
      .withAppPort(8080)
      .withDaprLogLevel(DaprLogLevel.DEBUG)
      .withLogConsumer(outputFrame -> System.out.println(outputFrame.getUtf8String()))
      .withAppChannelAddress("host.testcontainers.internal");

  @Autowired
  private DaprMessagingTemplate<String> messagingTemplate;

  @Autowired
  private TestRestController testRestController;

  @Test
  public void testDaprMessagingTemplate() throws InterruptedException {
    for (int i = 0; i < 10; i++) {
      var msg = "ProduceAndReadWithPrimitiveMessageType:" + i;

      messagingTemplate.send(TOPIC, msg);

      LOGGER.info("++++++PRODUCE {}------", msg);
    }

    // Wait for the messages to arrive
    Thread.sleep(1000);

    List<CloudEvent<String>> events = testRestController.getEvents();

    assertThat(events.size()).isEqualTo(10);
  }

}
