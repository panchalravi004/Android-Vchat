package com.ravi.vchat;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersAdapter extends BaseAdapter {
    private Context context;
    List<String> uid;
    List<String> uname;
    List<String> status;
    List<String> img;
    List<String> email;
    List<String> checkfd;

    ImageView profile;
    TextView un,sts;
    ImageButton addBtn;
    ConstraintLayout cl;

    FirebaseUser cuser;
    FirebaseFirestore db;

    public UsersAdapter(Context context,List<String> uid,List<String> uname,List<String> status,List<String> img,List<String> email,List<String> checkfd){
        this.context = context;
        this.uid = uid;
        this.uname = uname;
        this.status = status;
        this.img = img;
        this.email = email;
        this.checkfd = checkfd;

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

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = LayoutInflater.from(context).inflate(R.layout.user_row,viewGroup,false);
        profile = view.findViewById(R.id.profileImg);
        un = view.findViewById(R.id.username);
        sts = view.findViewById(R.id.userStatus);
        addBtn = view.findViewById(R.id.btnAdd);
        cl = view.findViewById(R.id.main_frame);

        cuser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();


        if(uid.get(i).equals(cuser.getUid().toString())){
            view.setVisibility(View.GONE);
            cl.setMaxHeight(0);

            return view;
        }else{

            un.setText(uname.get(i));
            //for status of user
            if (status.get(i).equals("")) {
                sts.setText("Available");
            }else{
                sts.setText(status.get(i));
            }
            //for user profile image
            if(img.get(i).equals("")){
                profile.setImageResource(R.drawable.icon_smily);
            }else{
                Picasso.get().load(img.get(i)).into(profile);
            }
            //replace and disable button if user is already friend
            if(checkfd.contains(email.get(i))){
                addBtn.setBackgroundResource(R.drawable.icon_check);
                addBtn.setEnabled(false);
            }
            //add friend into our list
            addBtn.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View view) {
                    ProgressDialog pd = new ProgressDialog(context);
                    pd.setMessage("Adding...");
                    pd.show();

                    LocalDate date = LocalDate.now();
                    LocalTime time = LocalTime.now();

                    Map<String, Object> friend = new HashMap<>();
                    friend.put("email",email.get(i).toString());
                    friend.put("uid",uid.get(i).toString());
                    friend.put("time",date+" "+time);

                    Map<String, Object> userdata = new HashMap<>();
                    userdata.put("email",cuser.getEmail().toString());
                    userdata.put("uid",cuser.getUid().toString());
                    userdata.put("time",date+" "+time);

                    db.collection("users")
                        .document(cuser.getUid().toString())
                        .collection("friend")
                        .document(uid.get(i).toString())
                        .set(friend)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    db.collection("users")
                                            .document(uid.get(i).toString())
                                            .collection("friend")
                                            .document(cuser.getUid().toString())
                                            .set(userdata)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        pd.dismiss();
                                                        Toast.makeText(context, "Added Successfully !", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            }
                        });


                }
            });
            return view;
        }
    }
}
