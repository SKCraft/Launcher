package com.skcraft.launcher.auth.skin;

import com.skcraft.launcher.util.HttpRequest;

import javax.annotation.Nullable;
import java.io.IOException;

import static com.skcraft.launcher.util.HttpRequest.url;

public class VisageSkinService {
	@Nullable
	public static byte[] fetchSkinHead(String uuid) throws InterruptedException {
		String skinUrl = String.format("https://visage.surgeplay.com/face/32/%s.png", uuid);

		try {
			return HttpRequest.get(url(skinUrl))
					.execute()
					.expectResponseCode(200)
					.returnContent()
					.asBytes();
		} catch (IOException e) {
			return null;
		}
	}
}
