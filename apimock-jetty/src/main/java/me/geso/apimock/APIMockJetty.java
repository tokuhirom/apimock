package me.geso.apimock;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.Jetty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class APIMockJetty implements AutoCloseable {
	private static final Logger log = LoggerFactory.getLogger(APIMockJetty.class);
	private Server server;
	private APIMockServlet servlet;

	public APIMockJetty() {
		this.servlet = new APIMockServlet();
	}

	/**
	 * Register new request handler for POST method.
	 * {@code mock.post("/", c -> "TOP");}
	 * @param path Handler path.
	 * @param callback Callback handler.
	 */
	public APIMockJetty post(String path, APIMockCallback callback) {
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
	public APIMockJetty get(String path, APIMockCallback callback) {
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
	public APIMockJetty put(String path, APIMockCallback callback) {
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
	public APIMockJetty delete(String path, APIMockCallback callback) {
		log.info("Registering DELETE {}", path);
		this.servlet.delete(path, callback);
		return this;
	}

	/**
	 * Run tomcat on empty port.
	 */
	public void start() throws Exception {
		this.start(0);
	}

	/**
	 * Start new tomcat server instance.
	 *
	 * @param port
	 */
	public void start(int port) throws Exception {
		if (this.server != null) {
			throw new IllegalStateException(
					"Jetty is already running.");
		}

		this.server = new Server(port);
		String contextPath = "/";
		ServletContextHandler context = new ServletContextHandler(
				server,
				contextPath,
				ServletContextHandler.SESSIONS
		);
		context.addServlet(new ServletHolder(servlet), "/*");
		this.server.start();
	}

	/**
	 * Get a port number from 
	 * @return
	 */
	public int getPort() {
		if (this.server == null) {
			throw new IllegalStateException(
					"Jetty is not running. Call `start` method first.");
		}
		ServerConnector connector = (ServerConnector)server
				.getConnectors()[0];
		return connector.getLocalPort();
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
		if (this.server != null) {
			this.server.stop();
			this.server.destroy();
		}
	}
}
