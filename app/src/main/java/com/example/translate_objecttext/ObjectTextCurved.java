package com.example.translate_objecttext;

import static com.example.translate_objecttext.Helper.deleteFileOnExit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import java.io.File;
import java.io.IOException;

//Summary - Fragment Java class that holds the UI of the TEXT part in the app's main screen

public class ObjectTextCurved extends AppCompatActivity {

    //UI
    CurvedBottomNavigationView curvedBottomNavigationView;

    ObjectFragment_crv objectFragment;
    fragment_text_crv fragment_text_crv;
    Spinner sourceLangSelector;

    //Get languages list
    ArrayAdapter<TranslateViewModel.Language> adapter = null;
    TranslateViewModel.Language language = null;

    //For Image (Gallary, Camera)
    public static final String FILE_NAME = "temp.jpg";
    private static final int MAX_DIMENSION = 1200;
    private static final String TAG = ObjectTextCurved.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    Bitmap bitmap = null;

    public ILoadImage iLoadImage;
    String lng = null;

    int id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_text_curved);

        initUI();
    }

    private void initUI() {
        curvedBottomNavigationView = findViewById(R.id.curvedBottomNavigationView);
        curvedBottomNavigationView.inflateMenu(R.menu.bottom_nav_menu);
        sourceLangSelector = findViewById(R.id.sourceLangSelector);

        objectFragment = new ObjectFragment_crv();
        fragment_text_crv = new fragment_text_crv();

        final TranslateViewModel viewModel = new TranslateViewModel(getApplication());
        adapter = new ArrayAdapter<>(this, R.layout.spinner_item, viewModel.getAvailableLanguages());
        sourceLangSelector.setAdapter(adapter);
        sourceLangSelector.setSelection(adapter.getPosition(new TranslateViewModel.Language("en")));
        sourceLangSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                language = adapter.getItem(sourceLangSelector.getSelectedItemPosition());
                lng = language.getCode();


                int pos = curvedBottomNavigationView.getSelectedItemId();
                if (pos == R.id.tab_object) {
                    setFragment(objectFragment);
                    try {
                        iLoadImage = (ILoadImage) objectFragment;
                        if (iLoadImage != null) {
                            iLoadImage.onLoadImage(lng);
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }

                    if(getFragmentRefreshListener()!= null) {
                        getFragmentRefreshListener().onRefresh();
                    }
                } else if (pos == R.id.tab_text) {
                    setFragment(fragment_text_crv);
                    try {
                        iLoadImage = (ILoadImage) fragment_text_crv;
                        if (iLoadImage != null) {
                            iLoadImage.onLoadImage(lng);
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    if(getFragmentRefreshListener()!= null) {
                        getFragmentRefreshListener().onRefresh();
                    }
                }

//                deleteFileOnExit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        setFragment(objectFragment);


        curvedBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                id = item.getItemId();
                if (id == R.id.tab_object) {
                    setFragment(objectFragment);
//                    try {
//                        iLoadImage = (ILoadImage) objectFragment;
//                        if (iLoadImage != null) {
//                            iLoadImage.onLoadImage(bitmap, lng);
//                        }
//                    } catch (Exception exception) {
//                        exception.printStackTrace();
//                    }
                    return true;
                } else if (id == R.id.tab_text) {
                    setFragment(fragment_text_crv);
//                    try {
//                        iLoadImage = (ILoadImage) fragment_text_crv;
//                        if (iLoadImage != null) {
//                            iLoadImage.onLoadImage(bitmap, lng);
//                        }
//                    } catch (Exception exception) {
//                        exception.printStackTrace();
//                    }
                    return true;
                }

                return false;
            }
        });
    }

    void setFragment(Fragment fr) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.dashboard_frame, fr);
        fragmentTransaction.commit();
    }


    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST)) {
            startScan(ScanConstants.OPEN_MEDIA);
        }
    }

    protected void startScan(int preference) {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        startActivityForResult(intent, 1001);
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            try {
                Uri scannedResultUri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
                uploadImage(scannedResultUri);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "onActivityResult: Error : " + e.getMessage());
                finish();
            }
        }

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            uploadImage(photoUri);
        }

        int pos = curvedBottomNavigationView.getSelectedItemId();
        if (pos == R.id.tab_object) {
            setFragment(objectFragment);
            try {
                iLoadImage = (ILoadImage) objectFragment;
                if (iLoadImage != null) {
                    iLoadImage.onLoadImage(lng);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } else if (pos == R.id.tab_text) {
            setFragment(fragment_text_crv);
            try {
                iLoadImage = (ILoadImage) fragment_text_crv;
                if (iLoadImage != null) {
                    iLoadImage.onLoadImage(lng);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        deleteFileOnExit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (PermissionUtils.permissionGranted(requestCode, 1001, grantResults)) {
                permissionGrantedAndLocationEnabled = true;
            } else {
                permissionGrantedAndLocationEnabled = false;
                PermissionUtils.setShouldShowStatus(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    //Load image into  imageView
    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                MAX_DIMENSION);
                if (iLoadImage != null) {
                    iLoadImage.onLoadImage(lng);
                }


                if(getFragmentRefreshListener()!= null) {
                    getFragmentRefreshListener().onRefresh();
                }
                //mMainImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    public static Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    //Listener For Refresh Fragment and set data into fragment
    private FragmentRefreshListener fragmentRefreshListener;

    public FragmentRefreshListener getFragmentRefreshListener() {
        return fragmentRefreshListener;
    }

    public void setFragmentRefreshListener(FragmentRefreshListener fragmentRefreshListener) {
        this.fragmentRefreshListener = fragmentRefreshListener;
    }

    public interface FragmentRefreshListener{
        void onRefresh();
    }

    private boolean permissionGrantedAndLocationEnabled = false;

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionUtils.neverAskAgainSelected((Activity) ObjectTextCurved.this)) {
                PermissionUtils.displayNeverAskAgainDialog(ObjectTextCurved.this);
            } else {
                permissionGrantedAndLocationEnabled =
                        PermissionUtils.requestPermission((Activity) ObjectTextCurved.this, 1001);
            }
        }
    }

}