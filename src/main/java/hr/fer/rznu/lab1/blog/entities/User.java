package hr.fer.rznu.lab1.blog.entities;

import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;

@Entity
public class User {

	@Id
	private String username;

	private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public User(final String username, final String password) {
		super();
		this.username = username;
		this.password = password;
	}

	public User() {
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (!(other instanceof User)) {
			return false;
		}
		if (this == other) {
			return true;
		}
		User otherUser = (User) other;
		return this.username.equals(otherUser.username);
	}

	@Override
	public int hashCode() {
		return Objects.hash(username, password);
	}

	public static String encryptPassword(final String plainTextPassword) {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(plainTextPassword);
	}

	public void encryptPassword() {
		this.password = encryptPassword(password);
	}
}
