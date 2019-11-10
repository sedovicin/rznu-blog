package hr.fer.rznu.lab1.blog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
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
		mockMvc.perform(post(registerPath).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.content(Converter.convertObjectToJson(dummyUser))).andExpect(status().isCreated());

		// adding second user with the same username should fail
		when(userRepository.existsById(dummyUser.getUsername())).thenReturn(true);

		dummyUser.setPassword("password2");
		mockMvc.perform(post(registerPath).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.content(Converter.convertObjectToJson(dummyUser))).andExpect(status().isConflict());

		// second user should be added successfully
		dummyUser.setUsername("dummierUser");
		mockMvc.perform(post(registerPath).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.content(Converter.convertObjectToJson(dummyUser))).andExpect(status().isCreated());

	}

	@Test
	public void authorizationWorksProperly() throws Exception {
		User testUserEncryptedPass = new User("test", User.encryptPassword("pass"));
		when(userRepository.findById(testUserEncryptedPass.getUsername()))
				.thenReturn(Optional.of(testUserEncryptedPass));
		when(blogUserDetailsService.loadUserByUsername(testUserEncryptedPass.getUsername()))
				.thenReturn(BlogUserDetailsService.blogUserToSpringUser(testUserEncryptedPass));

		User testUser = new User("test", "pass");
		User notRegisteredUser = new User("notRegistered", "password");

		mockMvc.perform(post(registerPath).contentType(MediaType.APPLICATION_JSON)
				.content(Converter.convertObjectToJson(testUser))).andExpect(status().isCreated());

		int httpResponseStatus = mockMvc
				.perform(get(usersPath).accept(MediaType.APPLICATION_JSON)
						.with(httpBasic(testUser.getUsername(), testUser.getPassword())))
				.andReturn().getResponse().getStatus();
		assertThat(httpResponseStatus).isNotEqualTo(HttpStatus.UNAUTHORIZED.value());

		when(blogUserDetailsService.loadUserByUsername(notRegisteredUser.getUsername())).thenThrow(
				new UsernameNotFoundException("No user found with username " + notRegisteredUser.getUsername()));
		httpResponseStatus = mockMvc
				.perform(get(usersPath).accept(MediaType.APPLICATION_JSON)
						.with(httpBasic(notRegisteredUser.getUsername(), notRegisteredUser.getPassword())))
				.andReturn().getResponse().getStatus();
		assertThat(httpResponseStatus).isEqualTo(HttpStatus.UNAUTHORIZED.value());
	}

	@Test
	public void userListWorksProperly() throws Exception {
		String testUsername = "test";
		String testPassword = "pass";
		User testUser = new User(testUsername, User.encryptPassword(testPassword));
		UserDetails testUserDetails = BlogUserDetailsService.blogUserToSpringUser(testUser);

		// query for all users
		User user1 = new User("user1", "password1");
		User user2 = new User("user2", "password2");
		List<User> users = new ArrayList<>();
		users.add(user1);
		users.add(user2);

		ObjectMapper om = new ObjectMapper();
		String listToJson = om.writeValueAsString(Converter.removeUserPasswords(users));

		when(blogUserDetailsService.loadUserByUsername(testUser.getUsername())).thenReturn(testUserDetails);
		when(userRepository.findAll()).thenReturn(users);

		mockMvc.perform(get(usersPath).accept(MediaType.APPLICATION_JSON).with(httpBasic(testUsername, testPassword)))
				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(content().json(listToJson));

		// query for a single user
		users = new ArrayList<>();
		users.add(user1);
		listToJson = om.writeValueAsString(Converter.removeUserPasswords(users));

		when(blogUserDetailsService.loadUserByUsername(testUser.getUsername()))
				.thenReturn(BlogUserDetailsService.blogUserToSpringUser(testUser));
		when(userRepository.findById(user1.getUsername())).thenReturn(Optional.of(user1));

		mockMvc.perform(get(usersPath + "/" + user1.getUsername()).accept(MediaType.APPLICATION_JSON)
				.with(httpBasic(testUsername, testPassword))).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(content().json(listToJson));
	}
}
