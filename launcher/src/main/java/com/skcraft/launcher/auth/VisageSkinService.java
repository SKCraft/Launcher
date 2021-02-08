package com.skcraft.launcher.auth;

import com.skcraft.launcher.util.HttpRequest;

import java.io.IOException;
import java.util.Base64;

import static com.skcraft.launcher.util.HttpRequest.url;

public class VisageSkinService {
	public static String fetchSkinHead(String uuid) throws IOException, InterruptedException {
		String skinUrl = String.format("https://visage.surgeplay.com/face/32/%s.png", uuid);

		byte[] skinBytes = HttpRequest.get(url(skinUrl))
				.execute()
				.expectResponseCode(200)
				.returnContent()
				.asBytes();

		return Base64.getEncoder().encodeToString(skinBytes);
	}
}
