package com.wifidirect.milan.wifidirect.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wifidirect.milan.wifidirect.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by milan on 26.11.15..
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>  {
    private List<String> mMessages;


    public ChatAdapter() {
        this.mMessages = new ArrayList<>();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextViewMessage;

        public ViewHolder(View v) {
            super(v);
            mTextViewMessage = (TextView)v.findViewById(R.id.textviewmessage);
        }
    }


    public void addMessage(String message){
        mMessages.add(message);
        //notifyDataSetChanged();
        notifyItemChanged(mMessages.size());
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_message, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mTextViewMessage.setText(mMessages.get(position));

    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }


}
