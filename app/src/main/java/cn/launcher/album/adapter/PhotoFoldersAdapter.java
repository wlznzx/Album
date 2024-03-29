package cn.launcher.album.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;


import cn.launcher.album.bean.AlbumEntry;
import cn.launcher.album.callback.OnEditItemClickListener;
import cn.launcher.album.holder.PhotoFolderItemHolder;

import java.util.List;

/**
 * Created by jiaojie.jia on 2017/3/23.
 */

public class PhotoFoldersAdapter extends RecyclerView.Adapter {

    private List<AlbumEntry> mFolderList;

    private OnEditItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnEditItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setData(List<AlbumEntry> folders) {
        mFolderList = folders;
        notifyDataSetChanged();
    }

    public AlbumEntry getItem(int position) {
        return mFolderList == null ? null : mFolderList.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PhotoFolderItemHolder(parent.getContext(), onItemClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PhotoFolderItemHolder itemHolder = (PhotoFolderItemHolder) holder;
        itemHolder.setData(mFolderList.get(position));
    }

    @Override
    public int getItemCount() {
        return mFolderList == null ? 0 : mFolderList.size();
    }
}
