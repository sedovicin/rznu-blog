package hr.fer.rznu.lab1.blog.dto;

public class BlogPost {

	private String title;
	private String body;

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

	public BlogPost(final String title, final String body) {
		super();
		this.title = title;
		this.body = body;
	}

	public BlogPost() {
	}
}
