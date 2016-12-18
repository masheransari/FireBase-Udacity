package com.example.asheransari.chatingapplication;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

//import static com.firebase.ui.auth.ui.AcquireEmailHelper.RC_SIGN_IN;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String ANONYMUS = "anonynous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final String FRIENDLY_MSG_LENGTH_KEY = "friendly_msg_length";

    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;

    private String mUserName;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessageDatabaseReference;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    ChildEventListener mChildEventListener = null;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotoStorageReference;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    public static final int RC_SIGN_IN = 1;

    public static final int RC_PHOT_PICKER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUserName = ANONYMUS;

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        mMessageDatabaseReference = mFirebaseDatabase.getReference().child("messages");

        mChatPhotoStorageReference = mFirebaseStorage.getReference().child("chat_photos");

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerbutton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);
        final List<FriendlyMessage> friendlyMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.list_message, friendlyMessages);
        mMessageListView.setAdapter(mMessageAdapter);
        mMessageAdapter.clear();
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
//                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete Action Using"), RC_PHOT_PICKER);
            }
        });


        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(), mUserName, null);
                mMessageDatabaseReference.push().setValue(friendlyMessage);

                mMessageEditText.setText("");
            }
        });

//        mChildEventListener = new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
//                mMessageAdapter.add(friendlyMessage);
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        };
//        mMessageDatabaseReference.addChildEventListener(mChildEventListener);


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
//                    Toast.makeText(MainActivity.this, "You're now Signed in. Welcome to Friendly Chat!", Toast.LENGTH_SHORT).show();
                    onSignedInInitialize(user.getDisplayName());
                    Toast.makeText(MainActivity.this, "USER NAME = " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                } else {
                    onSignoutCleanup();
                    startActivityForResult(AuthUI.getInstance()
                            .createSignInIntentBuilder().setIsSmartLockEnabled(false)
                            .setProviders(AuthUI.EMAIL_PROVIDER, AuthUI.GOOGLE_PROVIDER,AuthUI.FACEBOOK_PROVIDER).build(), RC_SIGN_IN);
                }
            }
        };

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG).build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);


        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put(FRIENDLY_MSG_LENGTH_KEY, DEFAULT_MSG_LENGTH_LIMIT);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);
        fetchConfig();

    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Signed in!!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Signed in Cancelled!!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
             else if(requestCode ==  RC_PHOT_PICKER && resultCode ==  RESULT_OK)
            {
                Uri SelectImageUrI = data.getData();
                StorageReference PhotoRef = mChatPhotoStorageReference.child(SelectImageUrI.getLastPathSegment());

                PhotoRef.putFile(SelectImageUrI).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloaduri = taskSnapshot.getDownloadUrl();
                        FriendlyMessage friendlyMessage =
                                new FriendlyMessage(null,mUserName,downloaduri.toString());
                        mMessageDatabaseReference.push().setValue(friendlyMessage);

                    }
                });

            }
        }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        if (mAuthStateListener != null) {
            super.onPause();
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        detachDatabaseReadListner();
        mMessageAdapter.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    private void onSignedInInitialize(String userName) {
        mUserName = userName;
        attachDatabaseReadListner();
    }

    private void onSignoutCleanup() {
        mUserName = ANONYMUS;
        mMessageAdapter.clear();
        detachDatabaseReadListner();
    }

    private void attachDatabaseReadListner() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                    mMessageAdapter.clear();
//                    FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                    FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                    Log.e("MainActivity",""+dataSnapshot.getValue());
                    Log.e("MainActivity",""+friendlyMessage);
                    mMessageAdapter.add(friendlyMessage);
//                    mMessageListView.setAdapter(mMessageAdapter);
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) { }

                public void onChildRemoved(DataSnapshot dataSnapshot)  { }

                public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

                public void onCancelled(DatabaseError databaseError) { }
            };
            mMessageDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListner() {
        if (mChildEventListener != null) {
            mMessageDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }

    }

    public void fetchConfig()
    {
        long cacheExpiration = 3600;
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled())
        {
            cacheExpiration =0;
        }
        mFirebaseRemoteConfig.fetch(cacheExpiration).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mFirebaseRemoteConfig.activateFetched();
                applyRetrievedLengthLimit();
            }
        })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("MainActivity","Error Fetching Config ",e);
                    applyRetrievedLengthLimit();
                }
            });
    }

    private void applyRetrievedLengthLimit() {

        Long friendly_msg_length = mFirebaseRemoteConfig.getLong(FRIENDLY_MSG_LENGTH_KEY);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(friendly_msg_length.intValue())});
        Log.d(TAG, FRIENDLY_MSG_LENGTH_KEY + " = " + friendly_msg_length);

    }


}


