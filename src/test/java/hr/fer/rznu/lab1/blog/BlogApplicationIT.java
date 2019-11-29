package hr.fer.rznu.lab1.blog;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import hr.fer.rznu.lab1.blog.entities.User;
import hr.fer.rznu.lab1.blog.repositories.BlogPostRepository;
import hr.fer.rznu.lab1.blog.repositories.UserRepository;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
class BlogApplicationIT {

	@Value("${register}")
	private String registerPath;
	@Value("${users}")
	private String usersPath;
	@Value("${posts}")
	private String postsPath;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BlogPostRepository blogPostRepository;

	private final WebClient webClient = WebClient.builder().baseUrl("http://127.0.0.1:9090")
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE).build();

	private final String testUsername = "test";
	private final String testPassword = "testPassword";
	private final String testUsername1 = "test1";
	private final String testPassword1 = "testPassword1";

	private final String unauthUsername = "unauth";
	private final String unauthPassword = "password11";

	@Test
	@Order(value = 1)
	public void registerUserShouldWork() {

		User user = new User(testUsername, testPassword);
		User userEncryptedPassword = new User(testUsername, User.encryptPassword(testPassword));
		ClientResponse monoResponse = webClient.post().uri(registerPath).bodyValue(user).exchange().block();
		if (monoResponse != null) {
			assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.CREATED);
			Optional<User> userFromDB = userRepository.findById(testUsername);
			assertThat(userFromDB.isPresent()).isTrue();

			assertThat(userFromDB.get()).isEqualTo(userEncryptedPassword);
		}

		monoResponse = webClient.post().uri(registerPath).bodyValue(user).exchange().block();
		if (monoResponse != null) {
			assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.CONFLICT);
		}

		User user1 = new User(testUsername1, testPassword1);
		User userEncryptedPassword1 = new User(testUsername1, User.encryptPassword(testPassword1));
		monoResponse = webClient.post().uri(registerPath).bodyValue(user1).exchange().block();
		if (monoResponse != null) {
			assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.CREATED);
			Optional<User> userFromDB = userRepository.findById(testUsername1);
			assertThat(userFromDB.isPresent()).isTrue();

			assertThat(userFromDB.get()).isEqualTo(userEncryptedPassword1);
		}

		List<User> users = userRepository.findAll();
		assertThat(users.size()).isEqualTo(2);
		assertThat(users).contains(userEncryptedPassword, userEncryptedPassword1);
	}

	@Test
	@Order(value = 2)
	public void userFetchShouldWork() throws JsonMappingException, JsonProcessingException {
		List<User> users = userRepository.findAll();

		ClientResponse monoResponse = webClient.get().uri(usersPath).exchange().block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		monoResponse = webClient.get().uri(usersPath)
				.headers(headers -> headers.setBasicAuth(unauthUsername, unauthPassword)).exchange().block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		monoResponse = webClient.get().uri(usersPath)
				.headers(headers -> headers.setBasicAuth(testUsername, testPassword)).exchange().block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.OK);

		String response = monoResponse.bodyToMono(String.class).block();
		User[] parsedUsers = Converter.convertJsonStringToObject(response, User[].class);

		assertThat(users).containsExactlyInAnyOrder(parsedUsers);
	}

}
