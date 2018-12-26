package com.cts.product.mob.android;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cts.product.mob.R;
import com.cts.product.mob.adapter.ChatMessage;
import com.cts.product.mob.adapter.ConversationChatAdapter;

import java.util.ArrayList;
import java.util.List;

public class FragmentVoiceChat extends Fragment {
    public static final String TAG = FragmentVoiceChat.class.getName();

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private RecyclerView mRecyclerView;
    private ConversationChatAdapter mChatAdapter;
    private List<ChatMessage> mChatMessages = new ArrayList<>();

    public FragmentVoiceChat() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentVoiceChat.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentVoiceChat newInstance(String param1, String param2) {
        FragmentVoiceChat fragment = new FragmentVoiceChat();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_voice_chat, container, false);

        mRecyclerView = view.findViewById(R.id.recycler_chat_window);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        mRecyclerView.setAdapter(mChatAdapter);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mChatAdapter = new ConversationChatAdapter(mChatMessages);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void addMessage(ChatMessage chatMessage) {
        mChatMessages.add(chatMessage);
        mChatAdapter.notifyItemChanged(mChatMessages.size() - 1);
        mRecyclerView.scrollToPosition(mChatAdapter.getItemCount() - 1);
    }

    public void clearMessages() {
        mChatMessages.clear();
        mChatAdapter.notifyDataSetChanged();
    }

    public void updateLastMessage(String text) {
        if (mChatMessages.size() > 0) {
            int lastNum = mChatMessages.size() - 1;
            ChatMessage cm = mChatMessages.get(lastNum);
            if (cm.getDirection() == ChatMessage.ChatDirection.Sent) {
                mChatMessages.remove(lastNum);
            }
            addMessage(new ChatMessage(text, cm.getDirection()));
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) { //TODO
        super.onViewCreated(view, savedInstanceState);
//        addMessage(new ChatMessage("Test message 1", ChatMessage.ChatDirection.Sent));
//        addMessage(new ChatMessage("Test message 2", ChatMessage.ChatDirection.Sent));
//        addMessage(new ChatMessage("Test message 3", ChatMessage.ChatDirection.Received));
//        addMessage(new ChatMessage("Test message 4", ChatMessage.ChatDirection.Received));
//        addMessage(new ChatMessage("Test message 5", ChatMessage.ChatDirection.Sent));
//        addMessage(new ChatMessage("Test message 6", ChatMessage.ChatDirection.Received));
//        addMessage(new ChatMessage("Test message 7", ChatMessage.ChatDirection.Sent));
//        addMessage(new ChatMessage("Test message 8", ChatMessage.ChatDirection.Received));
    }
}
