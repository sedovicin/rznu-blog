package hr.fer.rznu.lab1.blog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest
@PropertySource(value = "classpath:restapi.properties")
public class WebMvcTests {

	@Autowired
	private MockMvc mockMvc;

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

}
