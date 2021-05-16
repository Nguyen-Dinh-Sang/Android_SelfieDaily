package com.example.selfie;

import androidx.annotation.LongDef;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String currentPhotoPath;
    private List<Image> listPathImage = new ArrayList<>();
    private List<Image> listSelectedImage;
    private RecyclerView scrollView;
    private GridLayoutManager gridLayoutManager;
    private AdapterImageList adapterImageList;
    private boolean isMultiSelected = false;
    String TAG = "AAAAA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adapterImageList = new AdapterImageList(this, listPathImage);
        scrollView = findViewById(R.id.picker);
        gridLayoutManager = new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false);
        scrollView.setLayoutManager(gridLayoutManager);

        File folder = new File(Environment.getExternalStorageDirectory().toString() + "/Android/data/com.example.selfie/files/Pictures/");

        if (folder.exists()) {
            File[] allFiles = folder.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".jpg"));
                }
            });

            listPathImage.clear();
            for (File file : allFiles) {
                listPathImage.add(new Image(file.getAbsolutePath()));
            }

            adapterImageList.setHasStableIds(true);
            scrollView.setAdapter(adapterImageList);

            adapterImageList.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    if (position >= listPathImage.size()) return;

                    if (!isMultiSelected) {
                        dialogReview(listPathImage.get(position).getPath());
                    } else {
                        if (listPathImage.get(position).isSelected()) {
                            listPathImage.get(position).setSelected(false);
                            listSelectedImage.remove(listPathImage.get(position));
                            if (listSelectedImage.size() < 1) {
                                isMultiSelected = false;
                            }
                        } else {
                            listPathImage.get(position).setSelected(true);
                            listSelectedImage.add(listPathImage.get(position));
                        }
                        adapterImageList.notifyItemChanged(position);
                    }
                }

                @Override
                public void onItemLongClick(int position) {
                    if (position >= listPathImage.size()) return;
                    if (!isMultiSelected) {
                        isMultiSelected = true;
                        listPathImage.get(position).setSelected(true);
                        adapterImageList.notifyItemChanged(position);
                        listSelectedImage = null;
                        listSelectedImage = new ArrayList<>();
                        listSelectedImage.add(listPathImage.get(position));
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_camera:
                dispatchTakePictureIntent();
                return true;
            case R.id.item_setting:
                dialogSetting();
                return true;
            case R.id.item_delete_selected:
                if (listSelectedImage != null) {
                    for (Image image : listSelectedImage) {
                        removedFile(image.getPath());
                    }
                }

                File folder = new File(Environment.getExternalStorageDirectory().toString() + "/Android/data/com.example.selfie/files/Pictures/");
                if (folder.exists()) {
                    File[] allFiles = folder.listFiles(new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            return (name.endsWith(".jpg"));
                        }
                    });

                    listPathImage.clear();
                    for (File file : allFiles) {
                        listPathImage.add(new Image(file.getAbsolutePath()));
                    }

                    isMultiSelected = false;
                    adapterImageList.notifyDataSetChanged();
                }
                return true;
            case R.id.item_delete_all:
                for (Image image : listPathImage) {
                    removedFile(image.getPath());
                }
                listPathImage.clear();
                adapterImageList.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            photoFile = createImageFile();
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            dialogReview(null);
        }
    }

    private void dialogReview(String link) {
        Log.d(TAG, "dialogReview: " + link);
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialogs_review);

        ImageView imageView = dialog.findViewById(R.id.iv_review);
        TextView textViewOk = dialog.findViewById(R.id.tv_ok);
        TextView textViewReTake = dialog.findViewById(R.id.tv_retake);
        if(link != null){
            Glide.with(this)
                    .load(link)
                    .into(imageView);
        } else {
            Glide.with(this)
                    .load(currentPhotoPath)
                    .into(imageView);
        }

        textViewOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (link == null) {
                    galleryAddPic();

                    File folder = new File(Environment.getExternalStorageDirectory().toString() + "/Android/data/com.example.selfie/files/Pictures/");
                    if (folder.exists()) {
                        File[] allFiles = folder.listFiles(new FilenameFilter() {
                            public boolean accept(File dir, String name) {
                                return (name.endsWith(".jpg"));
                            }
                        });

                        listPathImage.clear();
                        for (File file : allFiles) {
                            listPathImage.add(new Image(file.getAbsolutePath()));
                        }
                        adapterImageList.notifyDataSetChanged();
                    }
                    dialog.dismiss();
                } else {
                    dialog.dismiss();
                }
            }
        });

        textViewReTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (link == null) {
                    removedFile(currentPhotoPath);
                    dispatchTakePictureIntent();
                    dialog.dismiss();
                } else {
                    dispatchTakePictureIntent();
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }

    private File createImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        currentPhotoPath = image.getAbsolutePath();
        Log.d("AAAAA", "createImageFile: " + currentPhotoPath);
        return image;
    }

    private void removedFile(String link) {
        File file = new File(link);
        boolean deleted = file.delete();
        if (deleted) {
            Log.d("AAAAA", "Delete: đã xóa");
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void dialogSetting() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialogs_setting);

        Switch switchSetting = dialog.findViewById(R.id.switch_setting);
        TextView textViewOk = dialog.findViewById(R.id.tv_ok2);
        textViewOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        switchSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((Switch) v).isChecked();
                if (checked) {
                    Intent serviceIntent = new Intent(MainActivity.this, ExampleService.class);
                    serviceIntent.putExtra("inputExtra", "ok");

                    startService(serviceIntent);
                } else {
                    Intent serviceIntent = new Intent(MainActivity.this, ExampleService.class);
                    stopService(serviceIntent);
                }
            }
        });

        dialog.show();
    }
}