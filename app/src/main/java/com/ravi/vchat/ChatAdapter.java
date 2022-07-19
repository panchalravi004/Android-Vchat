package com.ravi.vchat;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.List;

public class ChatAdapter extends BaseAdapter {

    private Context context;
    List<String> sender;
    List<String> time;
    List<String> msg;
    List<String> seen;

    String ourid;
    String fid;

    LinearLayout ll;
    ConstraintLayout cl;
    TextView tvmsg,tvtime;
    ImageView imgseen;

    public ChatAdapter(Context context,List<String> sender,List<String> time,List<String> msg,String ourid,String fid,List<String> seen){
        this.context = context;
        this.sender = sender;
        this.time = time;
        this.msg = msg;
        this.ourid = ourid;
        this.fid = fid;
        this.seen = seen;
    }
    @Override
    public int getCount() {
        return sender.size();
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

        view = LayoutInflater.from(context).inflate(R.layout.chat_row,viewGroup,false);
        ll = view.findViewById(R.id.main_layout);
        cl = view.findViewById(R.id.clayout);
        tvmsg = view.findViewById(R.id.txtmsg);
        tvtime = view.findViewById(R.id.txtChatTime);
        imgseen = view.findViewById(R.id.imgSeen);

        tvtime.setText(time.get(i).substring(11,16));

        if(sender.get(i).equals(ourid)){
            ll.setGravity(Gravity.RIGHT);
            cl.setBackgroundResource(R.drawable.chat_right);
            if(seen.get(i).equals("true")){
                imgseen.setVisibility(View.VISIBLE);
            }else{
                imgseen.setVisibility(View.GONE);
            }
        }else{
            ll.setGravity(Gravity.LEFT);
            cl.setBackgroundResource(R.drawable.chat_left);
            imgseen.setVisibility(View.GONE);
        }

        tvmsg.setText(msg.get(i));

        return view;
    }
}
