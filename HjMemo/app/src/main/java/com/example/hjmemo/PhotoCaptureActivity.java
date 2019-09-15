package com.example.hjmemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileOutputStream;

public class PhotoCaptureActivity extends Activity {


    public static final String TAG = "PhotoCaptureActivity";

    CameraSurfaceView mCameraView;

    FrameLayout mFrameLayout;

    /**
     *
     */
    boolean processing = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ���¹ٿ� Ÿ��Ʋ ����
        final Window win = getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.photo_capture_activity);

        mCameraView = new CameraSurfaceView(getApplicationContext());
        mFrameLayout = (FrameLayout) findViewById(R.id.frame);
        mFrameLayout.addView(mCameraView);

        setCaptureBtn();

    }

    public void setCaptureBtn() {
        ImageButton takeBtn = (ImageButton) findViewById(R.id.capture_takeBtn);
        takeBtn.setImageResource(R.drawable.icon_capture);
        takeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!processing) {
                    processing = true;
                    mCameraView.capture(new CameraPictureCallback());
                }
            }
        });
    }

    /**
     *
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            mCameraView.capture(new CameraPictureCallback());

            return true;
        } else if(keyCode == KeyEvent.KEYCODE_BACK) {
            finish();

            return true;
        }

        return false;
    }


    class CameraPictureCallback implements Camera.PictureCallback {

        public void onPictureTaken(byte[] data, Camera camera) {
            Log.v(TAG, "onPictureTaken() called.");

            int bitmapWidth = 480;
            int bitmapHeight = 360;

            Bitmap capturedBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(capturedBitmap, bitmapWidth, bitmapHeight, false);

            Bitmap resultBitmap = null;

            Matrix matrix = new Matrix();
            matrix.postRotate(0);

            resultBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);

            try {
                File photoFolder = new File(BasicInfo.FOLDER_PHOTO);

                //������ ���ٸ� ������ �����Ѵ�.
                if(!photoFolder.isDirectory()){
                    Log.d(TAG, "creating photo folder : " + photoFolder);
                    photoFolder.mkdirs();
                }

                String photoName = "captured";

                // ���� �̹����� ������ ����
                File file = new File(BasicInfo.FOLDER_PHOTO + photoName);
                if(file.exists()) {
                    file.delete();
                }

                FileOutputStream outstream = new FileOutputStream(BasicInfo.FOLDER_PHOTO + photoName);
                resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, outstream);
                outstream.close();

            } catch (Exception ex) {
                Log.e(TAG, "Error in writing captured image.", ex);
                showDialog(BasicInfo.IMAGE_CANNOT_BE_STORED);
            }

            showParentActivity();
        }
    }



    /**
     * �θ� ��Ƽ��Ƽ�� ���ư���
     */
    private void showParentActivity() {
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);

        finish();
    }


    protected Dialog onCreateDialog(int id) {
        Log.d(TAG, "onCreateDialog() called");

        switch (id) {
            case BasicInfo.IMAGE_CANNOT_BE_STORED:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("������ ������ �� �����ϴ�. SDī�� ���¸� Ȯ���ϼ���.");
                builder.setPositiveButton("Ȯ��",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                return builder.create();
        }

        return null;
    }
}
