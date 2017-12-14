package com.parse.starter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.ByteArrayInputStream;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        final String username = getIntent().getStringExtra("username");
        setTitle(username + "'s Feed");
        final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.verticalLayout);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Image");
        query.whereEqualTo("username", username);
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                Log.i("findImage", "Inside done");
                if (e == null && !objects.isEmpty()) {
                    Log.i("findImage", "Inside if condition");
                    for (ParseObject object : objects) {
                        final ParseFile imageFile = (ParseFile) object.get("image");
                        Log.i("findImage", "Got the image file");
                        imageFile.getDataInBackground(new GetDataCallback() {
                            @Override
                            public void done(byte[] data, ParseException e) {
                                if (e == null && data != null) {
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(data,0, data.length);
                                    ImageView imageView = new ImageView(UserActivity.this);
                                    imageView.setLayoutParams(new ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT
                                    ));
                                    imageView.setImageBitmap(bitmap);
                                    Log.i("findImage", "Created new image view");
                                    linearLayout.addView(imageView);
                                    Log.i("findImage", "Updated linear layout");
                                }
                            }
                        });
                    }
                } else if (objects.isEmpty()) {
                    Toast.makeText(getApplicationContext(), username + " has not posted any photos yet", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Could not retrieve photos of " + username, Toast.LENGTH_LONG).show();
                    Log.e("findImageError", e.getStackTrace().toString());
                }
            }
        });
    }
}
