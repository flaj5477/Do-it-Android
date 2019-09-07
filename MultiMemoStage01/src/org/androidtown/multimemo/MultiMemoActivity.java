package org.androidtown.multimemo;

import org.androidtown.multimemo.common.TitleBitmapButton;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MultiMemoActivity extends Activity {

	public static final String TAG = "MultiMemoActivity";

	/**
	 * �޸� ����Ʈ��
	 */
	ListView mMemoListView;

	/**
	 * �޸� ����Ʈ �����
	 */
	MemoListAdapter mMemoListAdapter;

	/**
	 * �޸� ����
	 */
	int mMemoCount = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        // �޸� ����Ʈ
        mMemoListView = (ListView)findViewById(R.id.memoList);
    	mMemoListAdapter = new MemoListAdapter(this);
    	mMemoListView.setAdapter(mMemoListAdapter);
    	mMemoListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				viewMemo(position);
			}
		});


        // �� �޸� ��ư ����
        TitleBitmapButton newMemoBtn = (TitleBitmapButton)findViewById(R.id.newMemoBtn);
    	newMemoBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "newMemoBtn clicked.");

			}
		});

    	// �ݱ� ��ư ����
        TitleBitmapButton closeBtn = (TitleBitmapButton)findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

        loadMemoListData();
    }

    private void loadMemoListData() {
    	MemoListItem aItem = new MemoListItem("1", "2013-06-10 10:20", "������ ���� ��!",
    			null, null,
    			null, null,
    			null, null,
    			null, null);

    	mMemoListAdapter.addItem(aItem);
    	mMemoListAdapter.notifyDataSetChanged();
    }

    private void viewMemo(int position) {

    }
}