package com.example.moviefilter;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class MyAdapt extends FragmentStatePagerAdapter {
    //Данные для заполнения контента
    String []text1 = {"Неоновый демон","Титаник","Бегущий по лезвию"};
    String []text2 = {"Отель 'Гранд Будапешт'","Великий Гэтсби","Ла-Ла Ленд"};
    String []text3 = {"Сумерки", "Королевство полной луны", "Властелин Колец"};
    int []photo1 = {R.drawable.filt1, R.drawable.filt4, R.drawable.filt7};
    int[]photo2={ R.drawable.filt2, R.drawable.filt5, R.drawable.filt8};
    int[]photo3={R.drawable.filt3, R.drawable.filt6, R.drawable.filt9};
    //конструктор
    public MyAdapt(@NonNull FragmentManager fm) {
        super(fm);   }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        Bundle content = new Bundle(); //данные передаются в виде ассоциативного списка из элементов массивов с индексом position
        content.putInt("fil1",photo1[position]);
        content.putInt("fil2",photo2[position]);
        content.putInt("fil3",photo3[position]);
        content.putString("filname1",text1[position]);
        content.putString("filname2",text2[position]);
        content.putString("filname3",text3[position]);
        Filter info = new Filter(); //создаём фрагмент Filter.java
        info.setArguments(content); //заполняем его поля значениями
        return info; // передаём в onCreateView в Filter.java
    }

    @Override
    public int getCount() {
        return photo1.length;
    }// количество экранов пейджера
}
