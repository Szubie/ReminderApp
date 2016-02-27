package kwan.com.smartreminder;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private Button SendBtn;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    static boolean active = false;
    TextToSpeech t1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SendBtn = (Button) findViewById(R.id.Send);
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

        // hide the action bar
       // getActionBar().hide();
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                promptSpeechInput();
            }
        });
        SendBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String query ="";
                String result="";
                String JsonResult="";
                String SpeechText="";
                String subject="";
                String preposition="";
                String object="";
                String verb="";
                JSONAsyncTask task= new JSONAsyncTask();
                try {
                     query = URLEncoder.encode(txtSpeechInput.getText().toString(), "utf-8");
                }
                catch (UnsupportedEncodingException e){

                }

                try {
                    if (query != "") {
                         result = task.execute("http://192.168.43.132:1337/Speech/" + query).get();
                    }
                }
                catch (InterruptedException e){

                }
                catch (ExecutionException e){

                }
               //result= result.replace("+"," ");
                try {
                     JSONObject obj = new JSONObject(result);
                     JsonResult =obj.getString("result");
                     subject =obj.getString("subject");
                    verb =obj.getString("verb");
                    preposition =obj.getString("preposition");
                    object =obj.getString("object");

                }
                catch (JSONException e){

                }
                if(JsonResult.equals("ok")){
                    SpeechText = "Data insert ok!" ;
                }
                if(subject!=null){
                    SpeechText = subject+" "+verb+" "+preposition+" "+object ;
                }
                t1.speak(SpeechText, TextToSpeech.QUEUE_FLUSH, null);

            }

        });
     }



    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.items, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);

        switch(item.getItemId()){
            case R.id.home:
                Toast.makeText(getBaseContext(), "Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.ac2:
                Intent myintent2 = new Intent(this,Main2Activity.class);
                startActivityForResult(myintent2, 0);
                Toast.makeText(getBaseContext(), "This is Ac2", Toast.LENGTH_SHORT).show();
                break;

            case R.id.ac3:
                Toast.makeText(getBaseContext(), "This is Ac3", Toast.LENGTH_SHORT).show();
                break;

        }
        return true;

    }
    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));

                   // t1.speak("Jack's birthday is on April six", TextToSpeech.QUEUE_FLUSH, null);
                }
                break;
            }

        }
    }


}


class JSONAsyncTask extends AsyncTask<String, Void, String> {


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected String doInBackground(String... urls) {
        String data="";
        try {

            //------------------>>
            HttpGet httppost = new HttpGet(urls[0]);
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(httppost);

            // StatusLine stat = response.getStatusLine();
            int status = response.getStatusLine().getStatusCode();

            if (status == 200) {
                HttpEntity entity = response.getEntity();
                 data = EntityUtils.toString(entity);


               //JSONObject jsono = new JSONObject(data);

                return data;
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
       // return false;
        return data;
    }

    protected void onPostExecute(Boolean result) {

    }


}