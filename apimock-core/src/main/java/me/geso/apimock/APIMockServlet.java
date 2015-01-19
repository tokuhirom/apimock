package me.geso.apimock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.geso.routes.RoutingResult;
import me.geso.routes.WebRouter;
import me.geso.webscrew.response.WebResponse;

public class APIMockServlet extends HttpServlet {
	private static Logger log = LoggerFactory.getLogger(APIMockServlet.class);
	private static final long serialVersionUID = 1L;

	private WebRouter<APIMockCallback> router;

	@Override
	public void init() throws ServletException {
		log.info("Initialized: {}", router);
	}

	public APIMockServlet() {
		this.router = new WebRouter<>();
	}

	public void get(String path, APIMockCallback callback) {
		router.get(path, callback);
	}

	public void post(String path, APIMockCallback callback) {
		router.post(path, callback);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		log.info("{} {}", req.getMethod(), req.getPathInfo());
		RoutingResult<APIMockCallback> match = this.router.match(req.getMethod(), req.getPathInfo());
		if (match != null) {
			if (match.methodAllowed()) {
				APIMockContext c = new APIMockContext(req);
				try {
					Object ret = match.getDestination().run(c);
					if (ret instanceof WebResponse) {
						((WebResponse) ret).write(resp);
					} else {
						// WebResponse 以外は全部 JSON として jackson でシリアライズしていく
						resp.setStatus(200);
						resp.setContentType("application/json; charset=utf-8");

						// Output JSON
						ObjectMapper mapper = new ObjectMapper();
						mapper.writeValue(resp.getOutputStream(), ret);
					}
				} catch (Exception e) {
					log.error("Internal server error: {} {} : {} {}",
							req.getMethod(), req.getPathInfo(), e.getClass().getName(), e.getMessage());
					e.printStackTrace();;

					resp.setStatus(500);
					resp.getWriter().write(
							new StringBuilder()
							.append(e.getClass().getName())
							.append(" : ")
							.append(e.getMessage())
							.toString()
					);
				}
			} else {
				log.error("405 method not allowed: {}, {}", req.getMethod(), req.getPathInfo());
				resp.setStatus(405);
				resp.getWriter().write("Method not allowed.");
			}
		} else {
			router.getPatterns().forEach(route -> {
				log.info("{}", route.getPath());
			});
			log.error("404 not found: {} not matched for {}", req.getPathInfo(), router.getPatterns());

			resp.setStatus(404);
			resp.getWriter().write("Not found");
		}
	}
}
