package com.example.AccountService;

import com.example.AccountService.feignClient.HealthStatusProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AccountServiceApplicationTests {
	@Autowired
	private HealthStatusProvider healthStatusProvider;

	@Test
	void contextLoads() {
	}

	@Test
	void healthStatusProviderReportsApplicationHealth() {
		assertEquals("UP", healthStatusProvider.getStatus());
	}

	@Test
	void mainDelegatesToSpringApplication() {
		String[] args = {"--spring.main.web-application-type=none"};
		ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);

		try (var springApplication = mockStatic(SpringApplication.class)) {
			springApplication.when(() -> SpringApplication.run(AccountServiceApplication.class, args))
					.thenReturn(context);

			AccountServiceApplication.main(args);

			springApplication.verify(() -> SpringApplication.run(AccountServiceApplication.class, args));
		}
		assertNotNull(context);
	}

}
