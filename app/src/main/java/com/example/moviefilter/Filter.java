package com.example.moviefilter;

import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class Filter extends Fragment {

    public Filter(){};

    //создаём и заполняем разметку для текущего элемента
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment,container,false);
        Bundle content = getArguments(); //получаем из адаптера контент для заполнения полей разметки
        final ImageView filter1, filter2, filter3;
        TextView filname1, filname2, filname3;
        filname1 = view.findViewById(R.id.filname1); //текстовое поле
        filname1.setText(content.getString("filname1"));
        filname2 = view.findViewById(R.id.filname2); //текстовое поле
        filname2.setText(content.getString("filname2"));
        filname3 = view.findViewById(R.id.filname3); //текстовое поле
        filname3.setText(content.getString("filname3"));//заполняем данными из а-списка по ключу year*/
        filter1 = view.findViewById(R.id.filter1); // поле для картинки
        filter1.setImageResource(content.getInt("fil1"));
        filter1.setTag(content.getInt("tag1"));
        filter2 = view.findViewById(R.id.filter2); // поле для картинки
        filter2.setImageResource(content.getInt("fil2")); //заполня
        filter2.setTag(null);
        filter2.setTag(content.getInt("tag2"));
        filter3 = view.findViewById(R.id.filter3); // поле для картинки
        filter3.setImageResource(content.getInt("fil3")); //заполня
        filter3.setTag(null);
        filter3.setTag(content.getInt("tag3"));
        return view;
    }

}
