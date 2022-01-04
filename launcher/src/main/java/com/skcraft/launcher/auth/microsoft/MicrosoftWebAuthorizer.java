package com.skcraft.launcher.auth.microsoft;

import com.skcraft.launcher.auth.AuthenticationException;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.HttpRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Handles the Microsoft leg of OAuth authorization.
 */
@RequiredArgsConstructor
public class MicrosoftWebAuthorizer {
	private final String clientId;
	@Getter private String redirectUri;

	public OauthResult authorize() throws IOException, AuthenticationException, InterruptedException {
		if (Desktop.isDesktopSupported()) {
			// Interactive auth
			return authorizeInteractive();
		} else {
			// TODO Device code auth
			return null;
		}
	}

	private OauthResult authorizeInteractive() throws IOException, AuthenticationException, InterruptedException {
		OauthHttpHandler httpHandler = new OauthHttpHandler();
		SwingHelper.openURL(generateInteractiveUrl(httpHandler.getPort()));

		return httpHandler.await();
	}

	private URI generateInteractiveUrl(int port) throws AuthenticationException {
		redirectUri = "http://localhost:" + port;

		URI interactive;
		try {
			HttpRequest.Form query = HttpRequest.Form.form();
			query.add("client_id", clientId);
			query.add("scope", "XboxLive.signin XboxLive.offline_access");
			query.add("response_type", "code");
			query.add("redirect_uri", redirectUri);
			query.add("prompt", "select_account");

			interactive = new URI("https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize?"
					+ query.toString());
		} catch (URISyntaxException e) {
			throw new AuthenticationException(e, "Failed to generate OAuth URL");
		}

		return interactive;
	}
}
