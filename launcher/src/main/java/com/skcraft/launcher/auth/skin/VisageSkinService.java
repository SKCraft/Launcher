package com.skcraft.launcher.auth.skin;

import com.skcraft.launcher.util.HttpRequest;
import lombok.extern.java.Log;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.logging.Level;

import static com.skcraft.launcher.util.HttpRequest.url;

@Log
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
			log.log(Level.WARNING, "Failed to download or process skin from Visage.", e);
			return null;
		}
	}
}
