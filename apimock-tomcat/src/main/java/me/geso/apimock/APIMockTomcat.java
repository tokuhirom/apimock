package me.geso.apimock;

import java.io.File;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tokuhirom on 12/16/14.
 */
public class APIMockTomcat implements AutoCloseable {
	private static final Logger log = LoggerFactory.getLogger(APIMockTomcat.class);
	private Tomcat tomcat;
	private APIMockServlet servlet;

	public APIMockTomcat() {
		this.servlet = new APIMockServlet();
	}

	public APIMockTomcat post(String path, APIMockCallback callback) {
		log.info("Registering POST {}", path);
		this.servlet.post(path, callback);
		return this;
	}

	public APIMockTomcat get(String path, APIMockCallback callback) {
		log.info("Registering GET {}", path);
		this.servlet.get(path, callback);
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

	public void start(int port) throws LifecycleException {
		this.tomcat = new Tomcat();
		tomcat.setPort(port);
		org.apache.catalina.Context ctx = tomcat.addContext("/",
				new File(".").getAbsolutePath());
		Tomcat.addServlet(ctx, "mock", servlet);
		ctx.addServletMapping("/*", "mock");
		tomcat.start();
	}

	public int getPort() {
		if (this.tomcat == null) {
			throw new IllegalStateException("WTF");
		}
		return tomcat.getConnector().getLocalPort();
	}

	public String getURL() {
		return "http://127.0.0.1:" + this.getPort();
	}

	@Override
	public void close() throws Exception {
		if (this.tomcat != null) {
			this.tomcat.stop();
		}
	}
}
