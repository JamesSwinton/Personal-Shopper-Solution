package com.ses.zebra.pssdemo_2019.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ses.zebra.pssdemo_2019.R;
import com.slacorp.eptt.android.service.CoreCall;
import com.slacorp.eptt.core.common.Participant;

/**
 * Shows List of Participants in Call
 */
public class InCallFragment extends Fragment {

    // Debugging
    private static final String TAG = "InCallFragment";

    // Variables
    private CoreCall mCoreCall;
    private ListView mParticipantListView;
    private InCallAdapter mInCallAdapter = new InCallAdapter();

    /**
     * Fragment Initialisation Methods
     * onCreateView()           -> Returns Layout for Fragment
     * onStart()                -> Initialises ListView & Adapter. Assigns Adapter to ListView
     * onStop()                 -> Resets mCoreCall variable
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView() | CoreCall: " + mCoreCall);

        // Return View
        return inflater.inflate(R.layout.fragment_in_call, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart() | CoreCall: " + mCoreCall);

        // Init ListView
        mParticipantListView = getActivity().findViewById(R.id.in_call_list_view);
        mParticipantListView.setAdapter(mInCallAdapter);
    }

    @Override
    public void onStop() {
        Log.i(TAG, "onStop()");

        // Activity will pass another instance when re-started
        mCoreCall = null;

        // Allow super class to handle as usual
        super.onStop();
    }

    /**
     * Call Utility Methods
     * setCall()    -> Assigns Call Object
     *              -> Notifies Adapter
     * update()     -> Notifies Adapter
     */

    public void setCall(CoreCall call) {
        Log.i(TAG, "Setting Call Object: " + call);

        // Assign CoreCall Object to Member Variable -> Notify Adapter
        mCoreCall = call;
        mInCallAdapter.notifyDataSetChanged();
    }

    public void update() {
        mInCallAdapter.notifyDataSetChanged();
    }

    /**
     * Simple ListView Adapter to display Participants in-call
     */

    private class InCallAdapter extends BaseAdapter {

        // Get Number of Participants in Call
        @Override
        public int getCount() {
            return mCoreCall == null ? 0 : mCoreCall.getParticipants().length;
        }

        // Get Current Participant
        @Override
        public Participant getItem(int position) {
            return mCoreCall == null ? null : mCoreCall.getParticipants()[position];
        }

        // Get ID (HashCode) of Participant
        @Override
        public long getItemId(int position) {
            return mCoreCall == null ? 0 : mCoreCall.getParticipants()[position].hashCode();
        }

        // Get State -> Update Participant name & status appropriately
        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            // Inflate View
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.adapter_contact_list, container, false);
            }

            // Get UI Elements
            TextView participantName = convertView.findViewById(R.id.contactName);
            ImageView participantStatus = convertView.findViewById(R.id.contactStatusIcon);

            // Get current Participant Object
            Participant participant = getItem(position);

            // Set Participant Name
            participantName.setText(participant.name);

            // Set Participant Status Icon
            switch (participant.state) {
                case Participant.State.JOINED:
                    participantStatus.setImageResource(R.drawable.contact_status_green);
                    break;
                case Participant.State.UNAVAILABLE:
                case Participant.State.WAITING_FOR_ANSWER:
                case Participant.State.LEFT:
                case Participant.State.SERVER_RESOURCES:
                case Participant.State.SIGNED_OUT:
                case Participant.State.NOT_RESPONDING:
                case Participant.State.BUSY:
                case Participant.State.DND:
                case Participant.State.SILENT:
                case Participant.State.CALL_DROPPED:
                case Participant.State.CIRCUIT_CALL:
                case Participant.State.OVERRIDE:
                default:
                    participantStatus.setImageResource(R.drawable.contact_status_grey);
                    break;
            }

            // Return View
            return convertView;
        }
    }

}
