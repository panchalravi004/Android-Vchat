package com.ravi.vchat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageButton btnMenu;
    Button btnchat,btnstatus,btncalls;
    FloatingActionButton gotoFriends;
    FirebaseAuth fa;
    FirebaseFirestore db;
    FirebaseUser cuser;

    ListView lv;
    FriendAdapter fad;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnMenu = (ImageButton) findViewById(R.id.btn_popmenu);
        btnchat = (Button) findViewById(R.id.btn_chat);
        btnstatus = (Button) findViewById(R.id.btn_status);
        btncalls = (Button) findViewById(R.id.btn_calls);
        gotoFriends = (FloatingActionButton) findViewById(R.id.btn_gotofriends);
        lv = (ListView) findViewById(R.id.friendlist);
        fa = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        cuser = fa.getCurrentUser();


        gotoFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,FriendsActivity.class));
            }
        });

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu pm = new PopupMenu(MainActivity.this,btnMenu);
                pm.getMenuInflater().inflate(R.menu.menu,pm.getMenu());
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Toast.makeText(MainActivity.this, menuItem.getTitle(), Toast.LENGTH_SHORT).show();
                        if(menuItem.getTitle().equals("Logout")){
                            fa.signOut();
                            startActivity(new Intent(MainActivity.this,LoginActivity.class));
                        }
                        if(menuItem.getTitle().equals("Profile")){
                            startActivity(new Intent(MainActivity.this,ProfileActivity.class));
                        }
                        return true;
                    }
                });
                pm.show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        final Query colref = db.collection("users")
                .document(cuser.getUid().toString())
                .collection("friend")
                .orderBy("time", Query.Direction.DESCENDING);

        colref.addSnapshotListener(this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Toast.makeText(MainActivity.this, "Loading Error !", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(queryDocumentSnapshots != null){
//                    Toast.makeText(MainActivity.this, queryDocumentSnapshots.toString(), Toast.LENGTH_SHORT).show();
                    List<String> uid = new ArrayList<>();
                    List<String> time = new ArrayList<>();
                    List<String> email = new ArrayList<>();
                    List<String> uname = new ArrayList<>();
                    List<String> img = new ArrayList<>();
                    List<String> status = new ArrayList<>();
                    List<String> unseenmsg = new ArrayList<>();

                    for (QueryDocumentSnapshot doc: queryDocumentSnapshots) {
                        uid.add(doc.getString("uid"));
                        time.add(doc.getString("time"));
                        email.add(doc.getString("email"));

                        final DocumentReference docRef = db.collection("users").document(doc.getString("uid"));
                        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                if(e != null){
                                    Toast.makeText(MainActivity.this, "Loading Error !", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if(documentSnapshot != null){
                                    uname.add(documentSnapshot.getString("uname"));
                                    status.add(documentSnapshot.getString("status"));
                                    img.add(documentSnapshot.getString("img"));

                                    List<String> s = new ArrayList<>();
                                    s.add(doc.getString("uid").toString());

                                    final Query msgcount = db.collection("chatroom").whereIn("sender",s);
                                    msgcount.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                            if(e != null){
                                                Toast.makeText(MainActivity.this, "Unseen msgLoading Error !", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            if (queryDocumentSnapshots != null){
                                                int count = 0;
                                                int i = 0;
                                                for (DocumentSnapshot doc: queryDocumentSnapshots) {
                                                    if (doc.getString("receiver").toString().equals(cuser.getUid().toString())) {
                                                        if(doc.getString("seen").equals("false")){
                                                            count += 1;
//                                                            Toast.makeText(MainActivity.this, doc.getString("msg").toString()+""+queryDocumentSnapshots.size(), Toast.LENGTH_SHORT).show();
                                                        }
//                                                        Toast.makeText(MainActivity.this, doc.getString("msg"), Toast.LENGTH_SHORT).show();
                                                    }
                                                    i += 1;
                                                }
                                                if(String.valueOf(i).equals(String.valueOf(queryDocumentSnapshots.size()))){
                                                    unseenmsg.add(String.valueOf(count));
                                                    if(String.valueOf(uid.size()).equals(String.valueOf(unseenmsg.size()))){
                                                        if(String.valueOf(uid.size()).equals(String.valueOf(uname.size()))){
                                                            fad = new FriendAdapter(MainActivity.this,uid,time,email,uname,img,status,unseenmsg);
                                                            lv.setAdapter(fad);
                                                        }
//                                                        Toast.makeText(MainActivity.this, unseenmsg.toString(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                            }
                                        }
                                    });

                                }
                            }
                        });
//                        Toast.makeText(MainActivity.this, doc.toString(), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "Null", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
//        return;
    }
}