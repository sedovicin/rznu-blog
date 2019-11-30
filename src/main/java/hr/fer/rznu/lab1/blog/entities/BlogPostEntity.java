package hr.fer.rznu.lab1.blog.entities;

import java.util.Objects;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "BLOG_POST")
public class BlogPostEntity {

	@EmbeddedId
	private final BlogPostEntityId blogPostEntityId;

	private String title;
	private String body;

	public String getId() {
		return blogPostEntityId.getId();
	}

	public void setId(final String id) {
		blogPostEntityId.setId(id);
	}

	public String getUsername() {
		return blogPostEntityId.getUsername();
	}

	public void setUsername(final String username) {
		blogPostEntityId.setUsername(username);
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

	public BlogPostEntity(final String id, final String username, final String title, final String body) {
		super();
		blogPostEntityId = new BlogPostEntityId(id, username);
		this.title = title;
		this.body = body;
	}

	public BlogPostEntity() {
		blogPostEntityId = new BlogPostEntityId();
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (!(other instanceof BlogPostEntity)) {
			return false;
		}
		if (this == other) {
			return true;
		}
		BlogPostEntity otherBlogPost = (BlogPostEntity) other;
		boolean isEqual = (((this.blogPostEntityId == null) && (otherBlogPost.blogPostEntityId == null))
				|| (this.blogPostEntityId.equals(otherBlogPost.blogPostEntityId)));
		isEqual = isEqual && (((this.title == null) && (otherBlogPost.title == null))
				|| (this.title.equals(otherBlogPost.title)));
		isEqual = isEqual
				&& (((this.body == null) && (otherBlogPost.body == null)) || (this.body.equals(otherBlogPost.body)));
		return isEqual;
	}

	@Override
	public int hashCode() {
		return Objects.hash(blogPostEntityId, title, body);
	}
}
