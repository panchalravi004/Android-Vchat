package com.ravi.vchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    EditText uname,email,pwd;
    ImageButton signup,goto_login;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        email = (EditText) findViewById(R.id.et_email);
        uname = (EditText) findViewById(R.id.et_uname);
        pwd = (EditText) findViewById(R.id.et_pwd);

        signup = (ImageButton) findViewById(R.id.btn_signup);
        goto_login = (ImageButton) findViewById(R.id.btn_goto_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        goto_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignupActivity.this,LoginActivity.class));
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (validate()){
                    registerUser();
                }
            }
        });
    }

    public Boolean validate(){
        if(uname.getText().toString().equals("")){
            Toast.makeText(this, "Enter Your User Name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(email.getText().toString().equals("")){
            Toast.makeText(this, "Enter Your Email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(pwd.getText().toString().equals("")){
            Toast.makeText(this, "Enter Your Password", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
    public void registerUser() {
        String em = email.getText().toString();
        String pw = pwd.getText().toString();
        String un = uname.getText().toString();
        String img = "";
        String status = "Available";

        Map<String, Object> user = new HashMap<>();
        user.put("email",em);
        user.put("pwd",pw);
        user.put("uname",un);
        user.put("img",img);
        user.put("status",status);

        ProgressDialog pd = new ProgressDialog(SignupActivity.this);
        pd.setMessage("Loading....");
        pd.show();

        mAuth.createUserWithEmailAndPassword(em,pw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser cuser = mAuth.getCurrentUser();
                    user.put("uid",cuser.getUid().toString());

                    db.collection("users").document(cuser.getUid().toString())
                                    .set(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            pd.dismiss();
                                            Toast.makeText(SignupActivity.this, "User Register Successfully !", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(SignupActivity.this,LoginActivity.class));
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            pd.dismiss();
                                            Toast.makeText(SignupActivity.this, "Error on Data Add", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                    //Toast.makeText(SignupActivity.this, "uid : "+user.getUid().toString(), Toast.LENGTH_SHORT).show();
                }
                else{
                    pd.dismiss();
                    Toast.makeText(SignupActivity.this, "Fail To Register : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        return;
    }
}