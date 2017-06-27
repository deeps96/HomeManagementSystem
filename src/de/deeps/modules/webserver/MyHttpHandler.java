package de.deeps.modules.webserver;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * @author Deeps
 */

public abstract class MyHttpHandler implements HttpHandler {

	protected void sendErrorResponse(HttpExchange httpExchange, String error)
			throws IOException {
		httpExchange.sendResponseHeaders(400, error.getBytes().length);
		OutputStream os = httpExchange.getResponseBody();
		os.write(error.getBytes(), 0, error.getBytes().length);
		os.flush();
		os.close();
		httpExchange.close();
	}

	protected void sendResponseWithoutContent(HttpExchange httpExchange)
			throws IOException {
		httpExchange.sendResponseHeaders(204, -1);
		httpExchange.close();
	}

	protected void sendResponseWithContent(HttpExchange httpExchange,
			byte[] content) throws IOException {
		httpExchange.sendResponseHeaders(200, content.length);
		OutputStream os = httpExchange.getResponseBody();
		os.write(content, 0, content.length);
		os.flush();
		os.close();
		httpExchange.close();
	}

}
