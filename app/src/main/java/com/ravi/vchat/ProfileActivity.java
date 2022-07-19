package com.ravi.vchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    ImageButton btnBack,btnUpdate;
    TextView tvuname,tvemail;
    EditText etuname,etstatus;
    ImageView img_profile;
    ImageButton btnupload,btnselect;

    FirebaseUser cuser;
    FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageref;

    private final int PICK_IMAGE_REQUEST = 22;
    private Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        btnBack = (ImageButton) findViewById(R.id.btn_back);
        btnUpdate = (ImageButton) findViewById(R.id.btn_update);
        tvuname = (TextView) findViewById(R.id.txtuname);
        tvemail = (TextView) findViewById(R.id.txtemail);

        etuname = (EditText) findViewById(R.id.et_username);
        etstatus = (EditText) findViewById(R.id.et_userstatus);

        img_profile = (ImageView) findViewById(R.id.imgprofile);
        btnupload = (ImageButton) findViewById(R.id.btnUpload);
        btnselect = (ImageButton) findViewById(R.id.btnSelect);

        cuser = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        storage = FirebaseStorage.getInstance();
        storageref = storage.getReference();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(ProfileActivity.this,MainActivity.class));
                finish();
            }
        });
        //select profile image to upload
        btnselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        //upload image user profile
        btnupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
        //set data
        db.collection("users")
                .document(cuser.getUid().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            tvuname.setText(task.getResult().getString("uname"));
                            tvemail.setText(task.getResult().getString("email"));

                            etuname.setText(task.getResult().getString("uname"));
                            etstatus.setText(task.getResult().getString("status"));

                            if(task.getResult().getString("img").equals("")){
                                img_profile.setImageResource(R.drawable.icon_smily);
                            }else{
                                Picasso.get().load(task.getResult().getString("img")).into(img_profile);
                            }


                        }
                    }
                });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> user = new HashMap<>();
                user.put("uname",etuname.getText().toString());
                user.put("status",etstatus.getText().toString());

                db.collection("users")
                        .document(cuser.getUid().toString())
                        .update(user)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(ProfileActivity.this, "User Updated !", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void uploadImage() {
        if(filePath != null){
            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Uploading...");
            pd.show();

            StorageReference ref = storageref.child("ProfileImg/"+"profile_img_"+cuser.getUid().toString()+".jpg");

            ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Map<String,Object> imgurlset = new HashMap<>();
                            imgurlset.put("img",uri.toString());
                            db.collection("users")
                                .document(cuser.getUid().toString())
                                .update(imgurlset)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            pd.dismiss();
                                            Toast.makeText(ProfileActivity.this, "Image Uploaded !", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                        }
                    });
                }
            });
        }else{
            Toast.makeText(this, "Please Select Image !", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImage() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(i,"Select Image From Here..."),
                PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);
                img_profile.setImageBitmap(bitmap);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //empty
    }
}