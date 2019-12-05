package hr.fer.rznu.lab1.blog.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import hr.fer.rznu.lab1.blog.Converter;
import hr.fer.rznu.lab1.blog.entities.BlogPostEntity;
import hr.fer.rznu.lab1.blog.entities.User;
import hr.fer.rznu.lab1.blog.repositories.BlogPostRepository;
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
	public ResponseEntity<User> getUser(@PathVariable(value = "id") final String userName) {
		Optional<User> userOptional = userRepository.findById(userName);
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			user.setPassword("");
			return new ResponseEntity<>(user, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@PostMapping(path = "${users}/{id}", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> changePassword(@PathVariable(value = "id") final String userName,
			@RequestBody final String newPassword, final Principal principal) {
		if (!userName.equals(principal.getName())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Optional<User> userOptional = userRepository.findById(userName);
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			user.setPassword(User.encryptPassword(newPassword));
			userRepository.save(user);

			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@Autowired
	private BlogPostRepository blogPostRepository;

	@DeleteMapping(path = "${users}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> deleteUser(@PathVariable(value = "id") final String userName,
			final Principal principal) {
		if (!userName.equals(principal.getName())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Optional<User> userOptional = userRepository.findById(userName);
		if (userOptional.isPresent()) {
			User user = userOptional.get();
			List<BlogPostEntity> usersEntities = blogPostRepository
					.findAll(Example.of(new BlogPostEntity(null, user.getUsername(), null, null)));

			blogPostRepository.deleteAll(usersEntities);
			userRepository.delete(user);

			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@GetMapping(path = "/", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> documentation() throws IOException {
		String body = new String(Files.readAllBytes(new File("src/main/resources/static/doc.txt").toPath()));
		return new ResponseEntity<>(body, HttpStatus.OK);
	}
}
