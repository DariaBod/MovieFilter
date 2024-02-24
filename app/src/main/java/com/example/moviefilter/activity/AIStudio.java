package com.example.moviefilter.activity;

import static android.graphics.ColorSpace.connect;

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
import android.graphics.drawable.GradientDrawable;
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
        Log.d("MyTag", picUri.toString());
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
        Bitmap resultBitmap = createBlurredBitmap(transparentColors, 10, this);
        // GradientDrawable gd = createBitmap1(result);
        ImageView iv = findViewById(R.id.photo_for_filter);
        iv.setImageBitmap(resultBitmap);
        /* int width = exampleStatebit.getWidth() / 8;
        int height = exampleStatebit.getHeight() / 8;
        int[][] colorMatrix = new int[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                colorMatrix[i][j] = getAverageColor(i, j, width, height);
            }
        }
        Log.d("Dasha", Arrays.deepToString(colorMatrix));
        int[] colors1 = colorMatrix[0];
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors1);
        int[] colors2 = colorMatrix[1];
        GradientDrawable gd2 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors2);
        GradientDrawable[] mas = {gd, gd2};
        ImageView iv = findViewById(R.id.photo_for_filter);
        iv.setImageBitmap(createFilter(mas));*/
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

    private Bitmap createBitmapFromColors(int[][] averageColors, int width, int height) {
        Bitmap resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < averageColors.length; i++) {
            for (int j = 0; j < averageColors[i].length; j++) {
                int color = averageColors[i][j];
                for (int x = j * (width / averageColors[i].length); x < (j + 1) * (width / averageColors[i].length); x++) {
                    for (int y = i * (height / averageColors.length); y < (i + 1) * (height / averageColors.length); y++) {
                        resultBitmap.setPixel(x, y, color);
                    }
                }
            }
        }
        return resultBitmap;
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

        //applyBlur(bitmap, blurRadius, context);

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

    public static Bitmap createBitmap1(int[][] result, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length - 1; j++) {
                int startX = j * width / result[i].length;
                int endX = (j + 1) * width / result[i].length;

                int[] colors = {result[i][j], result[i][j + 1]};
                float[] positions = {0, 1};

                LinearGradient gradient = new LinearGradient(startX, 0, endX, 0, colors, positions, Shader.TileMode.CLAMP);

                Paint paint = new Paint();
                paint.setShader(gradient);

                int y = i * height / result.length;
                int nextY = (i + 1) * height / result.length;

                // Рисуем прямоугольник с градиентом
                canvas.drawRect(startX, y, endX, nextY, paint);
            }
        }

        return bitmap;
    }

    public static GradientDrawable createBitmap2(int[][] colors) {
        GradientDrawable gradientDrawable = new GradientDrawable();

        // Устанавливаем цвета градиента для каждого слоя
        int[] layerColors = new int[colors.length * colors[0].length];
        int index = 0;

        for (int i = 0; i < colors.length; i++) {
            for (int j = 0; j < colors[i].length; j++) {
                layerColors[index++] = colors[i][j];
            }
        }

        gradientDrawable.setColors(layerColors);

        // Дополнительные параметры градиента
        //gradientDrawable.setGradientType();
        gradientDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);

        // Установка радиуса углов (если необходимо)
        gradientDrawable.setCornerRadii(new float[]{0, 0, 0, 0, 20, 20, 20, 20});

        return gradientDrawable;
    }

    public Bitmap createFilter(GradientDrawable[] gradientDrawables) {
        int width = statebit.getWidth();
        int height = statebit.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

// Вычисление высоты каждой полосы
        int stripeHeight = height / gradientDrawables.length;

// Рисуем каждый GradientDrawable на соответствующей части Bitmap
        for (int i = 0; i < gradientDrawables.length; i++) {
            gradientDrawables[i].setBounds(0, i * stripeHeight, width, (i + 1) * stripeHeight);
            gradientDrawables[i].draw(canvas);
        }
        return bitmap;
    }

    public int getAverageColor(int i, int j, int width, int height) {
        int averageColor = 0;
        for (int w = i * width; w < (i + 1) * width; w++) {
            for (int h = j * height; h < (j + 1) * height; h++) {
                averageColor += exampleStatebit.getColor(w, h).toArgb();
            }
        }
        return averageColor / (width * height);
    }

    public void saveFS(View view) {
        ImageView iv = findViewById(R.id.photo_for_filter);
        Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
        picUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "MFPhoto", ""));
        statebit = bitmap;
        Log.d("MyTag", picUri.toString());
    }

    public void backFS(View view) {
        super.onBackPressed();
        Intent intent = new Intent(AIStudio.this, Editor.class);
        Log.d("MyTag", picUri.toString());
        intent.putExtra("uri", picUri);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("MyTag", picUri.toString());
        Intent intent = new Intent(AIStudio.this, Editor.class);
        intent.putExtra("uri", picUri);
        startActivity(intent);
        finish();
    }
}