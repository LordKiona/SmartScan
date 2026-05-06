package com.example.smartscan;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EditActivity extends AppCompatActivity {
    EditText editText;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit);

        ImageView imageView = findViewById(R.id.imageViewEdit);
        EditText editText = findViewById(R.id.editTextResult);

        // GET TEXT
        String text = getIntent().getStringExtra("RESULT_TEXT");

        // GET IMAGE
        String uriString = getIntent().getStringExtra("IMAGE_URI");

        if (uriString != null) {
            Uri uri = Uri.parse(uriString);
            imageView.setImageURI(uri);
        }

        if (text != null) {
            editText.setText(text);
        }
    }
    public void saveResult(View view) {

        String editedText = editText.getText().toString();

        Intent intent = new Intent();
        intent.putExtra("EDITED_TEXT", editedText);

        setResult(RESULT_OK, intent);
        finish();
    }
}