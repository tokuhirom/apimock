package me.geso.apimock;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.Collections;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class APIMockTomcatTest {

	@Test
	public void test() throws Exception {
		try (APIMockTomcat mock = new APIMockTomcat()) {
			mock.get("/", c -> "TOP");
			mock.start();
			URI uri = mock.getURI();
			HttpResponse resp = Request.Get(uri)
					.execute()
					.returnResponse();
			assertEquals(200, resp.getStatusLine().getStatusCode());
			assertEquals("\"TOP\"", EntityUtils.toString(resp.getEntity()));
		}
	}

	@Test
	public void testSerializeHash() throws Exception {
		try (APIMockTomcat mock = new APIMockTomcat()) {
			mock.get("/", c -> Collections.emptyMap());
			mock.start();
			URI uri = mock.getURI();
			HttpResponse resp = Request.Get(uri)
					.execute()
					.returnResponse();
			assertEquals(200, resp.getStatusLine().getStatusCode());
			assertEquals("{}", EntityUtils.toString(resp.getEntity()));
		}
	}

	@Test
	public void test405() throws Exception {
		try (APIMockTomcat mock = new APIMockTomcat()) {
			mock.get("/", c -> "TOP");
			mock.start();
			URI uri = mock.getURI();
			HttpResponse resp = Request.Post(uri)
					.execute()
					.returnResponse();
			assertEquals(405, resp.getStatusLine().getStatusCode());
		}
	}

	@Test
	public void test404() throws Exception {
		try (APIMockTomcat mock = new APIMockTomcat()) {
			mock.get("/foo", c -> "TOP");
			mock.start();
			URI uri = mock.getURI();
			HttpResponse resp = Request.Post(uri.resolve("/path"))
					.execute()
					.returnResponse();
			assertEquals(404, resp.getStatusLine().getStatusCode());
		}
	}

	@Test
	public void testPost() throws Exception {
		try (APIMockTomcat mock = new APIMockTomcat()) {
			mock.post("/", c -> "TOP");
			mock.start();
			URI uri = mock.getURI();
			HttpResponse resp = Request.Post(uri)
					.execute()
					.returnResponse();
			assertEquals(200, resp.getStatusLine().getStatusCode());
		}
	}

}
