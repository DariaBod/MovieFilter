package com.example.moviefilter.activity;

import static android.graphics.ColorSpace.connect;

import static com.example.moviefilter.ImageBeautifier.changeBitmapContrastBrightness;
import static com.example.moviefilter.ImageBeautifier.doSharpen;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moviefilter.ImageProcessor;
import com.example.moviefilter.R;

import java.util.Arrays;

public class AIStudio extends AppCompatActivity {
    static final int GALLERY_CODE = 0;
    Uri picUri;
    Bitmap statebit;
    Bitmap exampleStatebit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_studio);
        ImageView iv = findViewById(R.id.photo_for_filter);
        Bundle bd = getIntent().getExtras();
        picUri = bd.getParcelable("uri");
        iv.setImageURI(picUri);
        Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
        statebit = bitmap;
    }

    public void openGallery(View view) {
        Button button = findViewById(R.id.choose_photo);
        button.setVisibility(View.INVISIBLE);
        Intent GalleryIntent = null;
        GalleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        GalleryIntent.setType("image/*");
        GalleryIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(GalleryIntent, GALLERY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri ImageURI = data.getData();
        ImageView iv1 = findViewById(R.id.photo_for_example);
        iv1.setImageURI(ImageURI);
        exampleStatebit = ((BitmapDrawable) iv1.getDrawable()).getBitmap();
    }

    public void createAIFilter(View view) {
        int[][] result = ImageProcessor.splitImage(exampleStatebit, 8, 8);
          int alpha = (int) (255 * 0.4);
        int[][] transparentColors = setAlphaForColors(result, alpha);
        Bitmap resultBitmap = createBlurredBitmap(transparentColors, 1, this);
        Bitmap bitmap = doSharpen(statebit, new float[]{-0.15f, -0.15f, -0.15f, -0.15f, 2.2f, -0.15f, -0.15f, -0.15f, -0.15f}, this);
        Drawable pic = changeBitmapContrastBrightness(bitmap, (float) 1.5, -30);
        Drawable[] mas = {pic, new BitmapDrawable(resultBitmap)};
        LayerDrawable ld = new LayerDrawable(mas);
        Bitmap b = Bitmap.createBitmap(statebit.getWidth(), statebit.getHeight(), Bitmap.Config.ARGB_8888);
        ld.setBounds(0, 0, statebit.getWidth(), statebit.getHeight());
        ld.draw(new Canvas(b));
        ImageView iv = findViewById(R.id.photo_for_filter);
        iv.setImageBitmap(b);
    }

    public static int[][] setAlphaForColors(int[][] colors, int alpha) {
        int[][] resultColors = new int[colors.length][colors[0].length];
        for (int i = 0; i < colors.length; i++) {
            for (int j = 0; j < colors[i].length; j++) {
                resultColors[i][j] = setAlpha(colors[i][j], alpha);
            }
        }
        return resultColors;
    }

    public static int setAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    public static Bitmap createBlurredBitmap(int[][] transparentColors, int blurRadius, Context context) {
        int width = transparentColors[0].length;
        int height = transparentColors.length;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                bitmap.setPixel(j, i, transparentColors[i][j]);
            }
        }
        applyBlur(bitmap, blurRadius, context);
        return bitmap;
    }

    private static void applyBlur(Bitmap bitmap, int blurRadius, Context context) {
        RenderScript renderScript = RenderScript.create(context);

        Allocation input = Allocation.createFromBitmap(renderScript, bitmap);
        Allocation output = Allocation.createTyped(renderScript, input.getType());

        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        blurScript.setInput(input);
        blurScript.setRadius(blurRadius);
        blurScript.forEach(output);

        output.copyTo(bitmap);

        renderScript.destroy();
    }

    public void saveFS(View view) {
        ImageView iv = findViewById(R.id.photo_for_filter);
        Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
        picUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "MFPhoto", ""));
        statebit = bitmap;
    }

    public void backFS(View view) {
        super.onBackPressed();
        Intent intent = new Intent(AIStudio.this, Editor.class);
        intent.putExtra("uri", picUri);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(AIStudio.this, Editor.class);
        intent.putExtra("uri", picUri);
        startActivity(intent);
        finish();
    }
}