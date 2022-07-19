package com.ravi.vchat;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends BaseAdapter {
    private Context context;
    List<String> uid;
    List<String> utime;
    List<String> email;
    List<String> uname;
    List<String> img;
    List<String> status;
    List<String> cnt;


    ImageView profile;
    TextView username,time,count,sts;
    ConstraintLayout cl;

    FirebaseFirestore db;

    public FriendAdapter(Context context,List<String> uid,List<String> utime,List<String> email,List<String> uname,List<String> img,List<String> status,List<String> count){
        this.context = context;
        this.uid = uid;
        this.utime = utime;
        this.email = email;
        this.uname = uname;
        this.status = status;
        this.img = img;
        this.cnt = count;
    }

    @Override
    public int getCount() {
        return uid.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(context).inflate(R.layout.friend_row,viewGroup,false);
        
        profile = view.findViewById(R.id.fdImg);
        username = view.findViewById(R.id.friend_username);
        time = view.findViewById(R.id.friend_lastseen);
        count = view.findViewById(R.id.msg_count);
        sts = view.findViewById(R.id.friend_status);
        cl = view.findViewById(R.id.friend_main_frame);

        username.setText(uname.get(i).toString());

        if(status.get(i).equals("")){
            sts.setText("Available");
        }else{
            sts.setText(status.get(i).toString());
        }

        if(img.get(i).toString().equals("")){
            profile.setBackgroundResource(R.drawable.icon_smily);
        }else{
            Picasso.get().load(img.get(i).toString()).into(profile);
        }

        LocalDate todaydate = LocalDate.now();

        if(todaydate.toString().equals(utime.get(i).substring(0,10))){
            time.setText(utime.get(i).substring(11,16));
        }else{
            time.setText(utime.get(i).substring(0,10));
        }

        if(cnt.get(i).toString().equals("0")){
            count.setVisibility(View.GONE);
        }else{
            count.setText(cnt.get(i).toString());
        }
        cl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inte = new Intent(context,ChatActivity.class);
                inte.putExtra("uid",uid.get(i).toString());
                context.startActivity(inte);
//                Toast.makeText(context, uname.get(i)+" "+uid.get(i)+" "+utime.get(i), Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
}
