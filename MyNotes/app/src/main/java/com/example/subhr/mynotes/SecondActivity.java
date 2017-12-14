package com.example.subhr.mynotes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SecondActivity extends AppCompatActivity {

    List<String> savedNotes = new ArrayList<>();
    Button saveButton;
    Button editButton;
    Button deleteButton;
    EditText noteText;
    SharedPreferences sharedPreferences;
    Intent intent;

    public void editNote(View view) {
        String newNote = noteText.getText().toString();
        savedNotes.set(intent.getIntExtra("noteNumber", 0), newNote);
        try {
            sharedPreferences.edit().putString("notes", ObjectSerializer.serialize((Serializable) savedNotes)).apply();
            Toast.makeText(getApplicationContext(), "Note saved", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveNote(View view) {
        String currentNote = noteText.getText().toString();
        savedNotes.add(currentNote);
        try {
            sharedPreferences.edit().putString("notes", ObjectSerializer.serialize((Serializable) savedNotes)).apply();
            Toast.makeText(getApplicationContext(), "Note saved", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        saveButton = (Button) findViewById(R.id.saveButtoon);
        editButton = (Button) findViewById(R.id.editButton);
        saveButton.setVisibility(View.INVISIBLE);
        editButton.setVisibility(View.INVISIBLE);
        noteText = (EditText) findViewById(R.id.noteText);
        sharedPreferences = this.getSharedPreferences("com.example.subhr.mynotes", Context.MODE_PRIVATE);
        try {
            savedNotes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("notes", ObjectSerializer.serialize(new ArrayList<>())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        intent = getIntent();
        if (savedNotes.isEmpty() || intent.getIntExtra("noteNumber", 0) == -1) {
            noteText.setText("Write note...");
            saveButton.setVisibility(View.VISIBLE);
        } else {
            String savedNote = savedNotes.get(intent.getIntExtra("noteNumber", 0));
            noteText.setText(savedNote);
            editButton.setVisibility(View.VISIBLE);
        }
    }
}
