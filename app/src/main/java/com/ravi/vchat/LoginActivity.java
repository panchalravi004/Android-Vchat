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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    EditText email,pwd;
    ImageButton login,goto_signup;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = (EditText) findViewById(R.id.et_email);
        pwd = (EditText) findViewById(R.id.et_pwd);

        login = (ImageButton) findViewById(R.id.btn_signup);
        goto_signup = (ImageButton) findViewById(R.id.btn_goto_login);

        mAuth = FirebaseAuth.getInstance();

        goto_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,SignupActivity.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(validate()){
                    loginUser();
                }
            }
        });
    }
    public Boolean validate(){
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
    public void loginUser() {
        String em = email.getText().toString();
        String pw = pwd.getText().toString();

        ProgressDialog pd = new ProgressDialog(LoginActivity.this);
        pd.setMessage("Loading....");
        pd.show();

        mAuth.signInWithEmailAndPassword(em,pw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    pd.dismiss();
                    Toast.makeText(LoginActivity.this, "Login Successfully !", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this,MainActivity.class));
                }
                else{
                    pd.dismiss();
                    Toast.makeText(LoginActivity.this, "Login Error : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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