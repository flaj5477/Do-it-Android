package com.example.hjmemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

public class MultiMemoActivity extends AppCompatActivity {

    public static final String TAG = "MultiMemoActivity";   //태그 위치

    //메모 리스트뷰
    ListView mMemoListView;

    //메모 리스트 어댑터
    MemoListAdapter mMemoListAdapter;

    //메모 데이터 베이스 객체
    public static MemoDatabase mDatabase = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {  //메인 액티비티 생성시
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SD Card checking
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "SD 카드가 존재하지 않습니다. SD카드를 입력해주세요.", Toast.LENGTH_LONG).show();
            return;
        } else {
            String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            if (!BasicInfo.ExternalChecked && externalPath != null) {
                BasicInfo.ExternalPath = externalPath + File.separator;
                Log.d(TAG, "ExternalPath : " + BasicInfo.ExternalPath);

                BasicInfo.FOLDER_PHOTO = BasicInfo.ExternalPath + BasicInfo.FOLDER_PHOTO;
                BasicInfo.FOLDER_VOICE = BasicInfo.ExternalPath + BasicInfo.FOLDER_VOICE;
                BasicInfo.FOLDER_HANDWRITING = BasicInfo.ExternalPath + BasicInfo.FOLDER_HANDWRITING;
                BasicInfo.DATABASE_NAME = BasicInfo.ExternalPath + BasicInfo.DATABASE_NAME;

                BasicInfo.ExternalChecked = true;
            }
        }

        // 메모 리스트
        mMemoListView = (ListView)findViewById(R.id.memoList);
        mMemoListAdapter = new MemoListAdapter(this);
        mMemoListView.setAdapter(mMemoListAdapter);
        mMemoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {        //메모리스트 클릭하면 viewMemo()함수로 이동!
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {  //리스트아이템 클릭하면 메모보여주는 함수 실행
                viewMemo(position);
            }
        });

        // 새 메모 버튼 설정
        ImageButton newMemoBtn = (ImageButton)findViewById(R.id.newMemoBtn);
        newMemoBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "newMemoBtn clicked.");
                //메모 입력하는 페이지로 전환하도록!!
                Intent intent = new Intent(getApplicationContext(), MemoInsertActivity.class);
                intent.putExtra(BasicInfo.KEY_MEMO_MODE, BasicInfo.MODE_INSERT);    //새 메모모드 전달!
                startActivityForResult(intent, BasicInfo.REQ_INSERT_ACTIVITY);  //액티비티 요청코드: 1002
            }
        });

        checkDangerousPermissions();        //권한 허가 체크 함수
    }

    //권한 허가 체크 함수
    private void checkDangerousPermissions() {
        String[] permissions = {
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.WAKE_LOCK
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ActivityCompat.checkSelfPermission(this, permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "권한 있음", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "권한 없음", Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                Toast.makeText(this, "권한 설명 필요함.", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, permissions[i] + " 권한이 승인됨.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, permissions[i] + " 권한이 승인되지 않음.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //시작할때 실행
    protected void onStart() {
       //DB오픈
        openDatabase();

        //DB가져오기
        loadMemoListData();

        super.onStart();
    }

    //DB오픈 함수
    public void openDatabase() {
        // open database
        if (mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }

        mDatabase = MemoDatabase.getInstance(this);
        boolean isOpen = mDatabase.open();
        if (isOpen) {
            Log.d(TAG, "Memo database is open.");
        } else {
            Log.d(TAG, "Memo database is not open.");
        }
    }

    //메모리스트 데이터를 생성하는 메소드
    public int loadMemoListData() {
        String SQL = "select _id, INPUT_DATE, TITLE_TEXT, CONTENT_TEXT, ID_PHOTO, ID_VOICE, ID_HANDWRITING from MEMO order by INPUT_DATE desc";  //DB데이터 참조 SQL문

        int recordCount = -1;
        if (MultiMemoActivity.mDatabase != null) {
            Cursor outCursor = MultiMemoActivity.mDatabase.rawQuery(SQL);   //SQL실행

            recordCount = outCursor.getCount();
            Log.d(TAG, "cursor count : " + recordCount + "\n");

            mMemoListAdapter.clear();
            Resources res = getResources();

            for (int i = 0; i < recordCount; i++) { //DB select문 결과 갯수 만큼
                outCursor.moveToNext();

                String memoId = outCursor.getString(0); //메모아이디

                String dateStr = outCursor.getString(1);    //메모작성날짜
                if (dateStr.length() > 10) {
                    dateStr = dateStr.substring(0, 10);
                }

                String titleStr = outCursor.getString(2);   //메모제목
                String memoStr = outCursor.getString(3);    //메모텍스트 내용
                String photoId = outCursor.getString(4);    //메모사진 _id
                String photoUriStr = getPhotoUriStr(photoId);

                String voiceId = outCursor.getString(5);    //메모음성파일 _id
                String voiceUriStr = getVoiceUriStr(voiceId);

                String handwritingId = outCursor.getString(6);  //손그림 _id
                String handwritingUriStr = getHandwritingUriStr(handwritingId);

                mMemoListAdapter.addItem(new MemoListItem(memoId, dateStr, titleStr, memoStr, handwritingId, handwritingUriStr, photoId, photoUriStr, voiceId, voiceUriStr));
                Log.d( TAG, "제목: " + titleStr + "사진 URI : " + photoUriStr);
            }

            outCursor.close();

            mMemoListAdapter.notifyDataSetChanged();
        }

        return recordCount;
    }

    //DB에서 사진 id로 URI가져오는 함수
    public String getPhotoUriStr(String id_photo) {
        String photoUriStr = null;
        if (id_photo != null && !id_photo.equals("-1")) {
            String SQL = "select URI from " + MemoDatabase.TABLE_PHOTO + " where _ID = " + id_photo + "";
            Cursor photoCursor = MultiMemoActivity.mDatabase.rawQuery(SQL);
            if (photoCursor.moveToNext()) {
                photoUriStr = photoCursor.getString(0);
            }
            photoCursor.close();
        } else if(id_photo == null || id_photo.equals("-1")) {
            photoUriStr = "";
        }
        return photoUriStr;
    }

    /**
     * 손글씨 데이터 URI 가져오기
     */
    public String getHandwritingUriStr(String id_handwriting) {
        Log.d(TAG, "Handwriting ID : " + id_handwriting);

        String handwritingUriStr = null;
        if (id_handwriting != null && id_handwriting.trim().length() > 0 && !id_handwriting.equals("-1")) {
            String SQL = "select URI from " + MemoDatabase.TABLE_HANDWRITING + " where _ID = " + id_handwriting + "";
            Cursor handwritingCursor = MultiMemoActivity.mDatabase.rawQuery(SQL);
            if (handwritingCursor.moveToNext()) {
                handwritingUriStr = handwritingCursor.getString(0);
            }
            handwritingCursor.close();
        } else {
            handwritingUriStr = "";
        }

        return handwritingUriStr;
    }

    /**
     * 녹음 데이터 URI 가져오기
     */
    public String getVoiceUriStr(String id_voice) {
        Log.d(TAG, "Voice ID : " + id_voice);

        String voiceUriStr = null;
        if (id_voice != null && id_voice.trim().length() > 0 && !id_voice.equals("-1")) {
            String SQL = "select URI from " + MemoDatabase.TABLE_VOICE + " where _ID = " + id_voice + "";
            Cursor voiceCursor = MultiMemoActivity.mDatabase.rawQuery(SQL);
            if (voiceCursor.moveToNext()) {
                voiceUriStr = voiceCursor.getString(0);
            }
            voiceCursor.close();
        } else {
            voiceUriStr = "";
        }

        return voiceUriStr;
    }



    //메모 보여주기
    private void viewMemo(int position) {
        MemoListItem item = (MemoListItem)mMemoListAdapter.getItem(position);

        //새로운 인텐트 생성!
        Intent intent = new Intent(getApplicationContext(), MemoInsertActivity.class);
        intent.putExtra(BasicInfo.KEY_MEMO_MODE, BasicInfo.MODE_VIEW);
        intent.putExtra(BasicInfo.KEY_MEMO_ID, item.getId());
        intent.putExtra(BasicInfo.KEY_MEMO_DATE, item.getData(0));
        intent.putExtra(BasicInfo.KEY_MEMO_TITLE, item.getData(1));
        intent.putExtra(BasicInfo.KEY_MEMO_TEXT, item.getData(2));

        intent.putExtra(BasicInfo.KEY_ID_HANDWRITING, item.getData(3));
        intent.putExtra(BasicInfo.KEY_URI_HANDWRITING, item.getData(4));

        intent.putExtra(BasicInfo.KEY_ID_PHOTO, item.getData(5));
        intent.putExtra(BasicInfo.KEY_URI_PHOTO, item.getData(6));

        intent.putExtra(BasicInfo.KEY_ID_VOICE, item.getData(7));
        intent.putExtra(BasicInfo.KEY_URI_VOICE, item.getData(8));

        startActivityForResult(intent, BasicInfo.REQ_VIEW_ACTIVITY);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case BasicInfo.REQ_INSERT_ACTIVITY:
                if(resultCode == RESULT_OK) {
                    loadMemoListData();
                }
                break;

            case BasicInfo.REQ_VIEW_ACTIVITY:
                loadMemoListData();
                break;

        }
    }
}
