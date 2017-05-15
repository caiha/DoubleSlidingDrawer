package com.caiha.doubleslidingdrawer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        List<Fragment> list = new ArrayList();
        Fragment fragment1 = new Fragment1();
        Fragment fragment2 = new Fragment2();
        list.add(fragment1);
        list.add(fragment2);
        AirPortAdapter airPortAdapter = new AirPortAdapter(getSupportFragmentManager(), list);
        ViewPager viewById = (ViewPager) findViewById(R.id.content);
        viewById.setAdapter(airPortAdapter);
    }

    public class AirPortAdapter extends FragmentPagerAdapter {
        List<Fragment> fs;

        public AirPortAdapter(FragmentManager fm, List fs) {
            super(fm);
            this.fs = fs;
        }

        @Override
        public Fragment getItem(int arg0) {
            return fs.get(arg0);
        }

        @Override
        public int getCount() {
            return fs.size();
        }

    }
}
