package com.example.moviefilter.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.percentlayout.widget.PercentRelativeLayout;

import com.example.moviefilter.R;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.jaredrummler.android.colorpicker.ColorShape;

public class FilterStudio extends AppCompatActivity implements ColorPickerDialogListener {
    String selected_fill;
    int color_count;
    Uri picUri;
    int color_1 = 0, color_2 = 0, color_3 = 0, color_4 = 0, color_5 = 0;
    Bitmap statebit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_studio);
        int[] colors = {color(R.color.grad_button_1_1), color(R.color.grad_button_1_2), color(R.color.grad_button_1_3)};
        GradientDrawable gd1 = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        Button b1 = findViewById(R.id.fill_1);
        b1.setBackground(gd1);
        GradientDrawable gd2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
        Button b2 = findViewById(R.id.fill_2);
        b2.setBackground(gd2);
        GradientDrawable gd3 = new GradientDrawable(GradientDrawable.Orientation.TR_BL, colors);
        Button b3 = findViewById(R.id.fill_3);
        b3.setBackground(gd3);
        GradientDrawable gd4 = new GradientDrawable(GradientDrawable.Orientation.BR_TL, colors);
        Button b4 = findViewById(R.id.fill_4);
        b4.setBackground(gd4);
        Button b5 = findViewById(R.id.fill_5);
        b5.setBackgroundColor(color(R.color.grad_button_1_2));
        ImageView iv = findViewById(R.id.photo_for_filter);
        Bundle bd = getIntent().getExtras();
        picUri = bd.getParcelable("uri");
        Log.d("MyTag", picUri.toString());
        iv.setImageURI(picUri);
        Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
        statebit = bitmap;
    }

    public void plus_color(View view) {
        PercentRelativeLayout layout = findViewById(R.id.color_layout);
        layout.setVisibility(View.VISIBLE);
        if (color_count >= 5) color_count = 5;
        else if (color_count < 1) color_count = 1;
        else color_count++;
        color_change();
    }

    public void minus_color(View view) {
        PercentRelativeLayout layout = findViewById(R.id.color_layout);
        layout.setVisibility(View.VISIBLE);
        if (color_count < 1) color_count = 1;
        else if (color_count > 5) color_count = 4;
        else color_count--;
        color_change();
    }

    private void createColorPickerDialog(int id) {
        ColorPickerDialog.newBuilder()
                .setColor(Color.RED)
                .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                .setAllowCustom(true)
                .setShowAlphaSlider(true)
                .setAllowPresets(true)
                .setColorShape(ColorShape.SQUARE)
                .setDialogId(id)
                .show(this);
    }

    public void fill_1(View view) {
        selected_fill = "l-r";
        newFill();
    }

    public void fill_2(View view) {
        selected_fill = "t-b";
        newFill();
    }

    public void fill_3(View view) {
        selected_fill = "tr-bl";
        newFill();
    }

    public void fill_4(View view) {
        selected_fill = "br-tl";
        newFill();
    }

    public void fill_5(View view) {
        selected_fill = "full";
        PercentRelativeLayout layout = findViewById(R.id.color_layout);
        layout.setVisibility(View.VISIBLE);
        color_count = 1;
        Button b1 = findViewById(R.id.more_color);
        Button b2 = findViewById(R.id.less_color);
        b1.setVisibility(View.INVISIBLE);
        b2.setVisibility(View.INVISIBLE);
        color_change();
    }

    public void newFill() {
        PercentRelativeLayout layout = findViewById(R.id.color_layout);
        layout.setVisibility(View.VISIBLE);
        Button b1 = findViewById(R.id.more_color);
        Button b2 = findViewById(R.id.less_color);
        b1.setVisibility(View.VISIBLE);
        b2.setVisibility(View.VISIBLE);
        color_count = 1;
        color_change();
    }

    public void color_change() {
        Button c1 = findViewById(R.id.color_1);
        Button c2 = findViewById(R.id.color_2);
        Button c3 = findViewById(R.id.color_3);
        Button c4 = findViewById(R.id.color_4);
        Button c5 = findViewById(R.id.color_5);
        color_1 = 0;
        color_2 = 0;
        color_3 = 0;
        color_4 = 0;
        color_5 = 0;
        c1.setBackgroundColor(0xFF949AE9);
        c2.setBackgroundColor(0xFF949AE9);
        c3.setBackgroundColor(0xFF949AE9);
        c4.setBackgroundColor(0xFF949AE9);
        c5.setBackgroundColor(0xFF949AE9);
        switch (color_count) {
            case 1: {
                c1.setVisibility(View.INVISIBLE);
                c2.setVisibility(View.INVISIBLE);
                c3.setVisibility(View.VISIBLE);
                c4.setVisibility(View.INVISIBLE);
                c5.setVisibility(View.INVISIBLE);
                break;
            }
            case 2: {
                c1.setVisibility(View.INVISIBLE);
                c2.setVisibility(View.VISIBLE);
                c3.setVisibility(View.INVISIBLE);
                c4.setVisibility(View.VISIBLE);
                c5.setVisibility(View.INVISIBLE);
                break;
            }
            case 3: {
                c1.setVisibility(View.INVISIBLE);
                c2.setVisibility(View.VISIBLE);
                c3.setVisibility(View.VISIBLE);
                c4.setVisibility(View.VISIBLE);
                c5.setVisibility(View.INVISIBLE);
                break;
            }
            case 4: {
                c1.setVisibility(View.VISIBLE);
                c2.setVisibility(View.VISIBLE);
                c3.setVisibility(View.VISIBLE);
                c4.setVisibility(View.VISIBLE);
                c5.setVisibility(View.INVISIBLE);
                break;
            }
            case 5: {
                c1.setVisibility(View.VISIBLE);
                c2.setVisibility(View.VISIBLE);
                c3.setVisibility(View.VISIBLE);
                c4.setVisibility(View.VISIBLE);
                c5.setVisibility(View.VISIBLE);
                break;
            }
            default: {
                c1.setVisibility(View.INVISIBLE);
                c2.setVisibility(View.INVISIBLE);
                c3.setVisibility(View.VISIBLE);
                c4.setVisibility(View.INVISIBLE);
                c5.setVisibility(View.INVISIBLE);
                break;
            }
        }
    }

    public void pickColor_1(View view) {
        createColorPickerDialog(1);
    }

    public void pickColor_2(View view) {
        createColorPickerDialog(2);
    }

    public void pickColor_3(View view) {
        createColorPickerDialog(3);
    }

    public void pickColor_4(View view) {
        createColorPickerDialog(4);
    }

    public void pickColor_5(View view) {
        createColorPickerDialog(5);
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case 1: {
                color_1 = color;
                Button b1 = findViewById(R.id.color_1);
                b1.setBackgroundColor(color);
                break;
            }
            case 2: {
                color_2 = color;
                Button b2 = findViewById(R.id.color_2);
                b2.setBackgroundColor(color);
                break;
            }
            case 3: {
                color_3 = color;
                Button b3 = findViewById(R.id.color_3);
                b3.setBackgroundColor(color);
                break;
            }
            case 4: {
                color_4 = color;
                Button b4 = findViewById(R.id.color_4);
                b4.setBackgroundColor(color);
                break;
            }
            case 5: {
                color_5 = color;
                Button b5 = findViewById(R.id.color_5);
                b5.setBackgroundColor(color);
                break;
            }
        }
    }

    public void createFilter(View view) {
        ImageView iv = findViewById(R.id.photo_for_filter);
        iv.setImageBitmap(statebit);
        Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
        switch (selected_fill) {
            case "l-r": {
                filter(GradientDrawable.Orientation.LEFT_RIGHT);
                break;
            }
            case "t-b": {
                filter(GradientDrawable.Orientation.TOP_BOTTOM);
                break;
            }
            case "tr-bl": {
                filter(GradientDrawable.Orientation.TR_BL);
                break;
            }
            case "br-tl": {
                filter(GradientDrawable.Orientation.BR_TL);
                break;
            }
            case "full": {
                if (color_3 == 0) break;
                iv.getDrawable().setColorFilter(new PorterDuffColorFilter(color_3, PorterDuff.Mode.SRC_ATOP));
                Drawable dr = iv.getDrawable();
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                dr.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                dr.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }
        }
    }

    public void filter(GradientDrawable.Orientation orientation) {
        ImageView iv = findViewById(R.id.photo_for_filter);
        Drawable pic = iv.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
        switch (color_count) {
            case 1: {
                if (color_3 == 0) break;
                int[] colors = {color_3, color(R.color.clear)};
                GradientDrawable gd = new GradientDrawable(orientation, colors);
                Drawable[] mas = {pic, gd};
                LayerDrawable ld = new LayerDrawable(mas);
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                ld.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                ld.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }
            case 2: {
                if (color_2 == 0 && color_4 == 0) break;
                if (color_2 == 0) color_2 = color(R.color.clear);
                if (color_4 == 0) color_4 = color(R.color.clear);
                int[] colors = {color_2, color_4};
                GradientDrawable gd = new GradientDrawable(orientation, colors);
                Drawable[] mas = {pic, gd};
                LayerDrawable ld = new LayerDrawable(mas);
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                ld.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                ld.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }
            case 3: {
                if (color_2 == 0 && color_4 == 0 && color_3 == 0) break;
                if (color_2 == 0) color_2 = color(R.color.clear);
                if (color_3 == 0) color_3 = color(R.color.clear);
                if (color_4 == 0) color_4 = color(R.color.clear);
                int[] colors = {color_2, color_3, color_4};
                GradientDrawable gd = new GradientDrawable(orientation, colors);
                Drawable[] mas = {pic, gd};
                LayerDrawable ld = new LayerDrawable(mas);
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                ld.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                ld.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }
            case 4: {
                if (color_1 == 0 && color_2 == 0 && color_4 == 0 && color_3 == 0) break;
                if (color_1 == 0) color_1 = color(R.color.clear);
                if (color_2 == 0) color_2 = color(R.color.clear);
                if (color_3 == 0) color_3 = color(R.color.clear);
                if (color_4 == 0) color_4 = color(R.color.clear);
                int[] colors = {color_1, color_2, color_3, color_4};
                GradientDrawable gd = new GradientDrawable(orientation, colors);
                Drawable[] mas = {pic, gd};
                LayerDrawable ld = new LayerDrawable(mas);
                Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                ld.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                ld.draw(new Canvas(b));
                iv.setImageBitmap(b);
                break;
            }
            case 5: {
                if (color_1 == 0 && color_2 == 0 && color_4 == 0 && color_3 == 0 && color_5 == 0)
                    break;
                if (color_1 == 0) color_1 = color(R.color.clear);
                if (color_2 == 0) color_2 = color(R.color.clear);
                if (color_3 == 0) color_3 = color(R.color.clear);
                if (color_4 == 0) color_4 = color(R.color.clear);
                if (color_5 == 0) color_5 = color(R.color.clear);
                int[] colors = {color_1, color_2, color_3, color_4, color_5};
                GradientDrawable gd = new GradientDrawable(orientation, colors);
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

    public void saveFS(View view) {
        ImageView iv = findViewById(R.id.photo_for_filter);
        Bitmap bitmap = ((BitmapDrawable) iv.getDrawable()).getBitmap();
        picUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "MFPhoto", ""));
        statebit = bitmap;
        Log.d("MyTag", picUri.toString());
    }

    public void backFS(View view) {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            intent = new Intent(FilterStudio.this, Editor.class);
        }
        Log.d("MyTag", picUri.toString());
        intent.putExtra("uri", picUri);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("MyTag", picUri.toString());
        Intent intent = null;
        intent = new Intent(FilterStudio.this, Editor.class);
        intent.putExtra("uri", picUri);
        startActivity(intent);
        finish();
    }

    public int color(int id) {
        return getResources().getColor(id);
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        Log.d("MyTag", "Dialog " + dialogId + " dismissed");
    }
}