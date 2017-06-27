package de.deeps.modules.webserver;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import de.deeps.Utils;

/**
 * @author Deeps
 */

public class FileHandler extends MyHttpHandler {

	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		String requestURI = httpExchange.getRequestURI().toString();
		if (requestURI.contains("?")) {
			requestURI = requestURI.substring(0, requestURI.indexOf('?'));
		}
		addMimeTypeToHeader(requestURI, httpExchange.getResponseHeaders());
		try {
			sendResponseWithContent(
				httpExchange,
				Utils.readBytesFromFile(
					new File(Website.WEBSERVER_ROOT_DIR + requestURI)));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private void addMimeTypeToHeader(String requestURI,
			Headers responseHeaders) {
		if (requestURI.endsWith(".js")) {
			responseHeaders.add("Content-Type", "application/javascript");
		}
		if (requestURI.endsWith(".png")) {
			responseHeaders.add("Content-Type", "image/png");
		}
		if (requestURI.endsWith(".css")) {
			responseHeaders.add("Content-Type", "text/css");
		}
		if (requestURI.endsWith(".woff2")) {
			responseHeaders.add("Content-Type", "font/woff2");
		}
		if (requestURI.endsWith(".woff")) {
			responseHeaders.add("Content-Type", "font/woff");
		}
		if (requestURI.endsWith(".ttf")) {
			responseHeaders.add("Content-Type", "font/ttf");
		}
		if (requestURI.endsWith(".eot")) {
			responseHeaders.add("Content-Type", "font/eot");
		}
	}

}
