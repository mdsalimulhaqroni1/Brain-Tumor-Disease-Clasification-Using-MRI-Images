package com.example.brainandroid1;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.brainandroid1.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

    private ImageView imgView;
    private Button select, predict;
    private TextView tv;
    private Bitmap img;
    String k;
    float per;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgView = (ImageView) findViewById(R.id.img);
        tv = (TextView) findViewById(R.id.txt);
        select = (Button) findViewById(R.id.btn1);
        predict = (Button) findViewById(R.id.btn2);

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 100);

            }
        });

        predict.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (img!=null) {
                    img = Bitmap.createScaledBitmap(img, 150, 150, true);

                    try {
                        Model model = Model.newInstance(getApplicationContext());

                        // Creates inputs for reference.
                        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 150, 150, 3}, DataType.FLOAT32);

                        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                        tensorImage.load(img);
                        ByteBuffer byteBuffer = tensorImage.getBuffer();
                        inputFeature0.loadBuffer(byteBuffer);

                        // Runs model inference and gets result.
                        Model.Outputs outputs = model.process(inputFeature0);
                        TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                        // Releases model resources if no longer used.
                        model.close();
                        float n1 = outputFeature0.getFloatArray()[0];
                        float n2 = outputFeature0.getFloatArray()[1];
                        float n3 = outputFeature0.getFloatArray()[2];
                        float n4 = outputFeature0.getFloatArray()[3];


                        if ((n1 > n2) && (n1 > n3) && (n1 > n4)) {
                            k = "Glioma";
                            per = (float) (n1 * 100);
                        } else if ((n2 > n1) && (n2 > n3) && (n2 > n4)) {
                            k = "No Tumor Found";
                        } else if ((n3 > n2) && (n3 > n1) && (n3 > n4)) {
                            k = "Meningioma";
                            per = (float) (n3 * 100);
                        } else if ((n4 > n2) && (n4 > n3) && (n4 > n1)) {
                            k = "Pituitary";
                            per = (float) (n4 * 100);
                        }
                        if(k=="No Tumor Found"){
                            tv.setText(k);
                        }
                        else{
                            tv.setText("Tumor type: " + k + "\n" + "Accuracy: " + per + "%");
                        }
                    } catch (IOException e) {
                        // TODO Handle the exception
                    }
                }
                else{
                    Toast.makeText(MainActivity.this, "Please select a image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100)
        {
            imgView.setImageURI(data.getData());

            Uri uri = data.getData();
            try {
                img = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}