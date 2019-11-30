package hr.fer.rznu.lab1.blog.controllers;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import hr.fer.rznu.lab1.blog.Converter;
import hr.fer.rznu.lab1.blog.dto.BlogPost;
import hr.fer.rznu.lab1.blog.dto.BlogPostShort;
import hr.fer.rznu.lab1.blog.entities.BlogPostEntity;
import hr.fer.rznu.lab1.blog.repositories.BlogPostRepository;

@RestController
public class PostController {
	@Autowired
	private BlogPostRepository blogPostRepository;

	@Value("${users}")
	private String usersPath;
	@Value("${posts}")
	private String postsPath;

	@PutMapping(path = "${posts}/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> postNewBlog(@PathVariable final String id, @RequestBody final BlogPost blogPost,
			final Principal principal) {
		if (blogPost.getTitle().isEmpty() || blogPost.getBody().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		Optional<BlogPostEntity> existingEntity = blogPostRepository
				.findOne(Example.of(new BlogPostEntity(id, principal.getName(), null, null)));
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		HttpStatus status = null;
		if (existingEntity.isPresent()) {
			status = HttpStatus.OK;
		} else {
			status = HttpStatus.CREATED;
			headers.add("Location", usersPath + "/" + principal.getName() + postsPath + "/" + id);
		}
		BlogPostEntity entity = new BlogPostEntity(id, principal.getName(), blogPost.getTitle(), blogPost.getBody());
		blogPostRepository.save(entity);
		return new ResponseEntity<>(headers, status);
	}

	@GetMapping(path = "${posts}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<BlogPostShort> getBlogPosts() {
		List<BlogPostEntity> postEntities = blogPostRepository.findAll();

		return Converter.convertBlogPostEntitiesToShorts(postEntities);
	}
}
