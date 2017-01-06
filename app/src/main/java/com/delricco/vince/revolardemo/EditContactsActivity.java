package com.delricco.vince.revolardemo;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * This class uses code from this post: http://stackoverflow.com/questions/18605768/how-to-create-custom-contact-list-with-checkbox
 */

public class EditContactsActivity extends Activity implements AdapterView.OnItemClickListener {

    private static final String TAG = EditContactsActivity.class.getSimpleName();

    ArrayList<RevolarContact> userContactList = new ArrayList<>();
    ArrayList<RevolarContact> selectedContacts = new ArrayList<>();
    ArrayList<RevolarContact> currentSavedContacts;
    MyAdapter myAdapter ;
    Button saveButton;
    AppPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contacts);

        preferences = new AppPreferences(this.getApplicationContext());
        currentSavedContacts = preferences.getContacts();

        getAllContacts(this.getContentResolver());
        ListView contactsList = (ListView) findViewById(R.id.contacts_list);
        myAdapter = new MyAdapter();
        contactsList.setAdapter(myAdapter);
        contactsList.setOnItemClickListener(this);
        contactsList.setItemsCanFocus(false);
        contactsList.setTextFilterEnabled(true);

        saveButton = (Button) findViewById(R.id.button1);
        saveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                EditContactsActivity.this.selectedContacts = new ArrayList<>();

                for (int i = 0; i < userContactList.size(); i++)
                {
                    if (myAdapter.checkStates.get(i)) {
                        selectedContacts.add(userContactList.get(i));
                    }
                }

                saveContacts();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        myAdapter.toggle(arg2);
    }

    public void saveContacts() {
        if (selectedContacts.size() > 5 || selectedContacts.size() == 0) {
            Toast.makeText(this, R.string.contact_limits, Toast.LENGTH_SHORT).show();
            return;
        }

        preferences.clearContacts();
        preferences.saveContacts(selectedContacts);
        finish();
    }

    public void getAllContacts(ContentResolver cr) {
        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext())
        {
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            userContactList.add(new RevolarContact(name, phoneNumber));
        }

        /* Alphabetize the contact list to make it easy on the user */
        Collections.sort(userContactList, new Comparator<RevolarContact>() {
            public int compare(RevolarContact contact1, RevolarContact contact2) {
                return contact1.getName().compareTo(contact2.getName());
            }
        });

        phones.close();
    }

    class MyAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener {

        private SparseBooleanArray checkStates;
        LayoutInflater inflater;
        TextView contactName, contactPhoneNumber;
        CheckBox checkbox;

        MyAdapter() {
            checkStates = new SparseBooleanArray(userContactList.size());
            inflater = (LayoutInflater) EditContactsActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            /* Set checkbox states to true for contacts that we have currently saved */
            for (int i = 0; i < userContactList.size(); i++) {
                String name = userContactList.get(i).getName();
                String number = userContactList.get(i).getNumber();
                if (contactAlreadySelected(name, number)) {
                    checkStates.put(i, true);
                }
            }
        }

        @Override
        public int getCount() {
            return userContactList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (convertView == null)
                view = inflater.inflate(R.layout.row, null);

            String name = userContactList.get(position).getName();
            String number = userContactList.get(position).getNumber();

            contactName = (TextView) view.findViewById(R.id.contact_name);
            contactName.setText(name);

            contactPhoneNumber = (TextView) view.findViewById(R.id.contact_number);
            contactPhoneNumber.setText(number);

            checkbox = (CheckBox) view.findViewById(R.id.checkBox1);
            checkbox.setTag(position);
            checkbox.setChecked(checkStates.get(position, false));
            checkbox.setOnCheckedChangeListener(this);

            return view;
        }

        public boolean isChecked(int position) {
            return checkStates.get(position, false);
        }

        public void setChecked(int position, boolean isChecked) {
            checkStates.put(position, isChecked);
        }

        public void toggle(int position) {
            setChecked(position, !isChecked(position));
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            checkStates.put((Integer) buttonView.getTag(), isChecked);
        }

        private boolean contactAlreadySelected(String name, String number) {
            for (int i = 0; i < currentSavedContacts.size(); i++) {
                if (currentSavedContacts.get(i).getName().equals(name) &&
                    currentSavedContacts.get(i).getNumber().equals(number)) {
                    return true;
                }
            }

            return false;
        }
    }
}
