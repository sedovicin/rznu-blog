package hr.fer.rznu.lab1.blog;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import hr.fer.rznu.lab1.blog.dto.BlogPost;
import hr.fer.rznu.lab1.blog.entities.User;
import hr.fer.rznu.lab1.blog.repositories.BlogPostRepository;
import hr.fer.rznu.lab1.blog.repositories.UserRepository;
import hr.fer.rznu.lab1.blog.security.BlogUserDetailsService;

@WebMvcTest
@PropertySource(value = "classpath:restapi.properties")
public class BlogPostTests {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private BlogPostRepository blogPostRepository;

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private BlogUserDetailsService blogUserDetailsService;

	@Value("${register}")
	private String registerPath;
	@Value("${posts}")
	private String postsPath;

	@Test
	public void blogPostingWorksProperly() throws Exception {
		String testUsername = "test";
		String testPassword = "pass1";
		User testUser = new User(testUsername, User.encryptPassword(testPassword));

		BlogPost newPost = new BlogPost("New post", "This is a test post.");

		when(blogUserDetailsService.loadUserByUsername(testUser.getUsername()))
				.thenReturn(BlogUserDetailsService.blogUserToSpringUser(testUser));

		mockMvc.perform(
				put(postsPath).contentType(MediaType.APPLICATION_JSON).content(Converter.convertObjectToJson(newPost))
						.accept(MediaType.APPLICATION_JSON).with(httpBasic(testUsername, testPassword)).with(csrf()))
				.andExpect(status().isCreated());

		newPost = new BlogPost("Title only", "");

		when(blogUserDetailsService.loadUserByUsername(testUser.getUsername()))
				.thenReturn(BlogUserDetailsService.blogUserToSpringUser(testUser));

		mockMvc.perform(
				put(postsPath).contentType(MediaType.APPLICATION_JSON).content(Converter.convertObjectToJson(newPost))
						.with(httpBasic(testUsername, testPassword)).with(csrf()))
				.andExpect(status().isBadRequest());

		newPost = new BlogPost("", "Body only");

		when(blogUserDetailsService.loadUserByUsername(testUser.getUsername()))
				.thenReturn(BlogUserDetailsService.blogUserToSpringUser(testUser));

		mockMvc.perform(
				put(postsPath).contentType(MediaType.APPLICATION_JSON).content(Converter.convertObjectToJson(newPost))
						.with(httpBasic(testUsername, testPassword)).with(csrf()))
				.andExpect(status().isBadRequest());
	}
}
