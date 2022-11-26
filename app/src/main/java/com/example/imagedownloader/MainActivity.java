package com.example.imagedownloader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    EditText txtUrl;
    Button btnDownload;
    ImageView imgView;

    //Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtUrl = findViewById(R.id.txtURL);
        btnDownload = findViewById(R.id.btnDownload);
        imgView = (ImageView) findViewById(R.id.imgView);

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
                } else {
                    AsyncTask backgroundTask = new DownloadTask();
                    String url[] = new String[]{txtUrl.getText().toString()};
                    backgroundTask.execute(url);
                    //                preview(imagePath);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AsyncTask backgroundTask = new DownloadTask();
                String url[] = new String[]{txtUrl.getText().toString()};
                backgroundTask.execute(url);
            }
        }
    }

    private void preview(Bitmap image) {
//        Bitmap image = BitmapFactory.decodeFile(imagePath);
        imgView.setImageBitmap(image);
    }



    class DownloadTask extends AsyncTask<String, Integer, Bitmap> {

        ProgressDialog pd;

        private void downloadFile(String urlStr, String imagePath) {
            try {
                URL url = new URL(urlStr);
                URLConnection connection = url.openConnection();
                connection.connect();

                int fileSize  = connection.getContentLength();
                InputStream reader = new BufferedInputStream(url.openStream(), 8192);
                OutputStream writer = new FileOutputStream(imagePath);

                byte data[] = new byte[1024];
                int count;
                int total = 0;
                while ((count = reader.read(data)) != -1) {
                    writer.write(data, 0, count);

                    total += count;
                    publishProgress((int)((total*100)/fileSize));
                }

                writer.flush();
                writer.close();
                reader.close();

            } catch (Exception ex) {
                Log.d("Download File", ex.getMessage());
                ex.printStackTrace();
            }
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            Log.d("Download task.", strings[0]);
            String imagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/temp.jpg";

            downloadFile(strings[0], imagePath);

            Log.d("Download task.", "Download completed.");

            Bitmap image = BitmapFactory.decodeFile(imagePath);

            Log.d("Download task.", "Scale image.");
            float w = image.getWidth();
            float h = image.getHeight();
            int W = 400;
            int H = (int) ( (h * W) / w);
            Bitmap scaled = Bitmap.createScaledBitmap(image, W, H, false);

            return scaled;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            pd.dismiss();
            Log.d("On Post Execute", "Set image");
            super.onPostExecute(bitmap);
            preview(bitmap);
            Log.d("On Post Execute", "After imageView");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMax(100);
            pd.setIndeterminate(false);
            pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pd.setTitle("Downloading");
            pd.setMessage("Please wait..");
            pd.show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            pd.setProgress(values[0]);
        }
    }
}