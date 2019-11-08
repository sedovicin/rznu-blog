package hr.fer.rznu.lab1.blog.entities;

import javax.persistence.Entity;
import javax.persistence.Id;

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
}
