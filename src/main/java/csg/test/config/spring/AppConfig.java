package csg.test.config.spring;

import java.util.Map;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.amqp.Amqp;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@EnableIntegration
@RestController
public class AppConfig {

	@Autowired
	ConnectionFactory connectionFactory;

	@MessagingGateway
	public interface TestGateway {

		@Gateway(requestChannel = "testControlBus")
		void publish(String command);

	}

	@Autowired
	TestGateway testGateway;

	@RequestMapping("/start")
	public String startTest() {
		testGateway.publish("@testInbound.start()");
		return "Inbound adapter started.";
	}

	@RequestMapping("/stop")
	public String stopTest() {
		testGateway.publish("@testInbound.stop()");
		return "Inbound adapter stopped.";
	}

	@Bean
	public IntegrationFlow testBus() {
		return IntegrationFlows.from("testControlBus").controlBus().get();
	}

	@Bean
	public IntegrationFlow defineStatHubConsumerFlow() {
		return IntegrationFlows
			.from(Amqp.inboundAdapter(connectionFactory, "test.queue").id("testInbound").autoStartup(false))
			.<byte[]> handle((p, h) -> processPayload(p, h))
			.get();
	}

	protected String processPayload(byte[] payload, Map<String, Object> headers) {

		try {

			System.out.println("Payload: " + payload);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

}
