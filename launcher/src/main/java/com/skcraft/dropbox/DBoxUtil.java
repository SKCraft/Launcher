package com.skcraft.dropbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.DbxFiles.FileMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;



public class DBoxUtil {
	static final String ACCESS_TOKEN = "xnRdGgqjkwAAAAAAAAAACdx_I4-gFjKU38P1Y7SRFVM-gnPdHJ3gvwOl5YVfIMbo";
	private final ObjectMapper mapper = new ObjectMapper();
	
	DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial", "en_US");
	 DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
	
	 public  File jsonGeter (String dropboxLocal,String outputLocal) 
	{
		
		 //String outputLocal = dropboxLocal;
		
		try {
			
			outputLocal = "json";
			System.out.println(dropboxLocal);
			 outputLocal = "."+File.separator+"temp"+File.separator+outputLocal;
			 String theDirPath = "."+File.separator+"temp"+File.separator;
			 File theDir = new File(theDirPath);
			 if (!theDir.exists()) theDir.mkdir();
			
			
			 FileOutputStream downloadOut = new FileOutputStream(outputLocal);
			
			
			FileMetadata dow = client.files.downloadBuilder(dropboxLocal).run(downloadOut);
			//Path path = FileSystems.getDefault().getPath(theDirPath,theDirPath);
			//Files.delete(path);
			File file = new File(outputLocal);
			
			return file;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (DbxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		 
		 
			 
			 
			 
		 
		 
		 
		 return null;
		
	}
	 public <T> T DBoxasJson(String url,Class<T> cls) throws IOException {
         return mapper.readValue(asString(url), cls);
     }
	 public String asString(String url) throws IOException {
          return readFile(jsonGeter(url,url));
     }
	 private String readFile( File file ) throws IOException {
		    BufferedReader reader = new BufferedReader( new FileReader (file));
		    String         line = null;
		    StringBuilder  stringBuilder = new StringBuilder();
		    String         ls = System.getProperty("line.separator");

		    try {
		        while( ( line = reader.readLine() ) != null ) {
		            stringBuilder.append( line );
		            stringBuilder.append( ls );
		        }

		        return stringBuilder.toString();
		    } finally {
		        reader.close();
		    }
		}
}
