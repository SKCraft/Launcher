package com.skcraft.launcher.util;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.skcraft.launcher.model.modpack.DownloadableFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
	public static DownloadableFile saveStreamToObjectsDir(InputStream stream, File outputDir) throws IOException {
		byte[] input = ByteStreams.toByteArray(stream);
		HashFunction hf = Hashing.sha1();

		String fileHash = hf.hashBytes(input).toString();
		String filePath = fileHash.substring(0, 2) + "/" + fileHash.substring(2, 4) + "/" + fileHash;

		File dest = new File(outputDir, filePath);
		dest.getParentFile().mkdirs();

		Files.write(input, dest);

		DownloadableFile entry = new DownloadableFile();
		entry.setLocation(filePath);
		entry.setHash(fileHash);
		entry.setSize(input.length);

		return entry;
	}

	public static String getShaHash(File file) throws IOException {
		FileInputStream stream = new FileInputStream(file);
		byte[] input = ByteStreams.toByteArray(stream);
		String res = Hashing.sha1().hashBytes(input).toString();

		stream.close();
		return res;
	}
}
