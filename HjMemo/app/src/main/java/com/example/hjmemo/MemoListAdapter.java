package com.example.hjmemo;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

class MemoListAdapter extends BaseAdapter {

    private Context mContext;
    private List<MemoListItem> mItems = new ArrayList<MemoListItem>();

    public MemoListAdapter(Context context) {
        mContext = context;
    }   //생성자

    public void clear() {
        mItems.clear();
    }

    public void addItem(MemoListItem it) {
        mItems.add(it);
    }

    public void setListItems(List<MemoListItem> lit) {
        mItems = lit;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        MemoListItemView itemView;
        if (convertView == null) {
            itemView = new MemoListItemView(mContext);
        } else {
            itemView = (MemoListItemView) convertView;
        }

        // set current item data  (MemoListItemView 객체에 MemoListItem의 아이템 데이터를 설정 해 주는 부분)
        itemView.setContents(0, ((String) mItems.get(position).getData(0)));    //날짜
        itemView.setContents(1, ((String) mItems.get(position).getData(1)));    //메모제목
        itemView.setContents(2, ((String) mItems.get(position).getData(5)));    //이미지

        itemView.setMediaState( (String)mItems.get(position).getData(8));           //녹음파일있는지 없는지

        return itemView;
    }

    @Override
    public int getCount() {
        return  mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    public boolean areAllItemsSelectable() {
        return false;
    }

    public boolean isSelectable(int position) {
        try {
            return mItems.get(position).isSelectable();
        } catch (IndexOutOfBoundsException ex) {
            return false;
        }
    }
    @Override
    public long getItemId(int position) {
        return position;
    }


}
