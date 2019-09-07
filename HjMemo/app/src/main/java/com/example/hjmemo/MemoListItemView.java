package com.example.hjmemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

class MemoListItemView extends LinearLayout {
    private TextView itemTitle;

    private ImageView itemVoiceState;

    private TextView itemDate;

    private ImageView itemImageView;

    Bitmap bitmap;

    public MemoListItemView(Context context) {
        super(context);

        //레이아웃을 메모리에 객체화
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.memo_listitem, this, true);

        //레이아웃의 객체 참조
        itemTitle = (TextView) findViewById(R.id.itemTitle);
        itemVoiceState = (ImageView) findViewById(R.id.itemVoiceState);
        itemDate = (TextView) findViewById(R.id.itemDate);
        itemImageView = (ImageView) findViewById(R.id.itemImageView);

    }

    public void setContents(int index, String data) {       //메모리스트아이템뷰 내용 지정
        if (index == 0) {
            itemDate.setText(data);     //날짜 설정
        } else if (index == 1) {
            itemTitle.setText(data);     //텍스트메모 설정
        }  else if (index == 4) {        //MemoListItem.id_photo
            if (data == null || data.equals("-1") || data.equals("")) {
                itemImageView.setImageResource(R.drawable.person_add);
            } else {
                    itemImageView.setImageResource(R.drawable.person_add_en);
            }

        } else {
            throw new IllegalArgumentException();
        }
    }

    public void setMediaState(String sVoiceUri) {     //녹음 상태에 따라 아이콘 설정
        if(sVoiceUri == null || sVoiceUri.trim().length() < 1 || sVoiceUri.equals("-1")) {
            itemVoiceState.setImageResource(R.drawable.icon_voice_empty);
        } else {
            itemVoiceState.setImageResource(R.drawable.icon_voice);
        }
    }


}
