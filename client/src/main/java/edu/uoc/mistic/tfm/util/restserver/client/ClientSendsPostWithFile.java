package edu.uoc.mistic.tfm.util.restserver.client;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class ClientSendsPostWithFile {

	public static void main(String[] args) throws ClientProtocolException, IOException {
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost("http://localhost:8080/facturas/upload");
//		httpPost.addHeader("Content-Type", "application/json");
//	    httpPost.setHeader("Accept", "application/json");

		File file = new File("/Users/jherranzm/Downloads/Invoice_990001.xml.Enveloped-Sign.xml");
	    FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
	    StringBody stringBody1 = new StringBody(generateRandomString(10), ContentType.MULTIPART_FORM_DATA);
	    // 
	    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
	    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
	    builder.addPart("files", fileBody);
	    builder.addPart("numFactura", stringBody1);
	    HttpEntity entity = builder.build();	    httpPost.setEntity(entity);
	    
	    System.out.println(httpPost.getURI().toString());
	    
		CloseableHttpResponse response = client.execute(httpPost);
		if(response.getStatusLine().getStatusCode() != 200) {
			System.out.println(response.toString());
		}
		assert(200 == response.getStatusLine().getStatusCode());
		System.out.println(response.getEntity());
		client.close();
	}
	
	private static String generateRandomString(int count) {
	    return RandomStringUtils.randomAlphanumeric(count);
	}
	@SuppressWarnings("unused")
	private static int generateRandomInt() {
	    return ThreadLocalRandom.current().nextInt(1, 1000 + 1);
	}
}
