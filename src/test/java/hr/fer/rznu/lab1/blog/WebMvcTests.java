package hr.fer.rznu.lab1.blog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import hr.fer.rznu.lab1.blog.entities.User;
import hr.fer.rznu.lab1.blog.repositories.UserRepository;

@WebMvcTest
@PropertySource(value = "classpath:restapi.properties")
public class WebMvcTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserRepository userRepository;

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
}
