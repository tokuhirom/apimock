package me.geso.apimock;

import javax.servlet.http.HttpServletRequest;

public class APIMockContext {
	private final HttpServletRequest request;

	public APIMockContext(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletRequest getRequest() {
		return this.request;
	}
}
