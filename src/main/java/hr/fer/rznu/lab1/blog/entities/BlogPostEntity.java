package hr.fer.rznu.lab1.blog.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "BLOG_POST")
public class BlogPostEntity {

	@Id
	@GeneratedValue
	private Integer id;

	private String username;

	private String title;
	private String body;

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(final String body) {
		this.body = body;
	}

	public BlogPostEntity(final Integer id, final String username, final String title, final String body) {
		super();
		this.id = id;
		this.username = username;
		this.title = title;
		this.body = body;
	}

	public BlogPostEntity() {
	}
}
