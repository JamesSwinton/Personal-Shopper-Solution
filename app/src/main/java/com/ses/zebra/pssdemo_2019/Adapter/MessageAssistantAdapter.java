package com.ses.zebra.pssdemo_2019.Adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ses.zebra.pssdemo_2019.Debugging.Logger;
import com.ses.zebra.pssdemo_2019.POJOs.Assistance.HelpMessage;
import com.ses.zebra.pssdemo_2019.R;

import java.util.List;

public class MessageAssistantAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Debugging
    private static final String TAG = "BasketListAdapter";

    // Constants
    private static final int NO_MESSAGE_VIEW_TYPE = 0;
    private static final int SENT_MESSAGE_VIEW_TYPE = 1;
    private static final int RECEIVED_MESSAGE_VIEW_TYPE = 2;
    private static final int SYSTEM_MESSAGE_VIEW_TYPE = 3;

    // Variables
    private List<HelpMessage> mMessageList;

    public MessageAssistantAdapter(List<HelpMessage> messageList) {
        this.mMessageList = messageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case NO_MESSAGE_VIEW_TYPE:
                return new NoMessageHolder(LayoutInflater.from(
                        parent.getContext()).inflate(R.layout.adapter_chat_no_message, parent, false));
            case SENT_MESSAGE_VIEW_TYPE:
                return new SentMessageHolder(LayoutInflater.from(
                        parent.getContext()).inflate(R.layout.adapter_chat_message_sent, parent, false));
            case RECEIVED_MESSAGE_VIEW_TYPE:
                return new ReceivedMessageHolder(LayoutInflater.from(
                        parent.getContext()).inflate(R.layout.adapter_chat_message_received, parent, false));
            case SYSTEM_MESSAGE_VIEW_TYPE:
                return new SystemMessageHolder(LayoutInflater.from(
                        parent.getContext()).inflate(R.layout.adapter_chat_system_message, parent, false));
            default:
                return new NoMessageHolder(null);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        switch(viewHolder.getItemViewType()) {
            case NO_MESSAGE_VIEW_TYPE:
                Logger.i(TAG, "Showing No Message ViewHolder");
                break;
            case SENT_MESSAGE_VIEW_TYPE:
                Logger.i(TAG, "Showing Sent Message ViewHolder");
                // Cast ViewHolder to SentMessageViewHolder
                SentMessageHolder sentViewHolder = (SentMessageHolder) viewHolder;
                // Update Values
                sentViewHolder.mMessageText.setText(mMessageList.get(position).getMessage());
                sentViewHolder.mMessageTime.setText(mMessageList.get(position).getSentTime());
                break;
            case RECEIVED_MESSAGE_VIEW_TYPE:
                Logger.i(TAG, "Showing Received Message ViewHolder");
                // Cast ViewHolder to SentMessageViewHolder
                ReceivedMessageHolder receivedMessageHolder = (ReceivedMessageHolder) viewHolder;
                // Update Values
                receivedMessageHolder.mMessageText.setText(mMessageList.get(position).getMessage());
                receivedMessageHolder.mMessageTime.setText(mMessageList.get(position).getSentTime());
                break;
            case SYSTEM_MESSAGE_VIEW_TYPE:
                Logger.i(TAG, "Showing System Message ViewHolder");
                // Cast ViewHolder to SentMessageViewHolder
                SystemMessageHolder systemMessageHolder = (SystemMessageHolder) viewHolder;
                // Update Values
                systemMessageHolder.mSystemMessage.setText(mMessageList.get(position).getMessage());
                break;
        }
    }

    @Override
    public int getItemCount() {
        // Only 1 item = empty list = show empty view holder
        return mMessageList.isEmpty() ? 1 : mMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {

        if (mMessageList.size() == 0) {
            return NO_MESSAGE_VIEW_TYPE;
        }

        if (mMessageList.get(position).getSender() == HelpMessage.MessageType.SENT) {
            return SENT_MESSAGE_VIEW_TYPE;
        }

        if (mMessageList.get(position).getSender() == HelpMessage.MessageType.RECEIVED) {
            return RECEIVED_MESSAGE_VIEW_TYPE;
        }

        if (mMessageList.get(position).getSender() == HelpMessage.MessageType.SYSTEM) {
            return SYSTEM_MESSAGE_VIEW_TYPE;
        }

        return NO_MESSAGE_VIEW_TYPE;
    }

    public void updateMessageList(List<HelpMessage> messageList) {
        this.mMessageList = messageList;
        notifyDataSetChanged();
    }

    public static class NoMessageHolder extends RecyclerView.ViewHolder {
        NoMessageHolder(View view) {
            super(view);
        }
    }

    public static class SentMessageHolder extends RecyclerView.ViewHolder {

        // List Elements
        TextView mMessageText;
        TextView mMessageTime;

        SentMessageHolder(View view) {
            super(view);
            mMessageText = view.findViewById(R.id.messageText);
            mMessageTime = view.findViewById(R.id.messageSentTime);
        }
    }

    public static class ReceivedMessageHolder extends RecyclerView.ViewHolder {

        // List Elements
        TextView mMessageText;
        TextView mMessageTime;

        ReceivedMessageHolder(View view) {
            super(view);
            mMessageText = view.findViewById(R.id.messageText);
            mMessageTime = view.findViewById(R.id.messageSentTime);
        }
    }

    public static class SystemMessageHolder extends RecyclerView.ViewHolder {

        TextView mSystemMessage;

        SystemMessageHolder(View view) {
            super(view);
            mSystemMessage = view.findViewById(R.id.systemMessage);
        }
    }
}
