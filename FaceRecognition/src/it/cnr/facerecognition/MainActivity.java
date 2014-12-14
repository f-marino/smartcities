package it.cnr.facerecognition;

import it.eng.msp.core.model.FaceMatch;
import it.eng.msp.core.model.FaceMatch2;
import it.eng.msp.core.utils.FileUtils;
import it.eng.msp.core.utils.ParamUtils;
import it.eng.msp.core.utils.RESTClientUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private static final String TAG = "MainActivity";
	
	private static final int CAPTURE_SAMPLE_IMG_REQUEST_CODE = 100;
	private static final int CAPTURE_TEMPLATE_IMG_REQUEST_CODE = 101;
	final private static int SIFT = 0;
	final private static int EIGENFACES = 1;
	final private static int ONEFACE = 0;
	final private static int TWOFACES = 1;
	final private static int MAX_DIMENSION = 350;
	
	private ArrayAdapter<String> adapter1, adapter2;
	private final String[] array_spinner1 = { "SIFT", "EigenFaces" };
	private final String[] array_spinner2 = { "1 face", "2 faces" };
	
	private static SharedPreferences settings;
	private static String serverAddress;
	private static String serverPort;
    private static int selectedAlgorithm;
    private static int selectedMode;
	
	private static Uri SAMPLE_PHOTO_URI;
	private static Uri TEMPLATE_PHOTO_URI;

	private ProgressDialog mProgressBarHorizontal;
	private Bitmap pic;
	


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
		adapter1 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, array_spinner1);
		adapter2 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, array_spinner2);
		
		SAMPLE_PHOTO_URI = Uri.fromFile(new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "sample_img.jpg"));
		TEMPLATE_PHOTO_URI = Uri.fromFile(new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "template_img.jpg"));
        
        Button recognize = (Button)findViewById(R.id.recognize);
        
        recognize.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				sendIntentToCamera(CAPTURE_SAMPLE_IMG_REQUEST_CODE);
			}
        	
        });
        
        ImageButton takePhoto = (ImageButton)findViewById(R.id.takePhoto);
        
        takePhoto.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				sendIntentToCamera(CAPTURE_TEMPLATE_IMG_REQUEST_CODE);
			}
        	
        });
        
        settings = getPreferences(MODE_PRIVATE);
        serverAddress = settings.getString("serverAddress", "192.168.1.7");
        serverPort = settings.getString("serverPort", "8080");
        selectedAlgorithm = settings.getInt("algorithm", SIFT);
        selectedMode = settings.getInt("mode", ONEFACE);
        
        setLayout();
    }
    
    private void setLayout(){
    	switch(selectedMode){
    		case ONEFACE:
    			
    			findViewById(R.id.templateIMG).setVisibility(View.INVISIBLE);
    			findViewById(R.id.takePhoto).setVisibility(View.INVISIBLE);
    			findViewById(R.id.selectPhoto).setVisibility(View.INVISIBLE);
    			
    			break;
    	
			case TWOFACES:
				findViewById(R.id.name).setVisibility(View.INVISIBLE);
				findViewById(R.id.message).setVisibility(View.INVISIBLE);
				
				findViewById(R.id.templateIMG).setVisibility(View.VISIBLE);
				findViewById(R.id.takePhoto).setVisibility(View.VISIBLE);
//				findViewById(R.id.selectPhoto).setVisibility(View.VISIBLE);
				
				if(new File(TEMPLATE_PHOTO_URI.getPath()).exists())
					((ImageView)findViewById(R.id.templateIMG)).setImageBitmap(
							BitmapFactory.decodeFile(TEMPLATE_PHOTO_URI.getPath()));
				
				break;
    	}
    }
    
    private void sendIntentToCamera(int request_code){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        
        switch(request_code){
        case CAPTURE_SAMPLE_IMG_REQUEST_CODE:
        	intent.putExtra(MediaStore.EXTRA_OUTPUT, SAMPLE_PHOTO_URI);
        	break;
        case CAPTURE_TEMPLATE_IMG_REQUEST_CODE:
        	intent.putExtra(MediaStore.EXTRA_OUTPUT, TEMPLATE_PHOTO_URI);
        	break;
        }
        
        startActivityForResult(intent, request_code);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
        	showSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
	private void showSettings() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Settings");

		final View view = getLayoutInflater().inflate(R.layout.menu_layout, null);
		final Spinner algorithm = (Spinner) view.findViewById(R.id.spinner1);
		final Spinner mode = (Spinner) view.findViewById(R.id.spinner2);

		algorithm.setAdapter(adapter1);
		algorithm.setSelection(selectedAlgorithm);
		
		mode.setAdapter(adapter2);
		mode.setSelection(selectedMode);

		((TextView) (view.findViewById(R.id.editTextServerAddress)))
				.setText(serverAddress);
		
		((TextView) (view.findViewById(R.id.editTextServerPort)))
		.setText(serverPort);

		alert.setView(view);


		// Set an EditText view to get user input

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				SharedPreferences.Editor editor = settings.edit();

				selectedAlgorithm = algorithm.getSelectedItemPosition();
				editor.putInt("algorithm", selectedAlgorithm);

				serverAddress = ((EditText)view.findViewById(R.id.editTextServerAddress)).getText().toString();
				editor.putString("serverAddress", serverAddress);
				
				serverPort = ((EditText)view.findViewById(R.id.editTextServerPort)).getText().toString();
				editor.putString("serverPort", serverPort);

				selectedMode = mode.getSelectedItemPosition();
				editor.putInt("mode", selectedMode);

				editor.commit();
				
				setLayout();
				}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						// Canceled.
					}
				});
		
		alert.show();

	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	switch(requestCode){
    		case (CAPTURE_SAMPLE_IMG_REQUEST_CODE):
    			
                if (resultCode == RESULT_OK) {
                	
                	normalizeCapturedImage(SAMPLE_PHOTO_URI);
    	
                	mProgressBarHorizontal = ProgressDialog.show(this, "", "Face recognition in progress...");
                	
                	((Button)findViewById(R.id.recognize)).setVisibility(View.INVISIBLE);
                	((TextView)findViewById(R.id.message)).setText("");
                	((TextView)findViewById(R.id.name)).setText("");
                	
                    FaceRecognizerExecutor test = new FaceRecognizerExecutor();
                    test.execute("");
        			
                } else if (resultCode == RESULT_CANCELED) {
                	Log.d("debug", "User cancelled the image capture");
                } else {
                	Log.d("debug", "Image capture failed, advise user");
                }
    			break;
    			
    		   case (CAPTURE_TEMPLATE_IMG_REQUEST_CODE):
    			   
    			   normalizeCapturedImage(TEMPLATE_PHOTO_URI);
    		   	   ((ImageView)findViewById(R.id.templateIMG)).setImageBitmap(pic);
    		
            }


    	}
    	
    private void normalizeCapturedImage(Uri photo_uri){
    	ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
    	pic = BitmapFactory.decodeFile(photo_uri.getPath());
    	
    	double max = Math.max(pic.getWidth(), pic.getHeight());
    	double scale_factor = MAX_DIMENSION / max;
    	
    	pic = Bitmap.createScaledBitmap(pic, (int)(pic.getWidth() * scale_factor), (int)(pic.getHeight() * scale_factor), false);
    	
        Matrix matrix = new Matrix();
        matrix.postRotate(-90);
        pic =  Bitmap.createBitmap(pic, 0, 0, pic.getWidth(), pic.getHeight(), matrix, true);
    	
    	pic.compress(Bitmap.CompressFormat.JPEG, 100, baos);
    	
    	OutputStream outputStream;
		try {
			outputStream = new FileOutputStream (photo_uri.getPath());
			try {
				baos.writeTo(outputStream);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
    }

	public class FaceRecognizerExecutor extends AsyncTask<String, Void, String>{

		private static final String TAG = "FaceRecognizerExecutor";
		private String endpoint = "http://localhost:8080/ImageMatching/rest/processing/authentication";
		private final String UNIDENTIFIED_USER_MSG = "Unidentified user";
		
		public FaceRecognizerExecutor() {
		}

		@Override
		protected String doInBackground(String... params) {
			try{
				
				byte[] sampleIMG = FileUtils.toByteArray(new File(SAMPLE_PHOTO_URI.getPath()));
				String result = null;
				
				switch(selectedMode){
					case ONEFACE:
						FaceMatch2 faces1 = new FaceMatch2();
						faces1.setSampleIMG(sampleIMG);
						result = verifyFaces(faces1);
						
						break;
					case TWOFACES:
						FaceMatch faces2 = new FaceMatch();
						faces2.setSampleIMG(sampleIMG);
						faces2.setTemplateIMG(FileUtils.toByteArray(new File(TEMPLATE_PHOTO_URI.getPath())));
						result = verifyFaces(faces2);
						
						break;
				}
				
				return result;
			}catch(Exception e){
				Log.e(TAG, "Exception ",e);

			}
			return null;
		}
		
		
		
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			mProgressBarHorizontal.dismiss();
			
			TextView message = (TextView)findViewById(R.id.message);
			TextView message2 = (TextView)findViewById(R.id.message2);
			
			if(result.equals(UNIDENTIFIED_USER_MSG)){
				switch(selectedMode){
					case ONEFACE:
						message.setTextColor(Color.parseColor("#FF0000"));
						message.setText(UNIDENTIFIED_USER_MSG);
						message.setVisibility(View.VISIBLE);
						
						break;
					case TWOFACES:
						message2.setTextColor(Color.parseColor("#FF0000"));
						message2.setText(UNIDENTIFIED_USER_MSG);
						message2.setVisibility(View.VISIBLE);
						break;
				}
			}
			else{
				switch(selectedMode){
					case ONEFACE: 
						message.setTextColor(Color.parseColor("#33CC33"));
						message.setText(result);
						message.setVisibility(View.VISIBLE);
						
						TextView name = (TextView)findViewById(R.id.name);
						name.setTextColor(Color.parseColor("#33CC33"));
						name.setText("User identified");
						name.setVisibility(View.VISIBLE);
						break;
					case TWOFACES: 
						message2.setTextColor(Color.parseColor("#33CC33"));
						message2.setText("User identified");
						message2.setVisibility(View.VISIBLE);
						
						break;
				}
			}
			
			((Button)findViewById(R.id.recognize)).setVisibility(View.VISIBLE);
			
		}


		public void setHostEndPort(String host, String port) {
			endpoint = endpoint.replace("localhost", host);
			endpoint = endpoint.replace("8080", port);
		}

		public String verifyFaces(FaceMatch2 face) {


			setHostEndPort(serverAddress, serverPort);

			HttpResponse response = null;
			
			switch(selectedAlgorithm){
				case SIFT: 		
					response = RESTClientUtils.getInstance(ParamUtils.addPathSegment(
						endpoint, "verify1FaceWithProcessing")).postElement(face);
					break;
				case EIGENFACES:				
					response = RESTClientUtils.getInstance(ParamUtils.addPathSegment(
						endpoint, "verify1FaceWithEigenFaces")).postElement(face);
					break;
			}


			try{

				JSONObject obj = new JSONObject( RESTClientUtils.extractHTMLBody(response) );

				boolean result = obj.getBoolean("completed") && obj.getBoolean("success");
				
				if(result) return obj.getString("user");
				else return UNIDENTIFIED_USER_MSG;

			} catch (JSONException e) {
				Log.e(TAG, "JSONException in ENG Verification ", e);
				return UNIDENTIFIED_USER_MSG;
			}
		}
		
		public String verifyFaces(FaceMatch face) {


			setHostEndPort(serverAddress, serverPort);

			HttpResponse response = null;
			
			switch(selectedAlgorithm){
				case SIFT: 		
					response = RESTClientUtils.getInstance(ParamUtils.addPathSegment(
						endpoint, "verifyFacesWithProcessing")).postElement(face);
					break;
				case EIGENFACES:				
					response = RESTClientUtils.getInstance(ParamUtils.addPathSegment(
						endpoint, "verifyFacesWithEigenFaces")).postElement(face);
					break;
			}


			try{

				JSONObject obj = new JSONObject( RESTClientUtils.extractHTMLBody(response) );

				boolean result = obj.getBoolean("completed") && obj.getBoolean("success");
				
				if(result) return obj.getString("user");
				else return UNIDENTIFIED_USER_MSG;

			} catch (JSONException e) {
				Log.e(TAG, "JSONException in ENG Verification ", e);
				return UNIDENTIFIED_USER_MSG;
			}
		}


	}
   
}
