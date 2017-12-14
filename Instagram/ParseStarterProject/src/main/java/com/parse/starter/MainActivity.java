/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {

    EditText usernameEditText;
    EditText passwordEditText;
    Button signUpButton;
    TextView changeModeTextView;

    Boolean signUpModeActive = true;

    public void showUserList() {
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
    }

    public void signUp(View view) {
        usernameEditText = (EditText) findViewById(R.id.usernameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);

        if (usernameEditText.getText().toString().isEmpty() || passwordEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Username and password are required", Toast.LENGTH_LONG).show();
        } else {

            if (signUpModeActive) {
                ParseUser user = new ParseUser();
                user.setUsername(usernameEditText.getText().toString());
                user.setPassword(passwordEditText.getText().toString());

                user.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e ==  null) {
                            Log.i("SignUp", "Successful");
                            showUserList();
                        } else {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                ParseUser.logInInBackground(usernameEditText.getText().toString(), passwordEditText.getText().toString(), new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if (e == null && user != null) {
                            Log.i("Login", "Successful");
                            showUserList();
                        } else {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        signUpButton = (Button) findViewById(R.id.signupButton);
        changeModeTextView = (TextView) findViewById(R.id.changeModeTextView);
        changeModeTextView.setOnClickListener(this);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        RelativeLayout backgroundRelativeLayout = (RelativeLayout) findViewById(R.id.backgroundRelativeLayout);
        ImageView logoImageView = (ImageView) findViewById(R.id.logoImageView);
        backgroundRelativeLayout.setOnClickListener(this);
        logoImageView.setOnClickListener(this);
        passwordEditText.setOnKeyListener(this);
        /*ParseObject score = new ParseObject("Score");
        score.put("username", "rob");
        score.put("score", 86);
        score.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.i("SaveInBackground", "Successful");
                } else {
                    Log.i("SaveInBackground", "Failed. Error: " + e.toString());
                }
            }
        });*/

      /*  ParseQuery<ParseObject> query = ParseQuery.getQuery("Score");
        query.getInBackground("9dMJALqekA", new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null && object != null) {
                    object.put("score", 200);
                    object.saveInBackground();
                    Log.i("ObjectValue", object.getString("username"));
                    Log.i("ObjectValue", Integer.toString(object.getInt("score")));
                }
            }
        });*/

      /*ParseQuery<ParseObject> query = ParseQuery.getQuery("Score");
      query.whereEqualTo("username", "Subhra");
      query.setLimit(1);
      query.findInBackground(new FindCallback<ParseObject>() {
          @Override
          public void done(List<ParseObject> objects, ParseException e) {
              if (e == null) {
                  Log.i("findInBackground", "Retrieved " + objects.size() + " objects");
                  if (objects.size() > 0) {
                      for (ParseObject object : objects) {
                          Log.i("findInBackgroundResult", Integer.toString(object.getInt("score")));
                      }
                  }
              }
          }
      });*/

      /*ParseUser user = new ParseUser();
      user.setUsername("subhra");
      user.setPassword("password");

      user.signUpInBackground(new SignUpCallback() {
          @Override
          public void done(ParseException e) {
              if (e == null) {
                  Log.i("Sign up", "Successful");
              } else {
                  Log.i("Sign up", "Failed");
              }
          }
      });*/

      /*ParseUser.logInInBackground("subhra", "password", new LogInCallback() {
          @Override
          public void done(ParseUser user, ParseException e) {
              if (user != null) {
                  Log.i("Login", "Successful");
              } else {
                  Log.i("Login", "Failed. Error " + e.toString());
              }
          }
      });*/

      //ParseUser.logOut();

     /* if (ParseUser.getCurrentUser() != null) {
          Log.i("currentUser", "User logged in " + ParseUser.getCurrentUser().getUsername());
      } else {
          Log.i("currentUser", "User not logged in");
      }*/

     if (ParseUser.getCurrentUser() != null) {
         showUserList();
     }

      ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.changeModeTextView) {
            if (signUpModeActive) {
                signUpModeActive = false;
                signUpButton.setText("Login");
                changeModeTextView.setText("or, Sign up");
            } else {
                signUpModeActive = true;
                signUpButton.setText("Sign up");
                changeModeTextView.setText("or, Login");
            }
        } else if (v.getId() == R.id.backgroundRelativeLayout || v.getId() == R.id.logoImageView) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            signUp(v);
        }
        return false;
    }
}