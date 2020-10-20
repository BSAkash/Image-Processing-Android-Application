package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import android.content.ClipData;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.FileOutputStream;
import static android.graphics.Color.BLACK;
import static android.graphics.Color.blue;
import static android.graphics.Color.colorSpace;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

public class MainActivity extends AppCompatActivity {

    double resultnew;
    final List<Bitmap> bitmaps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button pickImage = findViewById(R.id.pickImage);

        pickImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View view){
                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                    return;
                }

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                startActivityForResult(intent, 1);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode, @Nullable Intent data){

        if(requestCode == 1 && resultCode == RESULT_OK){
            final ImageView imageView = findViewById(R.id.imageview);
            final TextView textView = (TextView) findViewById(R.id.TextView);
//            final List<Bitmap> bitmaps = new ArrayList<>();
            ClipData clipData = data.getClipData();

            if(clipData != null){
                for(int i= 0;i< clipData.getItemCount();i++){
                    Uri imageUri = clipData.getItemAt(i).getUri();
                    try{
                        InputStream is = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);

//                        Toast.makeText(this, "Image Average intensity: " + result, Toast.LENGTH_LONG).show();
                        bitmaps.add(bitmap);
                    } catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
            } else {

                Uri imageUri = data.getData();
                try {
                    InputStream is = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);

//                    Toast.makeText(this, "Image Average intensity: " + result, Toast.LENGTH_LONG).show();
                    bitmaps.add(bitmap);
                }catch(FileNotFoundException e){
                    e.printStackTrace();
                }
            }
            new Thread(new Runnable(){
                @Override
                public void run() {
                    for(final Bitmap b : bitmaps) {
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                imageView.setImageBitmap(b);
                                int pixels[] = new int[b.getWidth()*b.getHeight()];
                                b.getPixels(pixels, 0 , b.getWidth(), 0, 0,b.getWidth(), b.getHeight());
                                Log.d("IMAGE: ", red(pixels[0]) +"");
                                long r = 0, g = 0, bl = 0;
                                for(int j: pixels){
                                    r += red(j);
                                    g += green(j);
                                    bl += blue(j); }
                                double result = (double)((0.3*r)+(0.59*g)+(0.11*bl))/(double)(b.getWidth()*b.getHeight());
                                int image_intensity = Log.d("IMAGE INTENSITY", "" + result);
                                // TextView textView = (TextView) findViewById(R.id.TextView);
                                resultnew = (double) (result) / (255);
                                textView.setText("The Intensity is "+resultnew);
                                System.out.println("VARIABLES"+result+"VARIABLES"); }});
                        try {
                            Thread.sleep(3000);
                        }catch(InterruptedException e){
                            e.printStackTrace(); } }
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(MainActivity.this,"The end",Toast.LENGTH_LONG).show();}}); }}).start();


        }


    }


    public void export(View view){
        //generate data
        StringBuilder datanew = new StringBuilder();
        datanew.append("Image_Id,Intensity");
        for(final Bitmap b : bitmaps) {
            datanew.append("\n"+String.valueOf(b)+","+String.valueOf(resultnew));
        }

        try{
            //saving the file into device
            FileOutputStream out = openFileOutput("datanew.csv", Context.MODE_PRIVATE);
            out.write((datanew.toString()).getBytes());
            out.close();

            //exporting
            Context context = getApplicationContext();
            File filelocation = new File(getFilesDir(), "datanew.csv");
            Uri path = FileProvider.getUriForFile(context, "com.example.exportcsv.fileprovider", filelocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Data");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(Intent.createChooser(fileIntent, "Send mail"));
        }
        catch(Exception e){
            e.printStackTrace();
        }


    }

}
