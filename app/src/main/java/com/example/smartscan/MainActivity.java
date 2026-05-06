package com.example.smartscan;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.text.method.ScrollingMovementMethod;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 3000;

    Uri imageFileUri;
    ImageView imageView;
    TextView textViewSubText;
    String readerType;


    ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode() == RESULT_OK) {

                            Intent data = result.getData();

                            // GALLERY
                            if (data != null && data.getData() != null) {
                                imageFileUri = data.getData();
                            }

                            // CAMERA uses existing imageFileUri

                            if (imageFileUri != null) {

                                imageView.setImageURI(imageFileUri);
                                textViewSubText.setText("");

                                try {
                                    InputImage image =
                                            InputImage.fromFilePath(this, imageFileUri);

                                    if (readerType != null) {

                                        if (readerType.equals("TextReader")) {
                                            processText(image);
                                        } else if (readerType.equals("BarcodeReader")) {
                                            processBarcode(image);
                                        } else if (readerType.equals("ContentReader")) {
                                            processContent(image);
                                        }
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.ImageViewMian);
        textViewSubText = findViewById(R.id.TextViewSubText);
        textViewSubText.setMovementMethod(new ScrollingMovementMethod());
        // GET READER TYPE
        readerType = getIntent().getStringExtra("Reader_type");

        // SET TOP IMAGE
        if ("BarcodeReader".equals(readerType)) {
            imageView.setImageResource(R.drawable.barcode);
        }
        else if ("ContentReader".equals(readerType)) {
            imageView.setImageResource(R.drawable.content);
        }
        else if ("TextReader".equals(readerType)) {
            imageView.setImageResource(R.drawable.text);
        }
        else {
            imageView.setImageResource(R.drawable.text);
        }
    }

    public void OpenCamera(View view) {

        if (!checkPermission()) return;

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        imageFileUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new ContentValues()
        );

        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFileUri);

        activityResultLauncher.launch(intent);
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.CAMERA},
                    REQUEST_PERMISSION
            );
            return false;
        }
        return true;
    }

    public void Return(View view) {
        startActivity(new Intent(this, Start.class));
    }

    public void openGallery(View view) {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        activityResultLauncher.launch(intent);
    }

    public void EditText(View view) {
        Intent intent = new Intent(this, EditActivity.class);

        // TEXT
        intent.putExtra("RESULT_TEXT",
                textViewSubText.getText().toString());

        // IMAGE
        if (imageFileUri != null) {
            intent.putExtra("IMAGE_URI", imageFileUri.toString());
        }

        editLauncher.launch(intent);
    }
    ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {

                            if (result.getData() != null) {
                                String editedText =
                                        result.getData().getStringExtra("EDITED_TEXT");

                                textViewSubText.setText(editedText);
                            }
                        }
                    });
    private void processText(InputImage image) {
        TextRecognizer recognizer =
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
                .addOnSuccessListener(result -> {
                    String text = result.getText();

                    if (text.isEmpty()) {
                        textViewSubText.setText("No text found");
                    } else {
                        textViewSubText.setText(text);
                    }
                })
                .addOnFailureListener(e -> {
                    textViewSubText.setText("Text reading failed");
                });
    }

    private void processBarcode(InputImage image) {
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build();
        BarcodeScanner scanner = BarcodeScanning.getClient(options);
        Task<java.util.List<Barcode>> result = scanner.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodes) {
                        textViewSubText.append(Html.fromHtml("<font color='navy'><b>Detected barcode:</b></font><br>", Html.FROM_HTML_MODE_LEGACY));
                        String result = "";
                        for (Barcode barcode : barcodes) {
                            result = barcode.getRawValue();
                            textViewSubText.append(result + "\n");
                        }
                        if (result.length() < 2) {
                            textViewSubText.append(" Barcode not found.\n");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        textViewSubText.setText("Failed");
                    }
                });
    }

    private void processContent(InputImage image) {

        ImageLabeler labeler = ImageLabeling.getClient(
                ImageLabelerOptions.DEFAULT_OPTIONS
        );

        labeler.process(image)
                .addOnSuccessListener(labels -> {

                    if (labels.isEmpty()) {
                        textViewSubText.setText("No objects detected");
                        return;
                    }

                    StringBuilder result = new StringBuilder();

                    for (ImageLabel label : labels) {
                        result.append(label.getText()).append("\n");
                    }

                    textViewSubText.setText(result.toString());
                })
                .addOnFailureListener(e -> {
                    textViewSubText.setText("Image labeling failed");
                });
    }
}
