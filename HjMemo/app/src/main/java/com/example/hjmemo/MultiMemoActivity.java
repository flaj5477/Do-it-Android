package com.example.hjmemo;

import android.content.Intent;
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
                BasicInfo.FOLDER_VIDEO = BasicInfo.ExternalPath + BasicInfo.FOLDER_VIDEO;
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
            }
        });

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
        String SQL = "select _id, INPUT_DATE, CONTENT_TEXT, ID_PHOTO, ID_VIDEO, ID_VOICE, ID_HANDWRITING from MEMO order by INPUT_DATE desc";  //DB데이터 참조 SQL문

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
                String voiceUriStr = null;

                String handwritingId = outCursor.getString(6);  //손그림 _id
                String handwritingUriStr = null;

                mMemoListAdapter.addItem(new MemoListItem(memoId, dateStr, memoStr, handwritingId, handwritingUriStr, photoId, photoUriStr, voiceId, voiceUriStr));
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

    //메모 보여주기
    private void viewMemo(int position) {
    }
}
