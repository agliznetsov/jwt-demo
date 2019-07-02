package com.example.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.demo.dto.AuthenticationRequest;
import com.example.demo.dto.AuthenticationResponse;
import com.example.demo.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class AuthenticationTest {

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	TestRestTemplate restTemplate;

	private String token;

	@Before
	public void setup() {
		token = null;
		restTemplate.getRestTemplate().setInterceptors(
				Collections.singletonList((request, body, execution) -> {
					if (token != null) {
						request.getHeaders().add("Authorization", "Bearer " + token);
					}
					return execution.execute(request, body);
				}));
	}

	@Test
	public void testLoginOK() {
		ResponseEntity<AuthenticationResponse> response = tryLogin("user", "password", AuthenticationResponse.class);

		assertEquals(OK, response.getStatusCode());
		assertNotNull(response.getBody().getToken());
		assertEquals("user", response.getBody().getUser().getUsername());
	}

	@Test
	public void testLoginBadCredentials() {
		ResponseEntity<Map> response = tryLogin("user", "wrong_password", Map.class);
		assertEquals(FORBIDDEN, response.getStatusCode());
	}

	@Test
	public void testMeOK() {
		loginAsUser();
		ResponseEntity<UserDTO> response = restTemplate.getForEntity("/auth/me", UserDTO.class);
		assertEquals(OK, response.getStatusCode());
		assertEquals("user", response.getBody().getUsername());
	}

	@Test
	public void testMeUnauthenticated() {
		ResponseEntity<Map> response = restTemplate.getForEntity("/auth/me", Map.class);
		assertEquals(FORBIDDEN, response.getStatusCode()); //should be UNAUTHORIZED ?
	}


	private void loginAsUser() {
		ResponseEntity<AuthenticationResponse> response = tryLogin("user", "password", AuthenticationResponse.class);
		token = response.getBody().getToken();
	}

	private <T> ResponseEntity<T> tryLogin(String username, String password, Class<T> responseClass) {
		AuthenticationRequest request = AuthenticationRequest.builder().username(username).password(password).build();
		return restTemplate.postForEntity("/auth/login", request, responseClass);
	}
}
