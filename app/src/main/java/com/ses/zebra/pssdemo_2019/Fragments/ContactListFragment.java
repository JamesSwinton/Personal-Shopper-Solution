package com.ses.zebra.pssdemo_2019.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ses.zebra.pssdemo_2019.Adapter.ContactListAdapter;
import com.ses.zebra.pssdemo_2019.R;
import com.ses.zebra.pssdemo_2019.Utilities.EnumToText;
import com.slacorp.eptt.android.service.CoreBinder;
import com.slacorp.eptt.android.service.CoreListSet;
import com.slacorp.eptt.core.common.ContactList;
import com.slacorp.eptt.core.common.List;

public class ContactListFragment extends Fragment {

    // Debugging
    private static final String TAG = "ContactListFragment";

    // Constants

    private static final String BUNDLE_KEY = "Contact-Position";
    private static final Handler mHandler = new Handler();

    // Variables
    private int mSelectedPosition = -1;
    private CoreListener mCoreListener = new CoreListener();
    private LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());

    private ContactList mContactList;
    private ContactListAdapter mContactListAdapter;

    /**
     * Core Listener Class
     * Listens for changes in Core State
     * error()              -> Logs Error
     * sessionStateChange() -> Logs Session State
     * listData()           -> Gets Initial Contact List Data
     * presenceUpdate()     -> Gets Updated Contact List Data
     **/

    private class CoreListener extends com.slacorp.eptt.android.service.CoreListener {
        @Override
        public void error(final int error, String extra) {
            Log.e(TAG, "CoreListener Error - " + error + ", " + extra);
        }

        @Override
        public void sessionStateChange(final int state) {
            Log.i(TAG, "Session Sate: " + EnumToText.getSessionStateAsString(state));
        }

        @Override
        public void listData(List list) {
            Log.i(TAG, "List Data Received | Type: " + EnumToText.getListTypeAsString(list.type));

            // Update List
            mHandler.post(() -> updateList(list));
        }

        @Override
        public void presenceUpdate(List list) {
            Log.i(TAG, "List Date Updated | Type: " + EnumToText.getListTypeAsString(list.type));

            // Update List
            mHandler.post(() -> updateList(list));
        }
    }

    /**
     * Gets instance of CoreBinder from Parent activity when it has been bound
     */
    public void coreServiceBound(CoreBinder corebinder) {
        Log.i(TAG, "CoreServiceBound");

        // Set CoreListener to CoreBinder instance
        corebinder.addCoreListener(mCoreListener);

        // Update Contact List with Data from CoreBinder
        updateList(corebinder.getList(CoreListSet.CONTACT_LIST));
    }

    /**
     * Fragment Initialisation Methods
     * onSaveInstanceState()    -> Saves selected contact to Bundle, if contact is selected
     *                             Bundle is passed to onCreateView() so that new instances of this
     *                             Fragment can get the selected Contact
     * onCreateView()           -> Gets Selected Position from Adapter if possible
     *                             Returns Layout for Fragment
     * onStart()                -> Initialises RecyclerView & Adapter. Assigns Adapter to RecyclerView
     * onResume()               -> Restores selected contact which was obtained in onCreateView()
     */

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Get Selected Position
        mSelectedPosition = (mContactListAdapter == null) ? -1 : mContactListAdapter.getSelectedContactIndex();

        // Save Selected Position to Bundle -> Log Position
        outState.putInt(BUNDLE_KEY, mSelectedPosition);
        Log.i(TAG, "onSaveInstanceState called from: " + this);
        Log.i(TAG, "Saved Selection Position: " + mSelectedPosition);

        // Allow SuperClass to handle onPause as usual
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView called from: " + this);

        // Get SelectedPosition set in onSaveInstance
        mSelectedPosition = (savedInstanceState == null) ? -1 : savedInstanceState.getInt(BUNDLE_KEY);

        // Return Fragment Layout
        return inflater.inflate(R.layout.fragment_contact_list, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart called from: " + this);

        // Check Parent Activity is not Null
        if (getActivity() == null) {
            Log.e(TAG, "Parent Activity is Null, could not create Fragment");
            return;
        }

        // Get RecyclerView
        RecyclerView mContactRecyclerView = getActivity().findViewById(R.id.contactListRecyclerView);
        mContactRecyclerView.setHasFixedSize(true);
        mContactRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Init ContactListAdapter
        mContactListAdapter = new ContactListAdapter(getContext(), mContactRecyclerView);
        mContactListAdapter.setList(mContactList);

        // Set Adapter
        mContactRecyclerView.setAdapter(mContactListAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume called from: " + this);

        // Check if there was a previously selected position
        if (mSelectedPosition >= 0) {
            // Set Previously Selected Contact
            Log.i(TAG, "Contact previously selected: " + mSelectedPosition);
            mContactListAdapter.setSelectedContactIndex(mSelectedPosition);
            return;
        }

        // Notify no previously selected Contact
        Log.i(TAG, "No contact previously selected");
    }

    /**
     * Adapter Utility Methods
     * getSelectedContact()     -> Returns currently Selected Contact in mContactListAdapter
     * updateList()             -> Checks List for nulls -> Gets list.type && updates accordingly
     */

    public ContactList.Entry getSelectedContact() {
        Log.i(TAG, "Getting selected contact...");

        // Check ContactList is not Null
        if (mContactListAdapter == null) {
            Log.e(TAG, "ContactListAdapter is null. Could not get selected contact.");
            return null;
        }

        return mContactListAdapter.getSelectedContact();
    }

    private void updateList(List list) {
        Log.i(TAG, "Deterining List to update...");

        // Check List is not Null
        if (list == null) {
            Log.i(TAG, "List is null. Cannot update contact list.");
            return;
        }

        // Update Contact List
        switch (list.type) {
            case CoreListSet.CONTACT_LIST :
                updateContactList((ContactList) list);
                break;
            case CoreListSet.GROUP_LIST :
                // This Demo doesn't show groups.
                break;
            case CoreListSet.GROUP_PRESENCE_LIST :
                // This Demo doesn't show groups.
                break;
            default:
                Log.e(TAG, "Cannot determine List Type: "
                        + EnumToText.getListTypeAsString(list.type));
        }
    }

    private void updateContactList(ContactList contactList) {
        Log.i(TAG, "Updating Contact List");

        // Create Deep copy of ContactList as it may change or be invalid after this point
        mContactList = contactList.clone();

        // Check Adapter is not Null
        if (mContactListAdapter == null) {
            Log.e(TAG, "ContactListAdapter is null. Could not update Contact List");
            return;
        }

        // Update List
        mContactListAdapter.setList(mContactList);
    }

}
