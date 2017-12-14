package com.example.subhr.twitter;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseObject;

public class StarterApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //vgPKg5sNqoN4
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("74d6260b594ae19da623e4ccf8a8078d2e49bd71")
                .clientKey("a4f5672527cd81eda3aed66d1423a32cabff9835")
                .server("http://18.217.84.67:80/parse/").build());

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
    }
}
