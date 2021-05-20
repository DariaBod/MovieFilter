package com.example.moviefilter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.os.FileUriExposedException;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicConvolve3x3;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Editor extends FragmentActivity {
    static final int TAKE_PICTURE_REQUEST = 1;
    static final int GALLERY_CODE = 0;
    String mCurrentPhotoPath;
    Bitmap statebit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ViewPager vp=findViewById(R.id.pager);
        MyAdapt adapt = new MyAdapt(getSupportFragmentManager());
        vp.setAdapter(adapt);
        Intent intent = getIntent();
        int photo = intent.getIntExtra("photo", 0);
        if(photo==1) {
            openCamera(this);
        }
        if(photo==2) {
            openGallery(this);
        }
    }
    public void openGallery(Editor view){
        Intent GalleryIntent = null;
        GalleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        GalleryIntent.setType("image/*");
        GalleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(GalleryIntent,GALLERY_CODE);
    }
    public void openCamera(Editor view){
        getThumbnailPicture();
    }

    private void askPermission() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int readPermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            int writePermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int cameraPermission = ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA);

            if (writePermission != PackageManager.PERMISSION_GRANTED ||
                    readPermission != PackageManager.PERMISSION_GRANTED || cameraPermission!=PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user.
                this.requestPermissions(
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        101
                );
                return;
            }}
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView iv = findViewById(R.id.photo);
        if (requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK) {
            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            File file = new File(imageUri.getPath());
            try {
                InputStream ims = new FileInputStream(file);
                Bitmap bm=BitmapFactory.decodeStream(ims);
                iv.setImageBitmap(bm);
                statebit=bm;
                MediaStore.Images.Media.insertImage(getContentResolver(), bm, "MFPhoto" , "");
            } catch (FileNotFoundException e) {
                Log.d("MyTag", "Photo dont presented");
                return;
            }
        } else if(requestCode == GALLERY_CODE){
            Uri ImageURI = data.getData();
            iv.setImageURI(ImageURI);
            statebit = ((BitmapDrawable)iv.getDrawable()).getBitmap();
        }
    }

    private void getThumbnailPicture() {
        askPermission();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            photoFile = createImageFile();
            if (photoFile != null) {
                Log.d("MyTag", "Created photo file");
               try{ Uri photoURI = FileProvider.getUriForFile(
                       Editor.this,
                       "com.example.moviefilter.provider",
                       photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, TAKE_PICTURE_REQUEST);}
               catch(FileUriExposedException e){
                  Log.d("MyTag", "You loose uri");
               }
            }
    }
}

    private File createImageFile(){
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(getFilesDir().getAbsolutePath());
        if(storageDir.canWrite()){
            Log.d("MyTag", String.valueOf(storageDir));
        }
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            Log.d("MyTag", "Dont created temporary file");
            e.printStackTrace();
        }
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
    public void rightRotate(View view){
        ImageView iv = findViewById(R.id.photo);
        Bitmap bitmap = ((BitmapDrawable)iv.getDrawable()).getBitmap();
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        iv.setImageBitmap(rotatedBitmap);
    }
    public void leftRotate(View view){
        ImageView iv = findViewById(R.id.photo);
        Bitmap bitmap = ((BitmapDrawable)iv.getDrawable()).getBitmap();
        Matrix matrix = new Matrix();
        matrix.postRotate(270);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);
        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        iv.setImageBitmap(rotatedBitmap);
    }
    public void filter1(View view) {
        ImageView iv = findViewById(R.id.photo);
        ImageView f = findViewById(R.id.filter1);
        int tag = (int) f.getTag();
        switch (tag){
        case (R.drawable.filt1): {
            iv.setImageBitmap(statebit);
            Bitmap bitmap = ((BitmapDrawable)iv.getDrawable()).getBitmap();
            int []colors={0x61EE20D3,0x61EE20D3,0x61EE20D3,0x61EE20D3,0x68FAE30C,0x68FAE30C, 0x68FAE30C};
            GradientDrawable gd=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
            //pic.setColorFilter(new PorterDuffColorFilter(R.color.firstfil, PorterDuff.Mode.SRC_ATOP));
            doSharpen(bitmap, new float[]{ -0.15f, -0.15f, -0.15f, -0.15f, 2.2f, -0.15f, -0.15f, -0.15f, -0.15f});
            Drawable pic=changeBitmapContrastBrightness(bitmap, (float) 1.5,-40);
            //Drawable pic= new BitmapDrawable(getResources(), bitmap);
            Drawable[] mas={pic,gd};
            LayerDrawable ld=new LayerDrawable(mas);
            //слегка понижаю яркость
            //ld.setColorFilter(new PorterDuffColorFilter(Color.argb(30, 0, 0, 0), PorterDuff.Mode.SRC_ATOP));
            iv.setImageDrawable(ld);
            break;
        }
        }
    }

    public void filter2(View view) {
        ImageView iv = findViewById(R.id.photo);
        ImageView f = findViewById(R.id.filter2);
        int tag = (int) f.getTag();
        switch (tag){
            case (R.drawable.filt2): {
                iv.setImageBitmap(statebit);
                iv.getDrawable().setColorFilter(new PorterDuffColorFilter(0x48EE9695, PorterDuff.Mode.SRC_ATOP));
                break;
            }
        }
    }
    public void filter3(View view) {
        ImageView iv = findViewById(R.id.photo);
        ImageView f = findViewById(R.id.filter3);
        int tag = (int) f.getTag();
        switch (tag){
            case (R.drawable.filt3): {
                iv.setImageBitmap(statebit);
                Bitmap bitmap = ((BitmapDrawable)iv.getDrawable()).getBitmap();
                doSharpen(bitmap, new float[]{ -0.15f, -0.15f, -0.15f, -0.15f, 2.2f, -0.15f, -0.15f, -0.15f, -0.15f});
                Drawable pic=changeBitmapContrastBrightness(bitmap, (float) 1.5,-40);
                int []colors={0x701774E2, 0x80909194};
                GradientDrawable gd=new GradientDrawable(GradientDrawable.Orientation.BL_TR, colors);
                Drawable[] mas={pic,gd};
                LayerDrawable ld=new LayerDrawable(mas);
                //pic.setColorFilter(0x59BED4F0, PorterDuff.Mode.SRC_ATOP);
                iv.setImageDrawable(ld);
                break;
            }
        }
    }
    public Bitmap doSharpen(Bitmap original, float[] radius) {
        Bitmap bitmap = Bitmap.createBitmap(
                original.getWidth(), original.getHeight(),
                Bitmap.Config.ARGB_8888);

        RenderScript rs = RenderScript.create(Editor.this);

        Allocation allocIn = Allocation.createFromBitmap(rs, original);
        Allocation allocOut = Allocation.createFromBitmap(rs, bitmap);

        ScriptIntrinsicConvolve3x3 convolution
                = ScriptIntrinsicConvolve3x3.create(rs, Element.U8_4(rs));
        convolution.setInput(allocIn);
        convolution.setCoefficients(radius);
        convolution.forEach(allocOut);
        allocOut.copyTo(bitmap);
        rs.destroy();
        return bitmap;

    }
    public Drawable changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness)
    {
        ColorMatrix cm = new ColorMatrix(
                new float[]{
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                }
        );
        Drawable pic= new BitmapDrawable(getResources(), bmp);
        pic.setColorFilter(new ColorMatrixColorFilter(cm));
        return pic;
    }
    public void save(View view) {
        ImageView iv = findViewById(R.id.photo);
        Bitmap bitmap = ((BitmapDrawable)iv.getDrawable()).getBitmap();
        MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "MFPhoto" , "");
        statebit=bitmap;
    }
    public void rollback(View view) {
        ImageView iv = findViewById(R.id.photo);
        iv.setImageBitmap(statebit);
    }
    }
