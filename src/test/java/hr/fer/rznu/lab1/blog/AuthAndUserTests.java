package hr.fer.rznu.lab1.blog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

import hr.fer.rznu.lab1.blog.entities.User;
import hr.fer.rznu.lab1.blog.repositories.BlogPostRepository;
import hr.fer.rznu.lab1.blog.repositories.UserRepository;
import hr.fer.rznu.lab1.blog.security.BlogUserDetailsService;

@WebMvcTest
@PropertySource(value = "classpath:restapi.properties")
public class AuthAndUserTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private BlogPostRepository blogPostRepository;

	@MockBean
	private BlogUserDetailsService blogUserDetailsService;

	@Value("${register}")
	private String registerPath;
	@Value("${users}")
	private String usersPath;
	@Value("${posts}")
	private String postsPath;

	@Test
	public void authorizationOffForRegistration() throws Exception {
		int status = mockMvc.perform(post(registerPath)).andReturn().getResponse().getStatus();
		assertThat(status).isNotEqualTo(HttpStatus.UNAUTHORIZED.value());

		mockMvc.perform(get(usersPath)).andExpect(MockMvcResultMatchers.status().isUnauthorized());

		mockMvc.perform(get(postsPath)).andExpect(MockMvcResultMatchers.status().isUnauthorized());

	}

	@Test
	public void addingUserWorksProperly() throws Exception {

		// adding first user should be successful
		User dummyUser = new User("dummyUser", "password1");
		performUnauthorizedJsonRequest(post(registerPath), dummyUser).andExpect(status().isCreated());

		// adding second user with the same username should fail
		when(userRepository.existsById(dummyUser.getUsername())).thenReturn(true);
		dummyUser.setPassword("password2");
		performUnauthorizedJsonRequest(post(registerPath), dummyUser).andExpect(status().isConflict());

		// second user should be added successfully
		dummyUser.setUsername("dummierUser");
		performUnauthorizedJsonRequest(post(registerPath), dummyUser).andExpect(status().isCreated());

	}

	@Test
	public void authorizationWorksProperly() throws Exception {
		User testUserEncryptedPass = prepareTestUser("pass");
		when(userRepository.findById(testUserEncryptedPass.getUsername()))
				.thenReturn(Optional.of(testUserEncryptedPass));

		User testUser = new User("test", "pass");
		User notRegisteredUser = new User("notRegistered", "password");

		performUnauthorizedJsonRequest(post(registerPath), testUser).andExpect(status().isCreated());

		// try authorised request with existing user
		ensureAuthentication(testUserEncryptedPass);
		int httpResponseStatus = performJsonRequest(get(usersPath), testUser.getUsername(), testUser.getPassword(),
				null).andReturn().getResponse().getStatus();
		assertThat(httpResponseStatus).isNotEqualTo(HttpStatus.UNAUTHORIZED.value());

		// try authorised request with non-existing user
		when(blogUserDetailsService.loadUserByUsername(notRegisteredUser.getUsername())).thenThrow(
				new UsernameNotFoundException("No user found with username " + notRegisteredUser.getUsername()));
		httpResponseStatus = performJsonRequest(get(usersPath), notRegisteredUser.getUsername(),
				notRegisteredUser.getPassword(), null).andReturn().getResponse().getStatus();
		assertThat(httpResponseStatus).isEqualTo(HttpStatus.UNAUTHORIZED.value());
	}

	@Test
	public void userListWorksProperly() throws Exception {
		String testPassword = "pass";
		User testUser = prepareTestUser(testPassword);

		// query for all users
		User user1 = new User("user1", "password1");
		User user2 = new User("user2", "password2");
		List<User> users = new ArrayList<>();
		users.add(user1);
		users.add(user2);

		ObjectMapper om = new ObjectMapper();
		String listToJson = om.writeValueAsString(Converter.removeUserPasswords(users));

		when(userRepository.findAll()).thenReturn(users);

		ensureAuthentication(testUser);
		performJsonRequest(get(usersPath), testUser.getUsername(), testPassword, null).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(content().json(listToJson));

		// query for a single user
		users = new ArrayList<>();
		users.add(user1);
		listToJson = om.writeValueAsString(Converter.removeUserPasswords(users));

		when(userRepository.findById(user1.getUsername())).thenReturn(Optional.of(user1));

		ensureAuthentication(testUser);
		performJsonRequest(get(usersPath + "/" + user1.getUsername()), testUser.getUsername(), testPassword, null)
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(content().json(listToJson));
	}

	/**
	 * Creates and returns new test user
	 *
	 * @param password password to set to new user
	 * @return new test user with given password
	 */
	private User prepareTestUser(final String password) {
		String testUsername = "test";
		return new User(testUsername, User.encryptPassword(password));
	}

	/**
	 * Does mocking of authorising existing user
	 *
	 * @param user user to be authorised
	 */
	private void ensureAuthentication(final User user) {
		when(blogUserDetailsService.loadUserByUsername(user.getUsername()))
				.thenReturn(BlogUserDetailsService.blogUserToSpringUser(user));
	}

	/**
	 * Method helper for builder setup. Adds content type and accept headers, sets
	 * content and authorisation data.
	 *
	 * @param requestMethodWithPath method to be called
	 * @param username
	 * @param password
	 * @param content
	 * @return
	 * @throws Exception
	 */
	private MockHttpServletRequestBuilder jsonRequest(final MockHttpServletRequestBuilder requestMethodWithPath,
			final String username, final String password, final Object content) throws Exception {
		MockHttpServletRequestBuilder builder = requestMethodWithPath.contentType(MediaType.APPLICATION_JSON)
				.content(Converter.convertObjectToJson(content)).accept(MediaType.APPLICATION_JSON);
		if ((username != null) && (password != null)) {
			builder = builder.with(httpBasic(username, password));
		}
		return builder;
	}

	/**
	 * Performs a mocked request using MockMvc.
	 *
	 * @param request  request to be performed (example: get("/users"))
	 * @param user     authorized user
	 * @param password user's unencrypted password
	 * @param content  body content. Can be null
	 * @return ResultActions in order to maximally customize what is expected
	 * @throws Exception
	 */
	private ResultActions performJsonRequest(final MockHttpServletRequestBuilder request, final String username,
			final String password, final Object content) throws Exception {
		return mockMvc.perform(jsonRequest(request, username, password, content).with(csrf()));
	}

	/**
	 * Performs a mocked request using MockMvc, without authorisation
	 *
	 * @param request request to be performed (example: get("/users"))
	 * @param content body content. Can be null
	 * @return ResultActions in order to maximally customize what is expected
	 * @throws Exception
	 */
	private ResultActions performUnauthorizedJsonRequest(final MockHttpServletRequestBuilder request,
			final Object content) throws Exception {
		return mockMvc.perform(jsonRequest(request, null, null, content).with(csrf()));
	}
}
