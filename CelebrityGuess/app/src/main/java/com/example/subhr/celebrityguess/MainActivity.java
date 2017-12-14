package com.example.subhr.celebrityguess;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> dogUrls = new ArrayList<>();
    ArrayList<String> dogNames = new ArrayList<>();
    int chosenDog = 0;
    ImageView imageView;
    int locationOfCorrectAns = 0;
    String[] answers = new String[4];
    Button btn1;
    Button btn2;
    Button btn3;
    Button btn4;

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                InputStream in = httpURLConnection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while(data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void chosenDog(View view) {

        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAns))) {
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Wrong! It was " + dogNames.get(chosenDog), Toast.LENGTH_LONG).show();
        }
        createNewQuestion();
    }

    public void createNewQuestion() {
        Random rand = new Random();
        chosenDog = rand.nextInt(dogUrls.size());

        ImageDownloader imageTask = new ImageDownloader();
        Bitmap dogImage;
        try {
            dogImage = imageTask.execute(dogUrls.get(chosenDog)).get();
            imageView.setImageBitmap(dogImage);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        locationOfCorrectAns = rand.nextInt(4);

        int incorrectAnsLocation;

        for (int i = 0; i < 4; i++) {
            if (i == locationOfCorrectAns) {
                answers[i] = dogNames.get(chosenDog);
            } else {
                incorrectAnsLocation = rand.nextInt(dogUrls.size());
                while (incorrectAnsLocation == chosenDog) {
                    incorrectAnsLocation = rand.nextInt(dogUrls.size());
                }
                answers[i] = dogNames.get(incorrectAnsLocation);
            }
        }

        btn1.setText(answers[0]);
        btn2.setText(answers[1]);
        btn3.setText(answers[2]);
        btn4.setText(answers[3]);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.dogImageView);
        btn1 = (Button) findViewById(R.id.button1);
        btn2 = (Button) findViewById(R.id.button2);
        btn3 = (Button) findViewById(R.id.button3);
        btn4 = (Button) findViewById(R.id.button4);

        DownloadTask task = new DownloadTask();
        String result=null;
        try {
            result = task.execute("http://www.dogbreedslist.info/Tags-G/#.Whd2FUqWZPZ").get();
            Pattern p = Pattern.compile("img src=\"(.*?)\"");
            Matcher m =  p.matcher(result);
            while (m.find()) {
                dogUrls.add(m.group(1));
            }

            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(result);

            while (m.find()) {
                dogNames.add(m.group(1));
            }

            createNewQuestion();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
