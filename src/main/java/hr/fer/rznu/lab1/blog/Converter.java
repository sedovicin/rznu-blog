package hr.fer.rznu.lab1.blog;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import hr.fer.rznu.lab1.blog.dto.BlogPost;
import hr.fer.rznu.lab1.blog.entities.BlogPostEntity;
import hr.fer.rznu.lab1.blog.entities.User;

public class Converter {

	public static byte[] convertObjectToJson(final Object object) throws JsonProcessingException {
		ObjectMapper om = new ObjectMapper();
		return om.writeValueAsBytes(object);
	}

	public static List<User> removeUserPasswords(final List<User> users) {
		List<User> usersNew = new ArrayList<>();
		for (User user : users) {
			User newUser = new User(user.getUsername(), "");
			usersNew.add(newUser);
		}
		return usersNew;
	}

	public static BlogPostEntity blogPostToEntity(final BlogPost blogPost) {

		return null;
	}
}
