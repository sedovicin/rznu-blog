package hr.fer.rznu.lab1.blog.entities;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;

@Embeddable
public class BlogPostEntityId implements Serializable {
	private static final long serialVersionUID = 1L;

	private String id;
	private String username;

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

	public BlogPostEntityId(final String id, final String username) {
		super();
		this.id = id;
		this.username = username;
	}

	public BlogPostEntityId() {
	}

	@Override
	public boolean equals(final Object other) {
		if (other == null) {
			return false;
		}
		if (!(other instanceof BlogPostEntityId)) {
			return false;
		}
		if (this == other) {
			return true;
		}
		BlogPostEntityId otherId = (BlogPostEntityId) other;
		return this.id.equals(otherId.id) && this.username.equals(otherId.username);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, username);
	}
}
