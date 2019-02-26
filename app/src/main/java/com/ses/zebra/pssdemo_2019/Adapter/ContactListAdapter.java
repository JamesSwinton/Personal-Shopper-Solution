package com.ses.zebra.pssdemo_2019.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ses.zebra.pssdemo_2019.R;
import com.slacorp.eptt.core.common.ContactList;
import com.slacorp.eptt.core.common.Presence;
import com.slacorp.eptt.jcommon.Debugger;

public class ContactListAdapter extends RecyclerView.Adapter<ContactListAdapter.ContactListViewHolder> {

    // Debugging
    private static final String TAG = "ContactListAdapter";

    // Constants


    // Variables
    private Context mCx;
    private int mSelectedContact = -1;
    private ContactList mContactList;
    private RecyclerView mContactListRecyclerView;

    /**
     * Gets Instance of RecyclerView && assigns to Member Variable
     */
    public ContactListAdapter(Context cx, RecyclerView contactListRecyclerView) {
        this.mCx = cx;
        this.mContactListRecyclerView = contactListRecyclerView;
    }

    /**
     * Returns Layout for RecyclerView
     */
    @Override
    public ContactListAdapter.ContactListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ContactListViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.adapter_contact_list, parent, false)
        );
    }

    /**
     * Return ContactList size to ContactListViewHolder
     */
    @Override
    public int getItemCount() {
        return mContactList == null ? 0 : mContactList.getEntryCount();
    }

    /**
     * ViewHolder to define RecyclerView Layout & Elements
     */
    public class ContactListViewHolder extends RecyclerView.ViewHolder {

        TextView contactName;
        ImageView contactStatusIcon;

        public ContactListViewHolder(View view) {
            super(view);

            // Get Elements
            this.contactName = view.findViewById(R.id.contactName);
            this.contactStatusIcon = view.findViewById(R.id.contactStatusIcon);

            // Set Child Click Listener
            view.setOnClickListener(v -> {
                Log.i(TAG, "Contact Selected | Position: " + getLayoutPosition());

                // Update Selected Item
                notifyItemChanged(mSelectedContact);
                mSelectedContact = mContactListRecyclerView.getChildAdapterPosition(v);
                notifyItemChanged(mSelectedContact);
            });
        }
    }

    /**
     * Binds Variables to UI Elements within ContactListViewHolder
     */
    @Override
    public void onBindViewHolder(@NonNull ContactListViewHolder viewHolder, int position) {
        // Check Contact List is not Null
        if (mContactList == null) {
            Log.e(TAG, "Contact List is Null. Cannot Load Contacts.");
            viewHolder.contactName.setText("");
            viewHolder.contactStatusIcon.setImageResource(R.drawable.contact_status_grey);
            return;
        }

        // Check Contact is not Null
        ContactList.Entry contact = mContactList.getEntry(position);
        if (contact == null) {
            Log.e(TAG, "Contact not found in list | Total Contacts: " + mContactList.getEntryCount()
                             + " | Selected Position: " + position);
            viewHolder.contactName.setText("");
            viewHolder.contactStatusIcon.setImageResource(R.drawable.contact_status_grey);
            return;
        }

        // Set Background state (handled by background_contact_list_state.xml)
        viewHolder.itemView.setSelected(mSelectedContact == position);

        // Set Contact Name
        viewHolder.contactName.setText(mCx.getString(R.string.contactName, contact.firstName, contact.lastName));
        // Determine Presence -> Set Icon Accordingly
        switch (contact.getPresence()) {
            case Presence.UNAVAILABLE:
                viewHolder.contactStatusIcon.setImageResource(R.drawable.contact_status_grey);
                break;
            case Presence.AVAILABLE:
                viewHolder.contactStatusIcon.setImageResource(R.drawable.contact_status_green);
                break;
            case Presence.IN_CALL:
                viewHolder.contactStatusIcon.setImageResource(R.drawable.contact_status_red);
                break;
            case Presence.DND:
                viewHolder.contactStatusIcon.setImageResource(R.drawable.contact_status_dnd);
                break;
            case Presence.NOT_RESPONDING:
                viewHolder.contactStatusIcon.setImageResource(R.drawable.contact_status_green_qm);
                break;
            case Presence.SILENT:
                viewHolder.contactStatusIcon.setImageResource(R.drawable.contact_status_silent);
                break;
            case Presence.URGENT_COMM_ONLY:
                viewHolder.contactStatusIcon.setImageResource(R.drawable.contact_status_green);
                break;
            case Presence.CIRCUIT_CALL:
                viewHolder.contactStatusIcon.setImageResource(R.drawable.contact_status_red);
                break;
            case Presence.SIGNED_OUT:
                viewHolder.contactStatusIcon.setImageResource(R.drawable.contact_status_grey);
                break;
            default:
                viewHolder.contactStatusIcon.setImageResource(R.drawable.contact_status_grey);
                break;
        }
    }

    /**
     * Utility Methods
     * getSelectedContact()         -> Returns selected contact if selected & exists in ContactList
     * getSelectedItem()            -> Returns selected item index
     * setSelectedContactIndex()    -> Sets selected Contact & Refreshes Recycler View
     * setList()                    -> Sets ContactList & Refreshes Recycler View
     */

    public ContactList.Entry getSelectedContact() {
        // Check if Contact has been Selected
        if (mSelectedContact < 0) {
            Log.e(TAG, "getSelectedContact(), no item is selected.");
            return null;
        }

        // Check Contact List is Populated
        if (mContactList == null) {
            Debugger.e(TAG, "getSelectedContact(), mList is null.");
            return null;
        }

        // Check Contact exists in ContactList
        ContactList.Entry contact = mContactList.getEntry(mSelectedContact);
        if (contact == null) {
            Log.e(TAG, "Contact not found in list | Total Contacts: " + mContactList.getEntryCount()
                    + " | Selected Position: " + mSelectedContact);
        }

        return contact;
    }

    public int getSelectedContactIndex() {
        return mSelectedContact;
    }

    public void setSelectedContactIndex(int position) {
        mSelectedContact = position;

        // Update ContactList if Possible
        if (mContactList != null) {
            notifyItemChanged(mSelectedContact);
        }
    }

    public void setList(ContactList contactList) {
        mContactList = contactList;
        notifyDataSetChanged();
    }

}

