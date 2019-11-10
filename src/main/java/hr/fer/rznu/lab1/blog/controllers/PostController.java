package hr.fer.rznu.lab1.blog.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import hr.fer.rznu.lab1.blog.Converter;
import hr.fer.rznu.lab1.blog.dto.BlogPost;
import hr.fer.rznu.lab1.blog.entities.BlogPostEntity;
import hr.fer.rznu.lab1.blog.repositories.BlogPostRepository;

@RestController
public class PostController {
	@Autowired
	private BlogPostRepository blogPostRepository;

	@PutMapping(path = "${posts}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<String> postNewBlog(@RequestBody final BlogPost blogPost) {
		if (blogPost.getTitle().isEmpty() || blogPost.getBody().isEmpty()) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		BlogPostEntity entity = Converter.blogPostToEntity(blogPost);
		blogPostRepository.save(entity);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}
}
