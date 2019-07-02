package com.example.demo.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class UserDTO {
	Long id;
	String username;
	List<String> roles = new ArrayList<>();
}
