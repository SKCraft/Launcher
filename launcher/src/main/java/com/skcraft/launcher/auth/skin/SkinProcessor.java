package com.skcraft.launcher.auth.skin;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SkinProcessor {
	public static byte[] renderHead(byte[] skinData) throws IOException {
		BufferedImage skin = ImageIO.read(new ByteArrayInputStream(skinData));

		BufferedImage result = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
		Graphics graphics = result.getGraphics();

		// Draw bottom head layer
		graphics.drawImage(skin, 0, 0, 32, 32, 8, 8, 16, 16, null);
		// Draw top head layer
		graphics.drawImage(skin, 0, 0, 32, 32, 40, 8, 48, 16, null);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ImageIO.write(result, "png", outputStream);

		return outputStream.toByteArray();
	}
}
