package hr.fer.rznu.lab1.blog;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
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
import org.springframework.data.domain.Example;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import hr.fer.rznu.lab1.blog.dto.BlogPost;
import hr.fer.rznu.lab1.blog.dto.BlogPostShort;
import hr.fer.rznu.lab1.blog.entities.BlogPostEntity;
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
			assertThat(monoResponse.headers().header(HttpHeaders.LOCATION))
					.matches(locationList -> (locationList.size() == 1)
							&& locationList.get(0).equals(usersPath + "/" + testUsername));
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
			assertThat(monoResponse.headers().header(HttpHeaders.LOCATION))
					.matches(locationList -> (locationList.size() == 1)
							&& locationList.get(0).equals(usersPath + "/" + testUsername1));
			Optional<User> userFromDB = userRepository.findById(testUsername1);
			assertThat(userFromDB.isPresent()).isTrue();

			assertThat(userFromDB.get()).isEqualTo(userEncryptedPassword1);
		}

		List<User> users = userRepository.findAll();
		assertThat(users).hasSize(2);
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
		assertThat(parsedUsers).allSatisfy(user -> assertThat(user.getPassword()).isEmpty());
	}

	private static final List<BlogPostShort> blogPostsStatic = new ArrayList<>();
	private static BlogPostEntity blogPostStatic = null;

	@Test
	@Order(value = 3)
	public void blogPostAdditionAndUpdateShouldWork() {
		BlogPost blogPost = new BlogPost("Test blog", "Test content.");
		String blogPostId = "test-blog";
		ClientResponse monoResponse = webClient.put().uri(postsPath + "/" + blogPostId).bodyValue(blogPost).exchange()
				.block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		monoResponse = webClient.put().uri(postsPath + "/" + blogPostId)
				.headers(headers -> headers.setBasicAuth(unauthUsername, unauthPassword)).bodyValue(blogPost).exchange()
				.block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		monoResponse = webClient.put().uri(postsPath + "/" + blogPostId)
				.headers(headers -> headers.setBasicAuth(testUsername, testPassword)).bodyValue(blogPost).exchange()
				.block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(monoResponse.headers().header(HttpHeaders.LOCATION))
				.matches(locationList -> (locationList.size() == 1)
						&& locationList.get(0).equals(usersPath + "/" + testUsername + postsPath + "/" + blogPostId));

		List<BlogPostEntity> blogPosts = blogPostRepository.findAll();
		assertThat(blogPosts).hasSize(1);

		BlogPost blogPost1 = new BlogPost("Test blog 1", "Test content 1.");
		String blogPostId1 = "test-blog-1";
		monoResponse = webClient.put().uri(postsPath + "/" + blogPostId1)
				.headers(headers -> headers.setBasicAuth(testUsername, testPassword)).bodyValue(blogPost1).exchange()
				.block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(monoResponse.headers().header(HttpHeaders.LOCATION))
				.matches(locationList -> (locationList.size() == 1)
						&& locationList.get(0).equals(usersPath + "/" + testUsername + postsPath + "/" + blogPostId1));

		blogPosts = blogPostRepository.findAll();
		assertThat(blogPosts).hasSize(2);

		blogPost.setTitle("Different title");
		monoResponse = webClient.put().uri(postsPath + "/" + blogPostId)
				.headers(headers -> headers.setBasicAuth(testUsername, testPassword)).bodyValue(blogPost).exchange()
				.block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.OK);
		assertThat(monoResponse.headers().header(HttpHeaders.LOCATION))
				.matches(locationList -> (locationList.size() == 0));

		blogPosts = blogPostRepository.findAll();
		assertThat(blogPosts).hasSize(2);

		// blog post id should not be unique globally, but for the user
		monoResponse = webClient.put().uri(postsPath + "/" + blogPostId)
				.headers(headers -> headers.setBasicAuth(testUsername1, testPassword1)).bodyValue(blogPost).exchange()
				.block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(monoResponse.headers().header(HttpHeaders.LOCATION))
				.matches(locationList -> (locationList.size() == 1)
						&& locationList.get(0).equals(usersPath + "/" + testUsername1 + postsPath + "/" + blogPostId));

		List<BlogPostEntity> sameIdPosts = blogPostRepository
				.findAll(Example.of(new BlogPostEntity(blogPostId, null, null, null)));
		assertThat(sameIdPosts).hasSize(2);

		// blog post should have both title and body
		BlogPost badBlogPost = new BlogPost("", "Test content.");
		String badBlogPostId = "bad-blog";
		monoResponse = webClient.put().uri(postsPath + "/" + badBlogPostId)
				.headers(headers -> headers.setBasicAuth(testUsername, testPassword)).bodyValue(badBlogPost).exchange()
				.block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		badBlogPost = new BlogPost("Test title", "");
		badBlogPostId = "bad-blog";
		monoResponse = webClient.put().uri(postsPath + "/" + badBlogPostId)
				.headers(headers -> headers.setBasicAuth(testUsername, testPassword)).bodyValue(badBlogPost).exchange()
				.block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		blogPostsStatic.add(new BlogPostShort(blogPostId, testUsername, blogPost.getTitle()));
		blogPostsStatic.add(new BlogPostShort(blogPostId1, testUsername, blogPost1.getTitle()));
		blogPostsStatic.add(new BlogPostShort(blogPostId, testUsername1, blogPost.getTitle()));

		blogPostStatic = new BlogPostEntity(blogPostId, testUsername, blogPost.getTitle(), blogPost.getBody());
	}

	@Test
	@Order(value = 4)
	public void blogPostFetchShouldWork() throws JsonMappingException, JsonProcessingException {
		BlogPost blogPost = new BlogPost("Test blog 5000", "Test content 5000.");
		String blogPostId5000 = "test-blog-5000";

		// Create blog post for other test user
		ClientResponse monoResponse = webClient.put().uri(postsPath + "/" + blogPostId5000)
				.headers(headers -> headers.setBasicAuth(testUsername1, testPassword1)).bodyValue(blogPost).exchange()
				.block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.CREATED);

		blogPostsStatic.add(new BlogPostShort(blogPostId5000, testUsername1, blogPost.getTitle()));

		// Without authorization should return unauthorized
		monoResponse = webClient.get().uri(postsPath).exchange().block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		// with wrong user should return unauthorized
		monoResponse = webClient.get().uri(postsPath)
				.headers(headers -> headers.setBasicAuth(unauthUsername, unauthPassword)).exchange().block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		// check full list
		monoResponse = webClient.get().uri(postsPath)
				.headers(headers -> headers.setBasicAuth(testUsername1, testPassword1)).exchange().block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.OK);

		String response = monoResponse.bodyToMono(String.class).block();
		BlogPostShort[] parsedPosts = Converter.convertJsonStringToObject(response, BlogPostShort[].class);

		assertThat(blogPostsStatic).containsExactlyInAnyOrder(parsedPosts);
		assertThat(parsedPosts).allSatisfy(blogPostShort -> {
			assertThat(blogPostShort.getId()).isNotEmpty();
			assertThat(blogPostShort.getUsername()).isNotEmpty();
			assertThat(blogPostShort.getTitle()).isNotEmpty();
		});

		// check first user list
		monoResponse = webClient.get().uri(usersPath + "/" + testUsername + postsPath)
				.headers(headers -> headers.setBasicAuth(testUsername1, testPassword1)).exchange().block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.OK);

		response = monoResponse.bodyToMono(String.class).block();
		parsedPosts = Converter.convertJsonStringToObject(response, BlogPostShort[].class);

		List<BlogPostShort> firstUserPosts = blogPostsStatic.subList(0, 2);
		assertThat(firstUserPosts).containsExactlyInAnyOrder(parsedPosts);
		assertThat(parsedPosts).allSatisfy(blogPostShort -> {
			assertThat(blogPostShort.getId()).isNotEmpty();
			assertThat(blogPostShort.getUsername()).isNotEmpty();
			assertThat(blogPostShort.getTitle()).isNotEmpty();
		});

		// check second user list
		monoResponse = webClient.get().uri(usersPath + "/" + testUsername1 + postsPath)
				.headers(headers -> headers.setBasicAuth(testUsername1, testPassword1)).exchange().block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.OK);

		response = monoResponse.bodyToMono(String.class).block();
		parsedPosts = Converter.convertJsonStringToObject(response, BlogPostShort[].class);

		List<BlogPostShort> secondUserPosts = blogPostsStatic.subList(2, 4);
		assertThat(secondUserPosts).containsExactlyInAnyOrder(parsedPosts);
		assertThat(parsedPosts).allSatisfy(blogPostShort -> {
			assertThat(blogPostShort.getId()).isNotEmpty();
			assertThat(blogPostShort.getUsername()).isNotEmpty();
			assertThat(blogPostShort.getTitle()).isNotEmpty();
		});

		// check fetching only one
		monoResponse = webClient.get().uri(usersPath + "/" + testUsername + postsPath + "/" + blogPostStatic.getId())
				.headers(headers -> headers.setBasicAuth(testUsername1, testPassword1)).exchange().block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.OK);

		response = monoResponse.bodyToMono(String.class).block();
		BlogPostEntity fetchedBlogPost = Converter.convertJsonStringToObject(response, BlogPostEntity.class);

		assertThat(fetchedBlogPost).isEqualTo(blogPostStatic);

		// check fetching unexistent blog post
		monoResponse = webClient.get()
				.uri(usersPath + "/" + testUsername1 + postsPath + "/" + blogPostsStatic.get(1).getId())
				.headers(headers -> headers.setBasicAuth(testUsername1, testPassword1)).exchange().block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.NOT_FOUND);

		monoResponse = webClient.get().uri(usersPath + "/" + testUsername + postsPath + "/" + "unexistent-blog-post")
				.headers(headers -> headers.setBasicAuth(testUsername1, testPassword1)).exchange().block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.NOT_FOUND);

	}

	@Test
	@Order(value = 5)
	public void blogPostDeleteShouldWork() {
		// should be able to delete existing blog post
		ClientResponse monoResponse = webClient.delete()
				.uri(usersPath + "/" + testUsername + postsPath + "/" + blogPostsStatic.get(0).getId()).exchange()
				.block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		monoResponse = webClient.delete()
				.uri(usersPath + "/" + testUsername + postsPath + "/" + blogPostsStatic.get(0).getId())
				.headers(headers -> headers.setBasicAuth(unauthUsername, unauthPassword)).exchange().block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		monoResponse = webClient.delete()
				.uri(usersPath + "/" + testUsername + postsPath + "/" + blogPostsStatic.get(0).getId())
				.headers(headers -> headers.setBasicAuth(testUsername, testPassword)).exchange().block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.OK);

		// should not be able to delete another users blog post
		monoResponse = webClient.delete()
				.uri(usersPath + "/" + testUsername1 + postsPath + "/" + blogPostsStatic.get(2).getId())
				.headers(headers -> headers.setBasicAuth(testUsername, testPassword)).exchange().block();
		assertThat(monoResponse.statusCode()).isEqualTo(HttpStatus.FORBIDDEN);

	}
}
