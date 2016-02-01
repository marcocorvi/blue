<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical"
    android:layout_width="fill_parent"
    android:layout_height="fill_parent"
    >

    <!-- TextView android:id="@+id/filename"
        android:layout_width="fill_parent"
        android:layout_height="wrap_content"
        android:hint="filename"
    / -->

   <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
      android:orientation="horizontal"
      android:layout_width="fill_parent"
      android:layout_height="wrap_content"
       android:layout_marginBottom="15dp"
      >

      <Button android:id="@+id/btn_save"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/btn_save"
      />

      <Button android:id="@+id/btn_load"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/btn_load"
      />

      <Button android:id="@+id/btn_export"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/btn_export"
      />

      <Button android:id="@+id/btn_delete"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/btn_delete"
      />


  </LinearLayout>
   
  <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="horizontal"
    android:layout_width="fill_parent"
    android:layout_height="fill_parent"
    >

    <!-- LinearLayout
      android:orientation="vertical"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:layout_weight="1"
      -->
      <ListView android:id="@+id/files"
         android:layout_width="0dp"
         android:layout_weight="1"
         android:layout_height="match_parent"
         android:stackFromBottom="false"
         android:transcriptMode="alwaysScroll"
         android:textSize="14sp"
      />  
    <!-- /LinearLayout -->

    <!-- LinearLayout
      android:orientation="vertical"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:layout_weight="1"
      -->
      <!-- TextView 
          android:layout_width="0dp"
          android:layout_weight="1"
          android:layout_height="wrap_content"
          android:hint="sdcard"
      / -->
      <ListView android:id="@+id/files_ext"
         android:layout_width="0dp"
         android:layout_weight="1"
         android:layout_height="match_parent"
         android:stackFromBottom="false"
         android:transcriptMode="alwaysScroll"
         android:textSize="14sp"
      />  
    <!-- /LinearLayout -->

  </LinearLayout>
</LinearLayout>
