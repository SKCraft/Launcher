package com.skcraft.launcher.auth.microsoft;

import com.skcraft.launcher.auth.microsoft.model.*;
import com.skcraft.launcher.util.HttpRequest;

import java.io.IOException;
import java.net.URL;

import static com.skcraft.launcher.util.HttpRequest.url;

public class XboxTokenAuthorizer {
	private static final URL XBL_AUTHENTICATE_URL = url("https://user.auth.xboxlive.com/user/authenticate");
	private static final URL XSTS_AUTHENTICATE_URL = url("https://xsts.auth.xboxlive.com/xsts/authorize");

	public static XboxAuthorization authorizeWithXbox(String accessToken) throws IOException, InterruptedException {
		XboxAuthRequest<XblAuthProperties> xblPayload =
				new XboxAuthRequest<>(new XblAuthProperties("d=" + accessToken));

		XboxAuthResponse xblResponse = HttpRequest.post(XBL_AUTHENTICATE_URL)
				.bodyJson(xblPayload)
				.header("Accept", "application/json")
				.execute()
				.expectResponseCode(200)
				.returnContent()
				.asJson(XboxAuthResponse.class);

		XboxAuthRequest<XstsAuthProperties> xstsPayload =
				new XboxAuthRequest<>(new XstsAuthProperties(xblResponse.getToken()));
		xstsPayload.setRelyingParty("rp://api.minecraftservices.com/");

		XboxAuthResponse xstsResponse = HttpRequest.post(XSTS_AUTHENTICATE_URL)
				.bodyJson(xstsPayload)
				.header("Accept", "application/json")
				.execute()
				.expectResponseCode(200)
				.returnContent()
				.asJson(XboxAuthResponse.class);

		return new XboxAuthorization(xstsResponse.getToken(), xstsResponse.getUhs());
	}
}
