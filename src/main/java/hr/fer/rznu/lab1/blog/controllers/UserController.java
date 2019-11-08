package hr.fer.rznu.lab1.blog.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import hr.fer.rznu.lab1.blog.entities.User;
import hr.fer.rznu.lab1.blog.repositories.UserRepository;

@RestController
public class UserController {
	@Autowired
	private UserRepository userRepository;

	@Value(value = "${register}")
	private String registerPath;

	@PostMapping(path = "${register}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> registerUser(@RequestBody final User user) {
		if (userRepository.existsById(user.getUsername())) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		User newUser = new User(user.getUsername(), User.encryptPassword(user.getPassword()));
		userRepository.save(newUser);

		return new ResponseEntity<>(HttpStatus.CREATED);
	}
}
