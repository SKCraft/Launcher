package com.skcraft.plugin.curse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.skcraft.launcher.util.HttpRequest;
import com.skcraft.plugin.curse.model.CurseProject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.skcraft.launcher.util.HttpRequest.url;

public class CurseApi {
	private static final String CURSE_ADDON_SEARCH = "https://addons-ecs.forgesvc.net/api/v2/addon/search?";
	private static final String MINECRAFT_GAME_ID = "432";
	private static final String CURSE_MOD_SECTION_ID = "6";

	public static List<CurseProject> searchForProjects(String query, String gameVersion) throws IOException, InterruptedException {
		HttpRequest.Form form = HttpRequest.Form.form();
		form.add("gameID", MINECRAFT_GAME_ID);
		form.add("sectionId", CURSE_MOD_SECTION_ID); // Filter to mods only
		form.add("gameVersion", gameVersion);
		form.add("searchFilter", query);

		try {
			URI uri = new URI(CURSE_ADDON_SEARCH + form.toString());

			return HttpRequest.get(uri.toURL())
					.execute()
					.expectResponseCode(200)
					.returnContent()
					.asJson(new TypeReference<List<CurseProject>>() {});
		} catch (URISyntaxException | MalformedURLException e) {
			// Shhhh.
			throw new RuntimeException(e);
		}
	}

	public static CurseProject getById(String projectId) throws IOException, InterruptedException {
		String addonUrl = String.format("https://addons-ecs.forgesvc.net/api/v2/addon/%s", projectId);
		return HttpRequest.get(url(addonUrl))
				.execute()
				.expectResponseCode(200)
				.returnContent()
				.asJson(CurseProject.class);
	}
}
