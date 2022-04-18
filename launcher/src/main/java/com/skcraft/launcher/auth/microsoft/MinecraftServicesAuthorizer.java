package com.skcraft.launcher.auth.microsoft;

import com.skcraft.launcher.auth.AuthenticationException;
import com.skcraft.launcher.auth.microsoft.model.*;
import com.skcraft.launcher.util.HttpRequest;
import com.skcraft.launcher.util.SharedLocale;

import java.io.IOException;
import java.net.URL;

import static com.skcraft.launcher.util.HttpRequest.url;

public class MinecraftServicesAuthorizer {
	private static final URL MC_SERVICES_LOGIN = url("https://api.minecraftservices.com/authentication/login_with_xbox");
	private static final URL MC_SERVICES_PROFILE = url("https://api.minecraftservices.com/minecraft/profile");

	public static McAuthResponse authorizeWithMinecraft(XboxAuthorization auth) throws IOException, InterruptedException {
		McAuthRequest request = new McAuthRequest("XBL3.0 x=" + auth.getCombinedToken());

		return HttpRequest.post(MC_SERVICES_LOGIN)
				.bodyJson(request)
				.header("Accept", "application/json")
				.execute()
				.expectResponseCode(200)
				.returnContent()
				.asJson(McAuthResponse.class);
	}

	public static McProfileResponse getUserProfile(McAuthResponse auth)
			throws IOException, InterruptedException, AuthenticationException {
		return getUserProfile(auth.getAuthorization());
	}

	public static McProfileResponse getUserProfile(String authorization)
			throws IOException, InterruptedException, AuthenticationException {
		return HttpRequest.get(MC_SERVICES_PROFILE)
				.header("Authorization", authorization)
				.execute()
				.expectResponseCodeOr(200, req -> {
					HttpRequest.BufferedResponse content = req.returnContent();
					if (content.asBytes().length == 0) {
						return new AuthenticationException("Got empty response from Minecraft services",
								SharedLocale.tr("login.minecraft.error", req.getResponseCode()));
					}

					McServicesError error = content.asJson(McServicesError.class);

					if (error.getError().equals("NOT_FOUND")) {
						return new AuthenticationException("No Minecraft profile",
								SharedLocale.tr("login.minecraftNotOwnedError"));
					}

					return new AuthenticationException(error.getErrorMessage(),
							SharedLocale.tr("login.minecraft.error", error.getErrorMessage()));
				})
				.returnContent()
				.asJson(McProfileResponse.class);
	}
}
