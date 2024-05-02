package com.skcraft.launcher.auth.microsoft;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.skcraft.launcher.Launcher;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Log
public class OauthHttpHandler {
	private Executor executor = Executors.newCachedThreadPool();
	private HttpServer server;
	private OauthResult result;

	public OauthHttpHandler() throws IOException {
		server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);

		server.createContext("/", new Handler());
		server.setExecutor(executor);
		server.start();
	}

	public int getPort() {
		return server.getAddress().getPort();
	}

	public OauthResult await() throws InterruptedException {
		synchronized (this) {
			this.wait();
		}

		server.stop(3);

		return result;
	}

	private class Handler implements HttpHandler {
		@Override
		public void handle(HttpExchange httpExchange) throws IOException {
			String query = httpExchange.getRequestURI().getQuery();
			Map<String, String> qs = Splitter.on('&').withKeyValueSeparator('=').split(query);
			if (qs.get("error") != null) {
				result = new OauthResult.Error(qs.get("error_description"));
			} else {
				result = new OauthResult.Success(qs.get("code"));
			}

			synchronized (OauthHttpHandler.this) {
				OauthHttpHandler.this.notifyAll();
			}

			byte[] response;
			InputStream is = Launcher.class.getResourceAsStream("login.html");
			if (is != null) {
				response = IOUtils.toByteArray(is);
			} else {
				response = "Unable to fetch resource login.html".getBytes(Charsets.UTF_8);
			}

			InputStream iconStream = Launcher.class.getResourceAsStream("icon.png");
			if (iconStream != null) {
				byte[] iconBytes = IOUtils.toByteArray(iconStream);
				String encodedIcon = Base64.getEncoder().encodeToString(iconBytes);
				response = String.format(new String(response), encodedIcon).getBytes();
			} else {
				log.warning("Unable to fetch resource icon.png");
			}

			httpExchange.sendResponseHeaders(200, response.length);
			httpExchange.getResponseBody().write(response);
			httpExchange.getResponseBody().flush();
			httpExchange.getResponseBody().close();
		}
	}
}
