package com.example.alexander.android3lesson3_homework;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    private TextView pathTextView;
    private Button selectPhotoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPermission();
    }

    private void getPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        } else {
            setUI();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                  setUI();
                } else {
                    Toast.makeText(this, "PERMISSION_DENIED", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void setUI() {
        pathTextView = findViewById(R.id.photo_path);
        selectPhotoButton = findViewById(R.id.select_photo_button);
        selectPhotoButton.setOnClickListener(v -> pickPhoto());
        findViewById(R.id.convert_to_png_button).setOnClickListener (v ->
                convertToPng(pathTextView.getText().toString()));
    }

    @SuppressLint("CheckResult")
    private void convertToPng(String s) {
        if (!TextUtils.isEmpty(s)){
            getObservableFromFile(s)
                    .observeOn(Schedulers.computation())
                    .subscribeOn(Schedulers.io())
                    .subscribe(result -> {
                        Toast.makeText(this, "s"+result.toString(), Toast.LENGTH_SHORT).show();
                      });
        } else {
            Toast.makeText(this,
                    "ВЫБЕРИТЕ ФАЙЛ", Toast.LENGTH_SHORT).show();
        }
    }
//see https://stackoverflow.com/questions/44434583/rxjava-convert-byte-array-to-bitmap
    public Observable<Boolean> getObservableFromFile(String path) {
        return Observable.fromCallable(() -> {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/DCIM/DSC01387.JPG");
                File convertedImage = new File(Environment.getExternalStorageDirectory()+"/convertedimg.png");
                FileOutputStream outStream=new FileOutputStream(convertedImage);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.flush();
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }).subscribeOn(Schedulers.io());
    }

    // see
    private void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 0);
    }

    private void setTextViews(String realPath) {
        pathTextView.setText("URI Path: " + realPath);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        if (resCode == Activity.RESULT_OK && data != null) {
            String realPath;
            realPath = RealPathUtil.getRealPathFromURI(this, data.getData());
            setTextViews(realPath);
        }
    }
}