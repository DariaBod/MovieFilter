package com.example.moviefilter.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUriExposedException;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicConvolve3x3;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.moviefilter.MyAdapt;
import com.example.moviefilter.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.N)
public class Editor extends FragmentActivity {
    static final int TAKE_PICTURE_REQUEST = 1;
    static final int GALLERY_CODE = 0;
    String mCurrentPhotoPath;
    Bitmap statebit;
    Uri picUri;
    int page = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ViewPager vp = findViewById(R.id.pager);
        MyAdapt adapt = new MyAdapt(getSupportFragmentManager());
        vp.setAdapter(adapt);
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                       @Override
                                       public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                                       }

                                       @Override
                                       public void onPageSelected(int position) {
                                           page = position;
                                       }

                                       @Override
                                       public void onPageScrollStateChanged(int state) {

                                       }
                                   }
        );
        Intent intent = getIntent();
        int photo = intent.getIntExtra("photo", 0);
        if (photo == 1) {
            openCamera(this);
        }
        if (photo == 2) {
            openGallery(this);
        }
        ImageView iv = findViewById(R.id.photo);
        Bundle bd = getIntent().getExtras();
        if (bd.containsKey("uri")) {
            picUri = bd.getParcelable("uri");
            iv.setImageURI(picUri);
            Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
            statebit = bitmap;
        }
    }

    public void openGallery(Editor view) {
        askPermission();
        Intent GalleryIntent = null;
        GalleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        GalleryIntent.setType("image/*");
        GalleryIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(GalleryIntent, GALLERY_CODE);
    }

    public void openCamera(Editor view) {
        getThumbnailPicture();
    }

    private void askPermission() {
        int readPermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int cameraPermission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);

        if (writePermission != PackageManager.PERMISSION_GRANTED ||
                readPermission != PackageManager.PERMISSION_GRANTED || cameraPermission != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    101
            );
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView iv = findViewById(R.id.photo);
        if (requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK) {
            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            picUri = imageUri;
            File file = new File(imageUri.getPath());
            try {
                InputStream ims = new FileInputStream(file);
                Bitmap bm = BitmapFactory.decodeStream(ims);
                Bitmap rotated = rotateBitmap(bm, imageUri.getPath());
                iv.setImageBitmap(rotated);
                statebit = rotated;
                MediaStore.Images.Media.insertImage(getContentResolver(), rotated, "MFPhoto", "");
            } catch (FileNotFoundException e) {
                Log.d("MyTag", "Фото нет");
                return;
            }
        } else if (requestCode == GALLERY_CODE) {
            Uri ImageURI = data.getData();
            picUri = ImageURI;
            iv.setImageURI(ImageURI);
            statebit = ((BitmapDrawable) iv.getDrawable()).getBitmap();
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                iv.setImageURI(resultUri);
                statebit = ((BitmapDrawable) iv.getDrawable()).getBitmap();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
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
                try {
                    Uri photoURI = FileProvider.getUriForFile(
                            Editor.this,
                            "com.example.moviefilter.provider",
                            photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    startActivityForResult(intent, TAKE_PICTURE_REQUEST);
                } catch (FileUriExposedException e) {
                }
            }
        }
    }

    public static Bitmap rotateBitmap(Bitmap srcBitmap, String path) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(0));
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                break;
        }
        Bitmap destBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                srcBitmap.getHeight(), matrix, true);
        return destBitmap;
    }

    private File createImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(getFilesDir().getAbsolutePath());
        if (storageDir.canWrite()) {
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
            Log.d("MyTag", "Временный файл не создан");
            e.printStackTrace();
        }
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    public void filter1(View view) throws InterruptedException {
        ImageView iv = findViewById(R.id.photo);
        Log.d("MyTag", String.valueOf(page));
        switch (page) {
            case (0): {
                iv.setImageBitmap(statebit);
                Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                int[] colors = {color(R.color.filter1p0col1), color(R.color.filter1p0col1), color(R.color.filter1p0col1),
                        color(R.color.filter1p0col1), color(R.color.filter1p0col2), color(R.color.filter1p0col2),
                        color(R.color.filter1p0col2)};
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                bitmap = doSharpen(bitmap, new float[]{-0.15f, -0.15f, -0.15f, -0.15f, 2.2f, -0.15f, -0.15f, -0.15f, -0.15f});
                Drawable pic = changeBitmapContrastBrightness(bitmap, (float) 1.5, -40);
                Drawable[] mas = {pic, gd};
                LayerDrawable ld = new LayerDrawable(mas);
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                ld.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                ld.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }
            case (1): {
                iv.setImageBitmap(statebit);
                Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                Drawable pic = changeBitmapContrastBrightness(bitmap, (float) 1.1, -50);
                int[] colors = {color(R.color.filter4p1col1), color(R.color.filter4p1col2), color(R.color.filter4p1col3),
                        color(R.color.filter4p1col4), color(R.color.filter4p1col5)};
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TR_BL, colors);
                Drawable[] mas = {pic, gd};
                LayerDrawable ld = new LayerDrawable(mas);
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                ld.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                ld.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }
            case (2): {
                iv.setImageBitmap(statebit);
                Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                faceDetect(bitmap);
                Bitmap bit = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                Drawable pic = changeBitmapContrastBrightness(bit, (float) 1.2, -50);
                int[] colors = {color(R.color.filter7p2col1), color(R.color.filter7p2col1)};
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                Drawable[] mas = {pic, gd};
                LayerDrawable ld = new LayerDrawable(mas);
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                ld.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                ld.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }
            case (3): {
                iv.setImageBitmap(statebit);
                iv.getDrawable().setColorFilter(new PorterDuffColorFilter(color(R.color.filter10p3col1), PorterDuff.Mode.SRC_ATOP));
                Drawable dr = iv.getDrawable();
                Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                dr.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                dr.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }
            case (4): {
                iv.setImageBitmap(statebit);
                Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                Drawable pic = changeBitmapContrastBrightness(bitmap, (float) 1.3, -20);
                int[] colors = {color(R.color.filter13p4col1), color(R.color.filter13p4col2), color(R.color.filter13p4col2),
                        color(R.color.filter13p4col3), color(R.color.filter13p4col4)};
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TR_BL, colors);
                Drawable[] mas = {pic, gd};
                LayerDrawable ld = new LayerDrawable(mas);
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                ld.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                ld.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }

            case (5): {
                iv.setImageBitmap(statebit);
                Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                Drawable pic = changeBitmapContrastBrightness(bitmap, (float) 1.3, -50);
                int[] colors = {color(R.color.filter16p5col1), color(R.color.filter16p5col2), color(R.color.filter16p5col3),
                        color(R.color.filter16p5col4), color(R.color.filter16p5col5)};
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                Drawable[] mas = {pic, gd};
                LayerDrawable ld = new LayerDrawable(mas);
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                ld.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                ld.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }
        }
    }

    public void filter2(View view) {
        ImageView iv = findViewById(R.id.photo);
        switch (page) {
            case (0): {
                iv.setImageBitmap(statebit);
                iv.getDrawable().setColorFilter(new PorterDuffColorFilter(color(R.color.filter2p0col1), PorterDuff.Mode.SRC_ATOP));
                Drawable dr = iv.getDrawable();
                Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                dr.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                dr.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }
            case (1): {
                iv.setImageBitmap(statebit);
                Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                Drawable pic = changeBitmapContrastBrightness(bitmap, (float) 1.0, -30);
                int[] colors = {color(R.color.filter5p1col1), color(R.color.filter5p1col2), color(R.color.filter5p1col3),
                        color(R.color.filter5p1col3), color(R.color.filter5p1col3), color(R.color.filter5p1col3),
                        color(R.color.filter5p1col2), color(R.color.filter5p1col1)};
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                Drawable[] mas = {pic, gd};
                LayerDrawable ld = new LayerDrawable(mas);
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                ld.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                ld.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }
            case (2): {
                iv.setImageBitmap(statebit);
                Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                Drawable pic = changeBitmapContrastBrightness(bitmap, (float) 1.0, -30);
                int[] colors = {color(R.color.filter8p2col1), color(R.color.filter8p2col2), color(R.color.filter8p2col3),
                        color(R.color.filter8p2col4), color(R.color.filter8p2col5), color(R.color.filter8p2col5),
                        color(R.color.filter8p2col5), color(R.color.filter8p2col5)};
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                Drawable[] mas = {pic, gd};
                LayerDrawable ld = new LayerDrawable(mas);
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                ld.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                ld.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }
            case (3): {
                iv.setImageBitmap(statebit);
                Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                Drawable pic = changeBitmapContrastBrightness(bitmap, (float) 1.08, 5);
                int[] colors = {color(R.color.filter11p3col1), color(R.color.filter11p3col1)};
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                Drawable[] mas = {pic, gd};
                LayerDrawable ld = new LayerDrawable(mas);
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                ld.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                ld.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }
            case (4): {
                iv.setImageBitmap(statebit);
                Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                Drawable pic = changeBitmapContrastBrightness(bitmap, (float) 1.35, -20);
                int[] colors = {color(R.color.filter14p4col1), color(R.color.filter14p4col1)};
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                Drawable coins = getDrawable(R.drawable.coin_lvl);
                Drawable[] mas = {pic, gd, coins};
                LayerDrawable ld = new LayerDrawable(mas);
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                ld.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                ld.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }

            case (5): {
                iv.setImageBitmap(statebit);
                Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                Drawable pic = changeBitmapContrastBrightness(bitmap, (float) 1.35, -50);
                int[] colors = {color(R.color.filter17p5col1), color(R.color.filter17p5col1)};
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                Drawable twn_pks = getDrawable(R.drawable.twin_pks);
                Drawable[] mas = {pic, gd, twn_pks};
                LayerDrawable ld = new LayerDrawable(mas);
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                ld.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                ld.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }
        }
    }

    public void filter3(View view) {
        ImageView iv = findViewById(R.id.photo);
        switch (page) {
            case (0): {
                iv.setImageBitmap(statebit);
                Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                bitmap = doSharpen(bitmap, new float[]{-0.15f, -0.15f, -0.15f, -0.15f, 2.2f, -0.15f, -0.15f, -0.15f, -0.15f});
                Drawable pic = changeBitmapContrastBrightness(bitmap, (float) 1.5, -40);
                int[] colors = {color(R.color.filter3p0col1), color(R.color.filter3p0col2)};
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.BL_TR, colors);
                Drawable[] mas = {pic, gd};
                LayerDrawable ld = new LayerDrawable(mas);
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                ld.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                ld.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }
            case (1): {
                iv.setImageBitmap(statebit);
                Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                Drawable pic = changeBitmapContrastBrightness(bitmap, (float) 1.08, -20);
                int[] colors = {color(R.color.filter6p1col1), color(R.color.filter6p1col1)};
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                Drawable[] mas = {pic, gd};
                LayerDrawable ld = new LayerDrawable(mas);
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                ld.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                ld.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }
            case (2): {
                iv.setImageBitmap(statebit);
                Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                Drawable pic = changeBitmapContrastBrightness(bitmap, (float) 1.3, 10);
                int[] colors = {color(R.color.filter9p2col1), color(R.color.filter9p2col2), color(R.color.filter9p2col2),
                        color(R.color.filter9p2col1), color(R.color.filter9p2col1)};
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                Drawable[] mas = {pic, gd};
                LayerDrawable ld = new LayerDrawable(mas);
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                ld.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                ld.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }

            case (3): {
                iv.setImageBitmap(statebit);
                Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                bitmap = doSharpen(bitmap, new float[]{-0.15f, -0.15f, -0.15f, -0.15f, 2.2f, -0.15f, -0.15f, -0.15f, -0.15f});
                Drawable pic = changeBitmapContrastBrightness(bitmap, (float) 1.45, -40);
                int[] colors = {color(R.color.filter12p3col1), color(R.color.filter12p3col1)};
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                Drawable[] mas = {pic, gd};
                LayerDrawable ld = new LayerDrawable(mas);
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                ld.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                ld.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }
            case (4): {
                iv.setImageBitmap(statebit);
                Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                bitmap = doSharpen(bitmap, new float[]{-0.15f, -0.15f, -0.15f, -0.15f, 2.2f, -0.15f, -0.15f, -0.15f, -0.15f});
                Drawable pic = changeBitmapContrastBrightness(bitmap, (float) 1.35, -5);
                int[] colors = {color(R.color.filter15p4col1), color(R.color.filter15p4col1)};
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                Drawable[] mas = {pic, gd};
                LayerDrawable ld = new LayerDrawable(mas);
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                ld.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                ld.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }

            case (5): {
                iv.setImageBitmap(statebit);
                Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                bitmap = doSharpen(bitmap, new float[]{-0.15f, -0.15f, -0.15f, -0.15f, 2.2f, -0.15f, -0.15f, -0.15f, -0.15f});
                Drawable pic = changeBitmapContrastBrightness(bitmap, (float) 1.4, -50);
                int[] colors = {color(R.color.filter18p5col1), color(R.color.filter18p5col1)};
                GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
                Drawable[] mas = {pic, gd};
                LayerDrawable ld = new LayerDrawable(mas);
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                ld.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                ld.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }
        }
    }

    public int color(int id) {
        return getResources().getColor(id);
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

    public Drawable changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(
                new float[]{
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                }
        );
        Drawable pic = new BitmapDrawable(getResources(), bmp);
        pic.setColorFilter(new ColorMatrixColorFilter(cm));
        return pic;
    }

    public void save(View view) {
        ImageView iv = findViewById(R.id.photo);
        Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
        picUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "MFPhoto", ""));
        Log.d("MyTag", picUri.toString());
        statebit = bitmap;
    }

    public void back(View view) {
        Intent intent = new Intent(Editor.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public void crop(View view) {
        CropImage.activity(picUri)
                .start(this);
    }

    public void rollback(View view) {
        ImageView iv = findViewById(R.id.photo);
        iv.setImageBitmap(statebit);
    }

    public void share(View view) throws IOException {
        ImageView iv = findViewById(R.id.photo);
        Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
        File photoFile = null;
        photoFile = createImageFile();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        byte[] bitmapData = bytes.toByteArray();
        //write the bytes in file
        FileOutputStream fos = new FileOutputStream(photoFile);
        fos.write(bitmapData);
        fos.flush();
        fos.close();
        Uri contentUri = FileProvider.getUriForFile(Editor.this, "com.example.moviefilter.provider", photoFile);
        if (contentUri != null) {
            Log.d("MyTag", "Uri");
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
            shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.setType("image/png");
            startActivity(Intent.createChooser(shareIntent, "Choose an app"));
        } else {
            Log.d("MyTag", "Not Uri");
        }
    }

    public void faceDetect(final Bitmap bit) throws InterruptedException {
        InputImage image = InputImage.fromBitmap(bit, 0);
        FaceDetectorOptions realTimeOpts =
                new FaceDetectorOptions.Builder()
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .build();
        FaceDetector detector = FaceDetection.getClient(realTimeOpts);
        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        for (Face face : faces) {
                                            List<PointF> faceContour =
                                                    face.getContour(FaceContour.FACE).getPoints();
                                            List<PointF> nose =
                                                    face.getContour(FaceContour.NOSE_BRIDGE).getPoints();
                                            faceDraw(faceContour, nose);
                                        }
                                    }
                                });
    }

    public void faceDraw(List<PointF> faceContour, List<PointF> nose) {
        PointF[] faceContourMas = faceContour.toArray(new PointF[faceContour.size()]);
        PointF[] noseMas = nose.toArray(new PointF[nose.size()]);
        ImageView iv = findViewById(R.id.photo);
        Bitmap bit = ((BitmapDrawable) iv.getDrawable()).getBitmap();
        Bitmap mutableBitmap = bit.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        float center_x = 0, center_y = 0;
        for (int i = 0; i < noseMas.length; i++)
            if (noseMas[i].y > center_y) {
                center_y = noseMas[i].y;
                center_x = noseMas[i].x;
            }
        canvas.drawBitmap(mutableBitmap, 0, 0, paint);
        Path path = new Path();
        path.moveTo(faceContourMas[0].x, faceContourMas[0].y);
        float max_x = faceContourMas[0].x, max_y = faceContourMas[0].y;
        float min_x = faceContourMas[0].x, min_y = faceContourMas[0].y;
        for (int i = 1; i < faceContourMas.length; i++) {
            path.lineTo(faceContourMas[i].x, faceContourMas[i].y);
            if (faceContourMas[i].x > max_x) max_x = faceContourMas[i].x;
            if (faceContourMas[i].x < min_x) min_x = faceContourMas[i].x;
            if (faceContourMas[i].y > max_y) max_y = faceContourMas[i].y;
            if (faceContourMas[i].y < min_y) min_y = faceContourMas[i].y;
        }
        paint.setShader(new RadialGradient(center_x, center_y, (max_y - min_y) / 3, color(R.color.filter7p2col3),
                color(R.color.filter7p2col2), Shader.TileMode.CLAMP));
        canvas.drawPath(path, paint);
        iv.setImageBitmap(mutableBitmap);
    }

    public void studio(View view) {
        Intent intent = new Intent(Editor.this, FilterStudio.class);
        Log.d("MyTag", picUri.toString());
        intent.putExtra("uri", picUri);
        startActivity(intent);
        finish();
    }

    public void aiFilterMaker(View view) {
        Intent intent = new Intent(Editor.this, AIStudio.class);
        Log.d("MyTag", picUri.toString());
        intent.putExtra("uri", picUri);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Editor.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}