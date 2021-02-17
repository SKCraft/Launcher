package com.skcraft.launcher.auth;

import com.skcraft.launcher.util.HttpRequest;

import java.io.IOException;

import static com.skcraft.launcher.util.HttpRequest.url;

public class VisageSkinService {
	public static byte[] fetchSkinHead(String uuid) throws IOException, InterruptedException {
		String skinUrl = String.format("https://visage.surgeplay.com/face/32/%s.png", uuid);

		return HttpRequest.get(url(skinUrl))
				.execute()
				.expectResponseCode(200)
				.returnContent()
				.asBytes();
	}
}
