package com.ravi.vchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {
    FirebaseFirestore db;
    FirebaseUser cuser;
    ImageButton btnBack;
    UsersAdapter ua;
    ListView lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        db = FirebaseFirestore.getInstance();
        cuser = FirebaseAuth.getInstance().getCurrentUser();

        btnBack = (ImageButton) findViewById(R.id.btnBack);
        lv = (ListView) findViewById(R.id.userlist);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(FriendsActivity.this,MainActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        final CollectionReference docref = db.collection("users");
        docref.addSnapshotListener(this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Toast.makeText(FriendsActivity.this, "Loading Error !", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(queryDocumentSnapshots != null){
//                    Toast.makeText(FriendsActivity.this, queryDocumentSnapshots.toString(), Toast.LENGTH_SHORT).show();
                    List<String> uid = new ArrayList<>();
                    List<String> uname = new ArrayList<>();
                    List<String> status = new ArrayList<>();
                    List<String> img = new ArrayList<>();
                    List<String> email = new ArrayList<>();
                    List<String> checkfd = new ArrayList<>();


                    for (QueryDocumentSnapshot doc: queryDocumentSnapshots) {
                        uid.add(doc.getString("uid"));
                        uname.add(doc.getString("uname"));
                        status.add(doc.getString("status"));
                        img.add(doc.getString("img"));
                        email.add(doc.getString("email"));

                        //check user is already exist or not
                        final DocumentReference checkfdref = db.collection("users")
                                .document(cuser.getUid().toString())
                                .collection("friend")
                                .document(doc.getString("uid"));
                        checkfdref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Toast.makeText(FriendsActivity.this, "Fd Check Error !", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (documentSnapshot != null) {
                                    checkfd.add(documentSnapshot.getString("email"));
                                    if (String.valueOf(uid.size()).equals(String.valueOf(checkfd.size()))) {
//                                        Toast.makeText(FriendsActivity.this, checkfd.toString(), Toast.LENGTH_SHORT).show();
                                        ua = new UsersAdapter(FriendsActivity.this,uid,uname,status,img,email,checkfd);
                                        lv.setAdapter(ua);
                                    }
                                }
                            }
                        });

                    }
                }else{
                    Toast.makeText(FriendsActivity.this, "Null", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //empty
    }
}