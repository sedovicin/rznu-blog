package hr.fer.rznu.lab1.blog.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import hr.fer.rznu.lab1.blog.Converter;
import hr.fer.rznu.lab1.blog.entities.User;
import hr.fer.rznu.lab1.blog.repositories.UserRepository;

@RestController
public class UserController {
	@Autowired
	private UserRepository userRepository;

	@Value("${users}")
	private String usersPath;

	@PostMapping(path = "${register}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> registerUser(@RequestBody final User user) {
		if (userRepository.existsById(user.getUsername())) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		User newUser = new User(user.getUsername(), User.encryptPassword(user.getPassword()));
		userRepository.save(newUser);

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Location", usersPath + "/" + user.getUsername());
		return new ResponseEntity<>(headers, HttpStatus.CREATED);
	}

	@GetMapping(path = "${users}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<User> getUsers() {
		List<User> users = userRepository.findAll();

		return Converter.removeUserPasswords(users);
	}

	@GetMapping(path = "${users}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<User> getUser(@PathVariable(value = "id") final String userName) {
		List<User> userList = new ArrayList<>();
		Optional<User> userOptional = userRepository.findById(userName);
		if (userOptional.isPresent()) {
			userList.add(userOptional.get());
		}
		return Converter.removeUserPasswords(userList);
	}
}
