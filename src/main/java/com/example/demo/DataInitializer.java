package com.example.demo;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.demo.domain.User;
import com.example.demo.domain.Vehicle;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VehicleRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

	@Autowired
	VehicleRepository vehicles;

	@Autowired
	UserRepository users;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) throws Exception {
		log.debug("initializing vehicles data...");
		Arrays.asList("moto", "car").forEach(v -> this.vehicles.saveAndFlush(Vehicle.builder().name(v).build()));

		log.debug("printing all vehicles...");
		this.vehicles.findAll().forEach(v -> log.debug(" Vehicle :" + v.toString()));

		saveUser("user", Arrays.asList("ROLE_USER"));

		saveUser("admin", Arrays.asList("ROLE_USER", "ROLE_ADMIN"));

		log.debug("printing all users...");
		this.users.findAll().forEach(v -> log.debug(" UserDTO :" + v.toString()));
	}

	private void saveUser(String name, Collection<String> roles) {
		User user = new User();
		user.setUsername(name);
		user.setPassword(this.passwordEncoder.encode("password"));
		user.getRoles().addAll(roles);
		this.users.save(user);
	}
}
