package com.skcraft.launcher.auth.skin;

import com.skcraft.launcher.auth.microsoft.model.McProfileResponse;
import com.skcraft.launcher.util.HttpRequest;
import lombok.extern.java.Log;

import java.io.IOException;
import java.util.logging.Level;

@Log
public class MinecraftSkinService {
	static byte[] downloadSkin(String textureUrl) throws IOException, InterruptedException {
		return HttpRequest.get(HttpRequest.url(textureUrl))
				.execute()
				.expectResponseCode(200)
				.returnContent()
				.asBytes();
	}

	public static byte[] fetchSkinHead(McProfileResponse profile) throws InterruptedException {
		try {
			byte[] skin = downloadSkin(profile.getActiveSkin().getUrl());

			return SkinProcessor.renderHead(skin);
		} catch (IOException e) {
			log.log(Level.WARNING, "Failed to download or process skin.", e);
			return null;
		}
	}
}
