package com.wifidirect.milan.wifidirect.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wifidirect.milan.wifidirect.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by milan on 26.11.15..
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>  {
    private List<String> mMessages;
    private Context mContext;
    private boolean isMyMessage;
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("ccc 'at' H:mm a");
    private Calendar mCalendar = Calendar.getInstance();

    public ChatAdapter(Context context) {
        this.mMessages = new ArrayList<>();
        this.mContext = context;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextViewMessage;
        public TextView mTextViewTime;
        public RelativeLayout mRelativeLayoutMessageBody;
        public ViewHolder(View v) {
            super(v);
            mTextViewMessage = (TextView)v.findViewById(R.id.textviewmessage);
            mRelativeLayoutMessageBody = (RelativeLayout)v.findViewById(R.id.relativemessagebody);
            mTextViewTime = (TextView)v.findViewById(R.id.textviewtime);
        }
    }


    public void addMessage(String message, boolean status){
        mMessages.add(message);
        this.isMyMessage = status;
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
        holder.mTextViewTime.setText(String.valueOf(mSimpleDateFormat.format(mCalendar.getTime())));

        if(isMyMessage) {
            holder.mRelativeLayoutMessageBody.setBackgroundColor(mContext.getResources().getColor(R.color.colorPurple700));
        } else {
            holder.mRelativeLayoutMessageBody.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));
        }

    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }


}
