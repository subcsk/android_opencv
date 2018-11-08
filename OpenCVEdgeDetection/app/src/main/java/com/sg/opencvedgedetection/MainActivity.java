package com.sg.opencvedgedetection;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.sg.utils.FileUtils;

import org.opencv.android.Utils;
import org.opencv.core.Mat;


public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("native-lib");
    }

    Button imageLoadBtn, imageProcessBtn;
    ImageView imageViewInput, imageViewOutput;
    Mat img_input, img_output;
    Uri albumURI;

    private static final String TAG = "OPEN_CV";

    private final int PERMISSION_REQUEST_CODE = 1;
    private final int REQ_CODE_SELECT_IMAGE = 100;

    String[] PERMISSIONS = {"android.permission.WRITE_EXTERNAL_STORAGE"};


    private boolean hasPermissions(String[] permissions) {
        int ret = 0;

        for (String perms : permissions) {
            ret = checkCallingOrSelfPermission(perms);
            if (!(ret == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }

        return true;
    }

    private void requestNecessaryPermissions(String[] permissions) {
        // 마시멜로(API 23) 이상에서 런타임 퍼미션(Runtime Permission) 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
         switch (permsRequestCode) {
            case PERMISSION_REQUEST_CODE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                       Log.d(TAG, "Permission was granted.");

                    } else {
                        showDialogforPermission("앱을 실행하려면 퍼미션을 허가하셔야 합니다.");
                    }
                }
                break;
        }
    }

    private void showDialogforPermission(String msg) {
        final AlertDialog.Builder myDialog = new AlertDialog.Builder(MainActivity.this);
        myDialog.setTitle("알림");
        myDialog.setMessage(msg);
        myDialog.setCancelable(false);
        myDialog.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE);
                }
            }
        });
        myDialog.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        myDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageLoadBtn = (Button)findViewById(R.id.imageLoadBtn);
        imageProcessBtn = (Button)findViewById(R.id.imageProcBtn);

        imageViewInput = (ImageView)findViewById(R.id.imageViewInput);
        imageViewOutput = (ImageView)findViewById(R.id.imageViewOutput);

        imageLoadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadImageFromGallery(v);

            }
        });

        imageProcessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean readOK = read_image_file();
                if (readOK) imageprocessing_and_showResult();
            }
        });

        // check permission
        if (!hasPermissions(PERMISSIONS)) {
            requestNecessaryPermissions(PERMISSIONS);
        }
    }

    public void loadImageFromGallery(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQ_CODE_SELECT_IMAGE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (requestCode == REQ_CODE_SELECT_IMAGE && resultCode == RESULT_OK && null != data) {
                Uri uri = data.getData();
                Log.d(TAG, "FILE_INFO :: " + data.getData());

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);

                int nh = (int) (bitmap.getHeight() * (1024.0 / bitmap.getWidth()));
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 1024, nh, true);

                imageViewInput.setImageBitmap(scaled);
                albumURI = uri;


            } else {
                Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Oops! 로딩에 오류가 있습니다.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void imageprocessing_and_showResult() {
        imageprocessing(img_input.getNativeObjAddr(), img_output.getNativeObjAddr());

        Bitmap bitmapInput = Bitmap.createBitmap(img_input.cols(), img_input.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img_input, bitmapInput);
        imageViewInput.setImageBitmap(bitmapInput);

        Bitmap bitmapOutput = Bitmap.createBitmap(img_output.cols(), img_output.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img_output, bitmapOutput);
        imageViewOutput.setImageBitmap(bitmapOutput);
    }

    private boolean read_image_file() {

        if (albumURI == null) {
            return false;
        }

        String path = FileUtils.getPath(getBaseContext(), albumURI);

        img_input = new Mat();
        img_output = new Mat();

        loadImage(path, img_input.getNativeObjAddr());

        return true;
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native void loadImage(String imageFileName, long img);
    public native void imageprocessing(long inputImage, long outputImage);
}
