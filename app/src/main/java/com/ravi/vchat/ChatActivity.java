package com.ravi.vchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    String uid;
    ImageButton btBack,btMenu;
    FloatingActionButton btSend;
    EditText etmsg;
    FirebaseUser cuser;
    FirebaseFirestore db;

    ImageView profile;
    TextView username;

    ChatAdapter ca;
    ListView chatlist;

    ListenerRegistration lisReg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        uid = getIntent().getExtras().getString("uid");

        btBack = (ImageButton) findViewById(R.id.btnFDback);
        btMenu = (ImageButton) findViewById(R.id.btnFDmenu);

        btSend = (FloatingActionButton) findViewById(R.id.btnSendMsg);
        etmsg = (EditText) findViewById(R.id.etMsg);

        profile = (ImageView) findViewById(R.id.fdImg);
        username = (TextView) findViewById(R.id.fdname);

        chatlist = (ListView) findViewById(R.id.chatlist);

        cuser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChatActivity.this,MainActivity.class));
                finish();
            }
        });
        //

        //for menu
        btMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu pm = new PopupMenu(ChatActivity.this,btMenu);
                pm.getMenuInflater().inflate(R.menu.fd_chat_menu,pm.getMenu());;
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Toast.makeText(ChatActivity.this, menuItem.getTitle().toString(), Toast.LENGTH_SHORT).show();
                        if(menuItem.getTitle().equals("Clear Chat")){
                            clearchat();
                        }
                        if(menuItem.getTitle().equals("Remove User")){
                            clearchat();
                            removeuser();
                        }
                        return true;
                    }
                });
                pm.show();

            }
        });

        //sende meassage to the friend
        btSend.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                LocalDate date = LocalDate.now();
                LocalTime time = LocalTime.now();

                Map<String, Object> chatdata = new HashMap<>();
                chatdata.put("sender",cuser.getUid().toString());
                chatdata.put("receiver",uid);
                chatdata.put("msg",etmsg.getText().toString());
                chatdata.put("seen","false");
                chatdata.put("time",date+" "+time);

                db.collection("chatroom")
                        .document(date+" "+time)
                        .set(chatdata)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    etmsg.setText("");
                                    Map<String, Object> msgtime = new HashMap<>();
                                    msgtime.put("time",date+" "+time);

                                    db.collection("users")
                                            .document(cuser.getUid().toString())
                                            .collection("friend")
                                            .document(uid).update(msgtime)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        db.collection("users")
                                                                .document(uid)
                                                                .collection("friend")
                                                                .document(cuser.getUid().toString()).update(msgtime)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            Toast.makeText(ChatActivity.this, "msg Send", Toast.LENGTH_SHORT).show();

                                                                        }
                                                                    }
                                                                });
                                                    }
                                                }
                                            });
                                }
                            }
                        });
            }
        });

    }
    //clear  chat
    public void clearchat(){
        db.collection("chatroom")
                .whereEqualTo("sender",cuser.getUid().toString())
                .whereEqualTo("receiver",uid)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        for (DocumentSnapshot doc: queryDocumentSnapshots) {
                            doc.getReference().delete();
                        }
                    }
                });
        db.collection("chatroom")
                .whereEqualTo("sender",uid)
                .whereEqualTo("receiver",cuser.getUid().toString())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        for (DocumentSnapshot doc: queryDocumentSnapshots) {
                            doc.getReference().delete();
                        }
                    }
                });
    }
    //remove user
    public void removeuser(){
        db.collection("users")
                .document(cuser.getUid().toString())
                .collection("friend")
                .document(uid).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        db.collection("users")
                                .document(uid)
                                .collection("friend")
                                .document(cuser.getUid().toString()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(ChatActivity.this, "User Removed", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(ChatActivity.this,MainActivity.class));
                                        finish();
                                    }
                                });
                    }
                });
    }

    //seen all unseen message
    public void seenMsg(){
        List<String> s = new ArrayList<>();
        s.add(uid);
        //lisReg = <our listener>;<we can use it to add listener
        //listReg.remove(); to remove all the listener on particular time
        Query seenmsg = db.collection("chatroom").whereIn("sender",s);
        lisReg = seenmsg.addSnapshotListener(this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Toast.makeText(ChatActivity.this, "Unseen msgLoading Error !", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (queryDocumentSnapshots != null){

                    for (DocumentSnapshot doc: queryDocumentSnapshots) {
                        if (doc.getString("receiver").toString().equals(cuser.getUid().toString())) {
                            if(doc.getString("seen").equals("false")){
                                Map<String, Object> val = new HashMap<>();
                                val.put("seen","true");

                                db.collection("chatroom")
                                        .document(doc.getId().toString())
                                        .update(val).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
//                                                    Toast.makeText(ChatActivity.this, "All msg are seen", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        }
                    }
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //unseen msg to seen
        seenMsg();
        //for user name and profile image
        final DocumentReference userdata = db.collection("users").document(uid);
        userdata.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Toast.makeText(ChatActivity.this, "Loading Error for userdata", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(documentSnapshot != null){
                    username.setText(documentSnapshot.getString("uname"));
                    if(documentSnapshot.getString("img").toString().equals("")){
                        profile.setBackgroundResource(R.drawable.icon_smily);
                    }else{
                        Picasso.get().load(documentSnapshot.getString("img").toString()).into(profile);
                    }
                }
            }
        });
        //get chat
        List<String> sender = new ArrayList<>();
        sender.add(cuser.getUid().toString());
        sender.add(uid);
        final Query colref = db.collection("chatroom").whereIn("sender",sender);
        colref.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Toast.makeText(ChatActivity.this, "Loading Error ! ", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (queryDocumentSnapshots != null) {
                    List<String> senderid = new ArrayList<>();
                    List<String> time = new ArrayList<>();
                    List<String> msg = new ArrayList<>();
                    List<String> seen = new ArrayList<>();


                    for (DocumentSnapshot doc: queryDocumentSnapshots) {
                        if(doc.getString("receiver").toString().equals(uid) || doc.getString("receiver").toString().equals(cuser.getUid().toString())){
                            senderid.add(doc.getString("sender"));
                            time.add(doc.getString("time"));
                            msg.add(doc.getString("msg"));
                            seen.add(doc.getString("seen"));

                            Log.i("userchat",doc.getString("sender")+" -> "+doc.getString("msg"));
                        }
                    }
                    ca = new ChatAdapter(ChatActivity.this,senderid,time,msg,cuser.getUid().toString(),uid,seen);
                    chatlist.setAdapter(ca);

//                    Log.i("userchat",queryDocumentSnapshots.getDocuments().toString());
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        startActivity(new Intent(ChatActivity.this,MainActivity.class));
        finish();
//        return;
    }
}