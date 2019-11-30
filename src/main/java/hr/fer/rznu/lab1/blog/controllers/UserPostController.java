package hr.fer.rznu.lab1.blog.controllers;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import hr.fer.rznu.lab1.blog.Converter;
import hr.fer.rznu.lab1.blog.dto.BlogPostShort;
import hr.fer.rznu.lab1.blog.entities.BlogPostEntity;
import hr.fer.rznu.lab1.blog.repositories.BlogPostRepository;

@RestController
public class UserPostController {

	@Autowired
	private BlogPostRepository blogPostRepository;

	@GetMapping(path = "${users}/{userId}${posts}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public List<BlogPostShort> getUserBlogPosts(@PathVariable final String userId) {
		List<BlogPostEntity> postEntities = blogPostRepository
				.findAll(Example.of(new BlogPostEntity(null, userId, null, null)));

		return Converter.convertBlogPostEntitiesToShorts(postEntities);
	}

	@GetMapping(path = "${users}/{userId}${posts}/{postId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<BlogPostEntity> getUserBlogPosts(@PathVariable final String userId,
			@PathVariable final String postId) {
		Optional<BlogPostEntity> postEntity = blogPostRepository
				.findOne(Example.of(new BlogPostEntity(postId, userId, null, null)));

		if (postEntity.isPresent()) {
			return new ResponseEntity<>(postEntity.get(), HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@DeleteMapping(path = "${users}/{userId}${posts}/{postId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> deleteUserBlogPosts(@PathVariable final String userId,
			@PathVariable final String postId, final Principal principal) {
		if (!userId.equals(principal.getName())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Optional<BlogPostEntity> postEntity = blogPostRepository
				.findOne(Example.of(new BlogPostEntity(postId, userId, null, null)));

		if (postEntity.isPresent()) {
			blogPostRepository.delete(postEntity.get());
			return new ResponseEntity<>(HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}
