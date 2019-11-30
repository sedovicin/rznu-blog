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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

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
	public void blogPostAddingWorksProperly() throws Exception {
		String testPassword = "pass1";
		User testUser = prepareTestUser(testPassword);
		BlogPost newPost;
		String blogId = "testPost";

		// Valid blog post
		newPost = new BlogPost("New post", "This is a test post.");
		performJsonRequest(put(postsPath + "/" + blogId), testUser, testPassword, newPost)
				.andExpect(status().isCreated());

		// Missing body
		newPost = new BlogPost("Title only", "");
		performJsonRequest(put(postsPath + "/" + blogId + "2"), testUser, testPassword, newPost)
				.andExpect(status().isBadRequest());

		// Missing title
		newPost = new BlogPost("", "Body only");
		performJsonRequest(put(postsPath + "/" + blogId + "3"), testUser, testPassword, newPost)
				.andExpect(status().isBadRequest());

	}

//	@SuppressWarnings("unchecked")
//	@Test
//	public void blogPostListWorksProperly() throws Exception {
//		// fetch by username, by title, all, mine
//		String testPassword = "pass1";
//		User testUser = prepareTestUser(testPassword);
//
//		// Fetch all
//		BlogPostEntity post1 = new BlogPostEntity("blog1", "dummy", "New post1", "This is a test post.");
//		BlogPostEntity post2 = new BlogPostEntity("blog2", "test", "New post2", "This is a second post.");
//
//		List<BlogPostEntity> blogPosts = new ArrayList<>();
//		blogPosts.add(post1);
//		blogPosts.add(post2);
//
//		when(blogPostRepository.findAll()).thenReturn(blogPosts);
//		performJsonRequest(get(postsPath), testUser, testPassword, null).andExpect(status().isOk())
//				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
//				.andExpect(content().json(Converter.convertObjectToJsonString(blogPosts)));
//
//		post1 = new BlogPostEntity("blog1", "test", "New post1", "This is a test post.");
//		post2 = new BlogPostEntity("blog2", "test", "New post2", "This is a second post.");
//		blogPosts = new ArrayList<>();
//		blogPosts.add(post1);
//		blogPosts.add(post2);
//
//		when(blogPostRepository.findAll(ArgumentMatchers.any(Example.class))).thenReturn(blogPosts);
//		performJsonRequest(get(postsPath).param("username", testUser.getUsername()), testUser, testPassword, null)
//				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
//				.andExpect(content().json(Converter.convertObjectToJsonString(blogPosts)));
//		performJsonRequest(get(postsPath).param("title", "New post1"), testUser, testPassword, null)
//				.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
//				.andExpect(content().json(Converter.convertObjectToJsonString(blogPosts)));
//
//	}

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
		return requestMethodWithPath.contentType(MediaType.APPLICATION_JSON)
				.content(Converter.convertObjectToJson(content)).accept(MediaType.APPLICATION_JSON)
				.with(httpBasic(username, password));
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
	private ResultActions performJsonRequest(final MockHttpServletRequestBuilder request, final User user,
			final String password, final Object content) throws Exception {
		ensureAuthentication(user);
		return mockMvc.perform(jsonRequest(request, user.getUsername(), password, content).with(csrf()));
	}
}
