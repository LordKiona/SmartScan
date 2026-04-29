package com.example.smartscan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Start extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_start);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void OpenMainActivity_1(View view) {
        Intent intent = new Intent(Start.this, MainActivity.class);
        intent.putExtra("Reader_type","BarcodeReader");
        startActivity(intent);
    }

    public void OpenMainActivity_2(View view) {
        Intent intent = new Intent(Start.this, MainActivity.class);
        intent.putExtra("Reader_type","ContentReader");
        startActivity(intent);
    }

    public void OpenMainActivity_3(View view) {
        Intent intent = new Intent(Start.this, MainActivity.class);
        intent.putExtra("Reader_type","TextReader");
        startActivity(intent);
    }

}