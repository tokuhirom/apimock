package me.geso.apimock;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APIMockTomcat implements AutoCloseable {
	private static final Logger log = LoggerFactory.getLogger(APIMockTomcat.class);
	private Tomcat tomcat;
	private APIMockServlet servlet;

	public APIMockTomcat() {
		this.servlet = new APIMockServlet();
	}

	/**
	 * Register new request handler for POST method.
	 * {@code mock.post("/", c -> "TOP");}
	 * @param path Handler path.
	 * @param callback Callback handler.
	 */
	public APIMockTomcat post(String path, APIMockCallback callback) {
		log.info("Registering POST {}", path);
		this.servlet.post(path, callback);
		return this;
	}

	/**
	 * Register new request handler for GET method.
	 * {@code mock.get("/", c -> "TOP");}
	 * @param path Handler path.
	 * @param callback Callback handler.
	 */
	public APIMockTomcat get(String path, APIMockCallback callback) {
		log.info("Registering GET {}", path);
		this.servlet.get(path, callback);
		return this;
	}

	/**
	 * Register new request handler for PUT method.
	 * {@code mock.put("/", c -> "TOP");}
	 * @param path Handler path.
	 * @param callback Callback handler.
	 */
	public APIMockTomcat put(String path, APIMockCallback callback) {
		log.info("Registering PUT {}", path);
		this.servlet.put(path, callback);
		return this;
	}

	/**
	 * Register new request handler for DELETE method.
	 * {@code mock.delete("/", c -> "TOP");}
	 * @param path Handler path.
	 * @param callback Callback handler.
	 */
	public APIMockTomcat delete(String path, APIMockCallback callback) {
		log.info("Registering DELETE {}", path);
		this.servlet.delete(path, callback);
		return this;
	}

	/**
	 * Run tomcat on empty port.
	 *
	 * @throws org.apache.catalina.LifecycleException
	 */
	public void start() throws LifecycleException {
		this.start(0);
	}

	/**
	 * Start new tomcat server instance.
	 *
	 * @param port
	 * @throws LifecycleException
	 */
	public void start(int port) throws LifecycleException {
		start(port, "mock");
	}

	/**
	 * Start new tomcat server instance with specifying its name.
	 *
	 * @param port
	 * @throws LifecycleException
	 */
	public void start(int port, String name) throws LifecycleException {
		if (this.tomcat != null) {
			throw new IllegalStateException(
					"Tomcat is already running.");
		}

		this.tomcat = new Tomcat();
		tomcat.setPort(port);
		org.apache.catalina.Context ctx = tomcat.addContext("/",
			new File(".").getAbsolutePath());
		Tomcat.addServlet(ctx, name, servlet);
		ctx.addServletMapping("/*", name);
		tomcat.start();
	}

	/**
	 * Get a port number from 
	 * @return
	 */
	public int getPort() {
		if (this.tomcat == null) {
			throw new IllegalStateException(
					"Tomcat is not running. Call `start` method first.");
		}
		return tomcat.getConnector().getLocalPort();
	}

	public URL getURL() {
		try {
			return new URL("http://127.0.0.1:" + this.getPort());
		} catch (MalformedURLException e) {
			// This never happen.
			throw new RuntimeException(e);
		}
	}

	public URI getURI() {
		try {
			return this.getURL().toURI();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() throws Exception {
		if (this.tomcat != null) {
			this.tomcat.stop();
		}
	}
}
