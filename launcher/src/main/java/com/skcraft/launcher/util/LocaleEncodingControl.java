package com.skcraft.launcher.util;

import lombok.extern.java.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

@Log
public class LocaleEncodingControl extends ResourceBundle.Control {
	@Override
	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
		if (!format.equals("java.properties")) {
			return super.newBundle(baseName, locale, format, loader, reload);
		}

		String bundleName = this.toBundleName(baseName, locale);
		String resourceName = this.toResourceName(bundleName, "properties");

		InputStream is = this.getResourceAsStream(resourceName, loader, reload);
		if (is == null) {
			return null;
		}

		// Let's do the timewalk
		boolean isUtf8;
		is.mark(3);
		{
			byte[] buf = new byte[3];
			int read = 0;
			while (read < 3) {
				read = is.read(buf);
			}

			// the BOM is 0xEF,0xBB,0xBF
			isUtf8 = buf[0] == (byte) 0xEF && buf[1] == (byte) 0xBB && buf[2] == (byte) 0xBF;
		}
		is.reset();

		if (isUtf8) {
			log.info("Found UTF-8 locale file " + resourceName);
		}

		Charset charset = isUtf8 ? StandardCharsets.UTF_8 : StandardCharsets.ISO_8859_1;
		Reader reader = new InputStreamReader(is, charset);

		try {
			PropertyResourceBundle bundle = new PropertyResourceBundle(reader);
			reader.close();

			return bundle;
		} finally {
			// Just in case of exception...
			reader.close();
		}
	}

	private InputStream getResourceAsStream(String resourceName, ClassLoader loader, boolean reload) throws IOException {
		if (reload) {
			URL url = loader.getResource(resourceName);
			if (url != null) {
				URLConnection conn = url.openConnection();
				if (conn != null) {
					conn.setUseCaches(false);

					return conn.getInputStream();
				}
			}
		}

		return loader.getResourceAsStream(resourceName);
	}
}
