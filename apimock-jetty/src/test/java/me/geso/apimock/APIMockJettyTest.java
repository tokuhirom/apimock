package me.geso.apimock;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class APIMockJettyTest {

	@Test
	public void test() throws Exception {
		try (APIMockJetty mock = new APIMockJetty()) {
			mock.get("/", c -> "TOP");
			mock.start();
			URI uri = mock.getURI();
			HttpResponse resp = Request.Get(uri).execute().returnResponse();
			assertEquals(200, resp.getStatusLine().getStatusCode());
			assertEquals("\"TOP\"", EntityUtils.toString(resp.getEntity()));
		}
	}

	@Test
	public void testSerializeHash() throws Exception {
		try (APIMockJetty mock = new APIMockJetty()) {
			mock.get("/", c -> Collections.emptyMap());
			mock.start();
			URI uri = mock.getURI();
			HttpResponse resp = Request.Get(uri).execute().returnResponse();
			assertEquals(200, resp.getStatusLine().getStatusCode());
			assertEquals("{}", EntityUtils.toString(resp.getEntity()));
		}
	}

	@Test
	public void test405() throws Exception {
		try (APIMockJetty mock = new APIMockJetty()) {
			mock.get("/", c -> "TOP");
			mock.start();
			URI uri = mock.getURI();
			HttpResponse resp = Request.Post(uri).execute().returnResponse();
			assertEquals(405, resp.getStatusLine().getStatusCode());
		}
	}

	@Test
	public void test404() throws Exception {
		try (APIMockJetty mock = new APIMockJetty()) {
			mock.get("/foo", c -> "TOP");
			mock.start();
			URI uri = mock.getURI();
			HttpResponse resp = Request.Post(uri.resolve("/path")).execute()
					.returnResponse();
			assertEquals(404, resp.getStatusLine().getStatusCode());
		}
	}

	@Test
	public void testPost() throws Exception {
		try (APIMockJetty mock = new APIMockJetty()) {
			mock.post("/", c -> "TOP");
			mock.start();
			URI uri = mock.getURI();
			HttpResponse resp = Request.Post(uri).execute().returnResponse();
			assertEquals(200, resp.getStatusLine().getStatusCode());
		}
	}

	public static class MyEntity {
		private String msg;

		public String getMsg() {
			return this.msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}
	}

	@Test
	public void testReadJson() throws Exception {
		try (APIMockJetty mock = new APIMockJetty()) {
			mock.post("/", c -> {
				MyEntity value = c.readJson(MyEntity.class);
				assertEquals("Cool", value.getMsg());
				return "OK";
			});
			mock.start();
			URI uri = mock.getURI();
			HttpResponse resp = Request
					.Post(uri)
					.bodyString(
							"{\"msg\":\"Cool\"}",
							ContentType.create("application/json",
									StandardCharsets.UTF_8)).execute()
					.returnResponse();
			assertEquals(200, resp.getStatusLine().getStatusCode());
		}
	}

	@Test
	public void testUseSamePortTwice() throws Exception {
		int bindPort;
		try (APIMockJetty mock = new APIMockJetty()) {
			mock.post("/", c -> {
				MyEntity value = c.readJson(MyEntity.class);
				assertEquals("Cool", value.getMsg());
				return "OK";
			});
			mock.start();
			URI uri = mock.getURI();
			bindPort = mock.getPort();
			HttpResponse resp = Request
					.Post(uri)
					.bodyString(
							"{\"msg\":\"Cool\"}",
							ContentType.create("application/json",
									StandardCharsets.UTF_8)).execute()
					.returnResponse();
			assertEquals(200, resp.getStatusLine().getStatusCode());
		}
		try (APIMockJetty mock = new APIMockJetty()) {
			mock.post("/", c -> {
				MyEntity value = c.readJson(MyEntity.class);
				assertEquals("Cool", value.getMsg());
				return "OK";
			});
			mock.start(bindPort);
			URI uri = mock.getURI();
			HttpResponse resp = Request
					.Post(uri)
					.bodyString(
							"{\"msg\":\"Cool\"}",
							ContentType.create("application/json",
									StandardCharsets.UTF_8)).execute()
					.returnResponse();
			assertEquals(200, resp.getStatusLine().getStatusCode());
		}
	}

}
