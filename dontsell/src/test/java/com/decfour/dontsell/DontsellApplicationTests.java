package com.decfour.dontsell;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "jwt.secret=test-only-secret-key-at-least-32-bytes")
class DontsellApplicationTests {

	@Test
	void contextLoads() {
	}

}
