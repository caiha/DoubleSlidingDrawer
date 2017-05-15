# DoubleSlidingDrawer
使用方法
```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:sliding="http://schemas.android.com/apk/res-auto"
    android:layout_width="fill_parent"
    android:layout_height="fill_parent"
    android:orientation="vertical">

    <com.caiha.library.DoubleSlidingDrawer
        android:id="@+id/slidingdrawer"
        android:layout_width="fill_parent"
        android:layout_height="fill_parent"
        sliding:content="@+id/content"
        sliding:handle="@+id/handle"
        sliding:offset="0dp">

        <RadioGroup
            android:id="@+id/handle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="vertical">

            <RadioButton
                android:id="@+id/btnCn"
                android:layout_width="wrap_content"
                android:layout_height="100dp"
                android:background="@drawable/btn_background_seletor"
                android:button="@null"
                android:gravity="center"
                android:text="第\n一\n页"
                android:textColor="#FF36648B" />

            <RadioButton
                android:id="@+id/btnNet"
                android:layout_width="wrap_content"
                android:layout_height="100dp"
                android:background="@drawable/btn_background_seletor"
                android:button="@null"
                android:gravity="center"
                android:text="第\n二\n页"
                android:textColor="#FF36648B" />
        </RadioGroup>

        <android.support.v4.view.ViewPager
            android:id="@+id/content"
            android:layout_width="match_parent"
            android:layout_height="match_parent"></android.support.v4.view.ViewPager>
    </com.caiha.library.DoubleSlidingDrawer>

</LinearLayout>
```


