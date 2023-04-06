package com.aman.talkmate;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MessageAdaptor extends RecyclerView.Adapter {

    private SharedPreferences sharedPreferences;
    ArrayList<messageModelClass> dataHolder;
    String commonEncryptionKey, commonEncryptionIv;
    Context context;
    Activity activity;

    final int SENDER_VIEW_TYPE = 1;
    final int RECEIVER_VIEW_TYPE = -1;
    private View view;
    int count = 0;
    private boolean isAnimating;

    public MessageAdaptor() {
    }

    public MessageAdaptor(ArrayList<messageModelClass> dataHolder, Context context, Activity activity) {
        this.dataHolder = dataHolder;
        this.context = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == SENDER_VIEW_TYPE) {
            view = LayoutInflater.from(context).inflate(R.layout.layout_sender_chat_bubble, parent, false);
            return new SenderViewHolder(view);
        }
        view = LayoutInflater.from(context).inflate(R.layout.layout_receiver_chat_bubble, parent, false);
        return new ReceiverViewHolder(view);

    }

    @Override
    public int getItemViewType(int position) {
        // sender type
        if (dataHolder.get(position).getRole().equals(messageModelClass.SENT_BY_ME)) {
            return SENDER_VIEW_TYPE;
        } else { // receiver type
            return RECEIVER_VIEW_TYPE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder.getClass() == SenderViewHolder.class) {
            ((SenderViewHolder) holder).tvSenderMessage.setText(dataHolder.get(position).getContent());

        } else {
            ((ReceiverViewHolder) holder).tvReceiverMessage.setText(dataHolder.get(position).getContent());
        }
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (holder.getClass() == SenderViewHolder.class) {
                    ((SenderViewHolder) holder).OptionMenuCall(view);

                } else {
                    ((ReceiverViewHolder) holder).OptionMenuCall(view);
                }

                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataHolder.size();
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {

        TextView tvReceiverMessage;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReceiverMessage = itemView.findViewById(R.id.textView_receiver);
        }
        public void OptionMenuCall(View view){
            openOptionMenu(view);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {

        TextView tvSenderMessage;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderMessage = itemView.findViewById(R.id.textView_sender);
        }
        public void OptionMenuCall(View view){
            openOptionMenu(view);
        }
    }

    void openOptionMenu(View view){}


}