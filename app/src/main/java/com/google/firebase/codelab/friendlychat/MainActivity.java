/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.codelab.friendlychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.Indexables;
import com.google.firebase.appindexing.builders.PersonBuilder;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import java.util.List;
import java.util.ArrayList;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import java.util.Collections;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.function.Predicate;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;

    private static final String TAG = "MainActivity";
    public static final String PUBLIC_CHILD = "public";
    public static final String USER_CHILD = "users";
    private static final int REQUEST_INVITE = 1;
    private static final int REQUEST_UPLOAD_IMAGE = 2;
    private static final int REQUEST_PUBLISH_IMAGE = 3;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 100;
    public static final String ANONYMOUS = "anonymous";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;
    private GoogleApiClient mGoogleApiClient;
    private static final String MESSAGE_URL = "http://friendlychat.firebase.google.com/message/";

    private Button mUploadButton;
    private Button mPublishButton;
    private Button mSearchButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;

    private DatabaseReference myReference;

    private ListView myListView;

    private List<ImageInfo> privateList;
    private List<ImageInfo> publicList;
    private List<ImageInfo> contentList;

    private CustomListAdapter myListAdapter;

    private Query privateQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Set default username is anonymous.
        mUsername = ANONYMOUS;

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
//        mFirebaseUser = mFirebaseAuth.getCurrentUser();
//
//        if (mFirebaseUser != null) {
//            mUsername = mFirebaseUser.getDisplayName();
//            if (mFirebaseUser.getPhotoUrl() != null) {
//                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
//            }
//        }


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        contentList = new ArrayList<>();
        privateList = new ArrayList<>();
        publicList = new ArrayList<>();

        myListView = (ListView) findViewById(R.id.myListView);

        myListAdapter = new CustomListAdapter(
                this,
                R.layout.item_message,
                contentList);

        myListView.setAdapter(myListAdapter);

        Query publicQuery = mFirebaseDatabaseReference.child(PUBLIC_CHILD);
        publicQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                publicList.clear();
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                    String name = (String) messageSnapshot.child("name").getValue();
                    String text = (String) messageSnapshot.child("text").getValue();
                    String imageUrl = (String) messageSnapshot.child("imageUrl").getValue();
                    String photoUrl = (String) messageSnapshot.child("photoUrl").getValue();
                    String timeStamp = (String) messageSnapshot.child("timeStamp").getValue();
                    ImageInfo tInfo =  new ImageInfo(name, text, imageUrl, photoUrl, timeStamp);
                    publicList.add(tInfo);
                }
                updateContentList();
                updateListView();
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });

        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        if (mFirebaseUser != null) {
            mMessageEditText.setHint(R.string.description_hint);
        } else {
            mMessageEditText.setHint(R.string.sign_in_hint);
        }
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mSharedPreferences
                .getInt(CodelabPreferences.FRIENDLY_MSG_LENGTH, DEFAULT_MSG_LENGTH_LIMIT))});
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    if (mFirebaseUser != null) {
                        mUploadButton.setEnabled(true);
                        mPublishButton.setEnabled(true);
                    }
                    mSearchButton.setEnabled(true);
                } else {
                    mUploadButton.setEnabled(false);
                    mPublishButton.setEnabled(false);
                    mSearchButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        mUploadButton = (Button) findViewById(R.id.uploadButton);
        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_UPLOAD_IMAGE);
            }
        });

        mPublishButton = (Button) findViewById(R.id.publishButton);
        mPublishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_PUBLISH_IMAGE);
            }
        });

        mSearchButton = (Button) findViewById(R.id.searchButton);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateContentList();
                final String search_text = mMessageEditText.getText().toString().toLowerCase();
                contentList.removeIf(new Predicate<ImageInfo>() {
                    public boolean test(ImageInfo info) {
                        return !info.text.toLowerCase().contains(search_text);
                    }
                });
                mMessageEditText.setText("");
                updateListView();
            }
        });

        updateUserStatus();
    }

    private void updateListView() {
        ((BaseAdapter) myListView.getAdapter()).notifyDataSetChanged();
        // hide soft keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void updateContentList() {
        contentList.clear();
        contentList.addAll(publicList);
        contentList.addAll(privateList);
        Collections.sort(contentList, new Comparator<ImageInfo>() {
            @Override
            public int compare(ImageInfo lhs, ImageInfo rhs) {
                return -lhs.timeStamp.compareTo(rhs.timeStamp);
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_UPLOAD_IMAGE || requestCode == REQUEST_PUBLISH_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Uri uri = data.getData();
                    Log.d(TAG, "Uri: " + uri.toString());

                    FriendlyMessage tempMessage = new FriendlyMessage(mMessageEditText.getText().toString(),
                            mUsername, mPhotoUrl, LOADING_IMAGE_URL, new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));

                    if (requestCode == REQUEST_PUBLISH_IMAGE) {
                        myReference = mFirebaseDatabaseReference.child(PUBLIC_CHILD);
                    } else {
                        myReference = mFirebaseDatabaseReference.child(USER_CHILD).child(mFirebaseUser.getUid());
                    }

                    myReference.push().setValue(tempMessage, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError,
                                               DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                String key = databaseReference.getKey();
                                StorageReference storageReference =
                                        FirebaseStorage.getInstance()
                                                .getReference(mFirebaseUser.getUid())
                                                .child(key)
                                                .child(uri.getLastPathSegment());

                                putImageInStorage(storageReference, uri, key, myReference);
                            } else {
                                Log.w(TAG, "Unable to write message to database.",
                                        databaseError.toException());
                            }
                        }
                    });
                }
            }
        }
    }

    private void putImageInStorage(StorageReference storageReference, Uri uri, final String key, final DatabaseReference myReference) {
        storageReference.putFile(uri).addOnCompleteListener(MainActivity.this,
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(),
                                            mUsername, mPhotoUrl, task.getResult().getMetadata().getDownloadUrl().toString(),
                                            new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date())
                                    );
                            myReference.child(key).setValue(friendlyMessage);
                            mMessageEditText.setText("");
                            updateContentList();
                            updateListView();
                        } else {
                            Log.w(TAG, "Image upload task was not successful.",
                                    task.getException());
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    private void updateUserStatus() {
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        System.out.println("updateUserStatus");
        if (mFirebaseUser == null) {
            mUsername = ANONYMOUS;
            mMessageEditText.setHint(R.string.sign_in_hint);
            privateList.clear();
        } else {
            // signed in user
            mUsername = mFirebaseUser.getDisplayName();
            mMessageEditText.setHint(R.string.description_hint);
            System.out.println(mUsername);
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
            privateQuery = mFirebaseDatabaseReference.child(USER_CHILD).child(mFirebaseUser.getUid());
            privateQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    privateList.clear();
                    for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {
                        if ((String) messageSnapshot.child("timeStamp").getValue() == null) continue;
                        String name = (String) messageSnapshot.child("name").getValue();
                        String text = (String) messageSnapshot.child("text").getValue();
                        String imageUrl = (String) messageSnapshot.child("imageUrl").getValue();
                        String photoUrl = (String) messageSnapshot.child("photoUrl").getValue();
                        String timeStamp = (String) messageSnapshot.child("timeStamp").getValue();
                        ImageInfo tInfo =  new ImageInfo(name, text, imageUrl, photoUrl, timeStamp);
                        privateList.add(tInfo);
                    }
                    updateContentList();
                    updateListView();
                }
                @Override
                public void onCancelled(DatabaseError error) {}
            });
        }
        updateContentList();
        updateListView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                if (mFirebaseUser != null) {
                    mFirebaseAuth.signOut();
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                    updateUserStatus();
                    return true;
                } else {
                    return false;
                }
            case R.id.Sign_in_menu:
                if (mFirebaseUser == null) {
                    startActivity(new Intent(this, SignInActivity.class));
                    finish();
                    updateUserStatus();
                    return true;
                } else {
                    return false;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}