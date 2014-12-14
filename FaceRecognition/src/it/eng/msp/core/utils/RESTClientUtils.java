package it.eng.msp.core.utils;

import it.eng.msp.core.model.FaceMatch;
import it.eng.msp.core.model.FaceMatch2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;


public class RESTClientUtils {
	
	private static final String TAG = "RESTClientUtils";

	private HttpClient httpclient = null;
	private HttpPost httppost = null;

	private RESTClientUtils(String endpoint) {
		httpclient = new DefaultHttpClient();
		httppost = new HttpPost(endpoint);
	}

	public static RESTClientUtils getInstance(String endpoint) {
		return new RESTClientUtils(endpoint);
	}

	public static String extractHTMLBody (HttpResponse response) {
		try {

			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			StringBuilder builder = new StringBuilder();
			for (String line = null; (line = reader.readLine()) != null;) {
				builder.append(line).append("\n");
			}

			return builder.toString();

		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "UnsupportedEncodingException in ENG Verification ", e);
			return null;
		} catch (IllegalStateException e) {
			Log.e(TAG, "IllegalStateException in ENG Verification ", e);
			return null;
		} catch (IOException e) {
			Log.e(TAG, "IOException in ENG Verification ", e);
			return null;
		}
	}
	
	public HttpResponse postElement(FaceMatch2 elem) {
		try {
			
			StringEntity se = new StringEntity( faceMathToHTTPBodyXML ( elem ), HTTP.UTF_8 );
			se.setContentType("application/xml");

			httppost.setEntity(se);

			HttpResponse response = httpclient.execute(httppost);
			return response;

		} catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolException in ENG Verification ", e);
			return null;
		} catch (IOException e) {
			Log.e(TAG, "IOException in ENG Verification ", e);
			return null;
		} catch (Exception e) {
			Log.e(TAG, "Exception in ENG Verification ", e);
			return null;
		}
	}
	
	public HttpResponse postElement(FaceMatch elem) {
		try {
			
			StringEntity se = new StringEntity( faceMathToHTTPBodyXML ( elem ), HTTP.UTF_8 );
			se.setContentType("application/xml");

			httppost.setEntity(se);

			HttpResponse response = httpclient.execute(httppost);
			return response;

		} catch (ClientProtocolException e) {
			Log.e(TAG, "ClientProtocolException in ENG Verification ", e);
			return null;
		} catch (IOException e) {
			Log.e(TAG, "IOException in ENG Verification ", e);
			return null;
		} catch (Exception e) {
			Log.e(TAG, "Exception in ENG Verification ", e);
			return null;
		}
	}

	private String faceMathToHTTPBodyXML(FaceMatch2 faces){

		StringBuilder sb = new StringBuilder();

		/* header body xml*/
		sb.append("<?xml")
		.append("\n\t").append("version=\"1.0\"")
		.append("\n\t").append("encoding=\"UTF-8\"")
		.append("\n\t").append("standalone=\"yes\"")
		.append("\n").append("?>").append("\n");

		/* serializzazione classe FaceMatch */        
		sb.append("<faceMatch>")
		.append("\n\t").append("<sampleIMG>")
		.append("\n\t").append("\t").append( byteArrayToHEXString ( faces.getSampleIMG() ) )
		.append("\n\t").append("</sampleIMG>")
		.append("\n\t").append("<success>")
		.append("\n\t").append("\t").append("false")
		.append("\n\t").append("</success>")
		.append("\n\t").append("<persoName>")
		.append("\n\t").append("\t").append( faces.getPersoName() )
		
		.append("\n\t").append("</persoName>")
		.append("\n").append("</faceMatch>");
		String data = sb.toString();
//		Log.v(TAG, data);
		return data;
	}
	
	private String faceMathToHTTPBodyXML(FaceMatch faces){

		StringBuilder sb = new StringBuilder();

		/* header body xml*/
		sb.append("<?xml")
		.append("\n\t").append("version=\"1.0\"")
		.append("\n\t").append("encoding=\"UTF-8\"")
		.append("\n\t").append("standalone=\"yes\"")
		.append("\n").append("?>").append("\n");

		/* serializzazione classe FaceMatch */        
		sb.append("<faceMatch>")
		.append("\n\t").append("<sampleIMG>")
		.append("\n\t").append("\t").append( byteArrayToHEXString ( faces.getSampleIMG() ) )
		.append("\n\t").append("</sampleIMG>")
		.append("\n\t").append("<success>")
		.append("\n\t").append("\t").append("false")
		.append("\n\t").append("</success>")
		.append("\n\t").append("<templateIMG>")
		.append("\n\t").append("\t").append( byteArrayToHEXString ( faces.getTemplateIMG() ) )
		.append("\n\t").append("</templateIMG>")
		.append("\n").append("</faceMatch>");
		String data = sb.toString();
//		Log.v(TAG, data);
		return data;
	}

	public static String byteArrayToHEXString(byte[] array){

		String base64String = Base64.encodeToString(array, Base64.DEFAULT);
		return base64String;
	}
	
	public static String downloadImage(String urlImage, String outPath){
		InputStream in =null;
		int responseCode = -1;
		try{

			URL url = new URL(urlImage);//"http://192.xx.xx.xx/mypath/img1.jpg
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setDoInput(true);
			con.connect();
			responseCode = con.getResponseCode();
			if(responseCode == HttpURLConnection.HTTP_OK)
			{
				//download 
				in = con.getInputStream();
				Bitmap bmp = BitmapFactory.decodeStream(in);
				in.close();

				File filename = new File(outPath);
				FileOutputStream out = new FileOutputStream(filename);
				bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
				out.close();

//				Log.v(TAG, "img scaricata in: " + outPath);
			}

			return outPath;

		} catch(Exception ex) {
			Log.e(TAG, "Exception download image: " + urlImage, ex);
			return null;
		}
	}
}
