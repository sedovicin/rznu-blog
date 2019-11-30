package hr.fer.rznu.lab1.blog.dto;

import java.util.Objects;

public class BlogPostShort {

	private String id;
	private String username;
	private String title;

	public String getId() {
		return id;
	}

	public void setId(final String id) {
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

	public BlogPostShort(final String id, final String username, final String title) {
		super();
		this.id = id;
		this.username = username;
		this.title = title;
	}

	public BlogPostShort() {
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (!(other instanceof BlogPostShort)) {
			return false;
		}
		if (this == other) {
			return true;
		}
		BlogPostShort otherBlogPost = (BlogPostShort) other;
		return this.id.equals(otherBlogPost.id) && this.username.equals(otherBlogPost.username)
				&& this.title.equals(otherBlogPost.title);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, username, title);
	}
}
