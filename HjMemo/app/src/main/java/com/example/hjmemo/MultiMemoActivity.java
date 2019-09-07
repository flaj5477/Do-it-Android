package com.example.hjmemo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MultiMemoActivity extends AppCompatActivity {

    public static final String TAG = "MultiMemoActivity";   //태그 위치

    //메모 리스트뷰
    ListView mMemoListView;

    //메모 리스트 어댑터
    MemoListAdapter mMemoListAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {  //메인 액티비티 생성시
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 메모 리스트
        mMemoListView = (ListView)findViewById(R.id.memoList);
        mMemoListAdapter = new MemoListAdapter(this);
        mMemoListView.setAdapter(mMemoListAdapter);
        mMemoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {        //메모리스트 클릭하면 viewMemo()함수로 이동!
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                viewMemo(position);
            }
        });

        // 새 메모 버튼 설정
        ImageButton newMemoBtn = (ImageButton)findViewById(R.id.newMemoBtn);
        newMemoBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "newMemoBtn clicked.");
            }
        });

        loadMemoListData();        //테스트로 가상의 메모 데이터를 생성하는 메소드
    }

    private void loadMemoListData() {
        MemoListItem item1 = new MemoListItem("1","2019-09-08","이거 되면 잔다 하하", null, null, null, null, "voice1", null );
        mMemoListAdapter.addItem(item1);            //어댑터에 메모아이템 객체 추가
        mMemoListAdapter.notifyDataSetChanged();    //리스트뷰 갱신
    }


    //메모 보여주기
    private void viewMemo(int position) {
    }
}
