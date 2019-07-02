package com.example.demo.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.User;
import com.example.demo.dto.AuthenticationRequest;
import com.example.demo.dto.AuthenticationResponse;
import com.example.demo.dto.UserDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.jwt.JwtTokenProvider;

import ma.glasnost.orika.MapperFacade;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@Autowired
	UserRepository userRepository;

	@Autowired
	MapperFacade mapperFacade;

	@PostMapping("/login")
	public AuthenticationResponse signin(@RequestBody AuthenticationRequest data) {
		String username = data.getUsername();
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, data.getPassword()));
		User user = this.userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("Username " + username + "not found"));
		String token = jwtTokenProvider.createToken(username, user.getRoles());

		return AuthenticationResponse.builder()
				.token(token)
				.user(mapperFacade.map(user, UserDTO.class))
				.build();
	}

	@GetMapping("/me")
	public UserDTO currentUser(@AuthenticationPrincipal UserDetails userDetails) {
		User user = this.userRepository.findByUsername(userDetails.getUsername())
				.orElseThrow(
						() -> new UsernameNotFoundException("Username " + userDetails.getUsername() + "not found"));

		return mapperFacade.map(user, UserDTO.class);
	}
}
