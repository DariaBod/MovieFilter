package com.example.moviefilter;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class MyAdapt extends FragmentStatePagerAdapter {
    //Данные для заполнения контента
    String []text1 = {"Неоновый демон"};
    String []text2 = {"Отель 'Гранд Будапешт'"};
    String []text3 = {"Сумерки"};
    int []photo1 = {R.drawable.filt1};
    int[]photo2={ R.drawable.filt2};
    int[]photo3={R.drawable.filt3};
    //конструктор
    public MyAdapt(@NonNull FragmentManager fm) {
        super(fm);   }

    //создаём фрагмент на основе класса Cake.java
    @NonNull
    @Override
    public Fragment getItem(int position) {
        Bundle content = new Bundle(); //данные передаются в виде ассоциативного списка из элементов массивов с индексом position
        content.putInt("fil1",photo1[position]);
        content.putInt("fil2",photo2[position]);
        content.putInt("fil3",photo3[position]);// ключ - "photo", значение взято из массива photo
        content.putString("filname1",text1[position]);// ключ - "year", значение взято из массива year
        content.putString("filname2",text2[position]);
        content.putString("filname3",text3[position]);
        Filter info = new Filter(); //создаём фрагмент Cake.java
        info.setArguments(content); //заполняем его поля значениями
        return info; // передаём в onCreateView в Cake.java
    }

    @Override
    public int getCount() {
        return 1;//photo.length;
    }// количество экранов пейджера
}
