package com.example.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference ;
    private  static final int PICTURE_RESULT=42;
    EditText txtTitle;
    EditText txtDescription;
    EditText txtPrice;
    TravelDeal deal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mFirebaseDatabase=FirebaseUtil. mFirebaseDatabase;
        mDatabaseReference=FirebaseUtil.mDatabaseReference;

        txtDescription=(EditText)findViewById(R.id.txtDescription);
        txtTitle=(EditText)findViewById(R.id.txtTitle);
        txtPrice=(EditText)findViewById(R.id.txtPrice);
        Intent intent=getIntent();
        TravelDeal deal =(TravelDeal) intent.getSerializableExtra("Deal");
        if(deal==null){
            deal=new TravelDeal();
        }
        this.deal=deal;
        txtTitle.setText(deal.getTitle());
        txtPrice.setText(deal.getPrice());
        txtDescription.setText(deal.getDescription());
        Button btnImage=findViewById(R.id.btnImage);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(intent.createChooser(intent,"Insert Picture"),PICTURE_RESULT);

            }
        });

    }
//saving using the save button
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
 switch (item.getItemId()){
     case R.id.save_menu:
         saveDeal();
         Toast.makeText(this,"Deal saved",Toast.LENGTH_LONG).show();
         clean();
         backToList();
         return true;
     case R.id.delete_menu:
         deleteDeal();
         Toast.makeText(this,"Deal Deleted",Toast.LENGTH_LONG).show();
         backToList();
         return true;
     default:
         return super.onOptionsItemSelected(item);
 }
    }



    //save_menu creation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if (FirebaseUtil.isAdmin){
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            enableEditText(true);
        }
        else{
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            enableEditText(false);
        }
        return true;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode== PICTURE_RESULT && requestCode == RESULT_OK){
            Uri imageUri = data.getData();
           final StorageReference ref = FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());
            UploadTask uploadTask = ref.putFile(imageUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Log.i("The URL : ", downloadUri.toString());
//                        System.out.println("Upload " + downloadUri);
//                        //Toast.makeText(MainActivity, "Successfully uploaded", Toast.LENGTH_SHORT).show();
//                        if (downloadUri != null) {
//
//                            String url = downloadUri.toString();
//                            System.out.println("Upload " + url);
//
//                        }
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });


//           ref.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//                            String url = uri.toString();
//                           deal.setImageUrl(url);
//                        }
//                    });
////                    String url = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
////                    deal.setImageUrl(url);
//                }
//            });
        }
    }

    private void clean() {
        txtPrice.setText("");
        txtDescription.setText("");
        txtPrice.setText("");
        txtTitle.requestFocus();

    }
    private void saveDeal() {
        deal.setTitle(txtTitle.getText().toString());
        deal.setDescription(txtDescription.getText().toString());
        deal.setPrice(txtPrice.getText().toString());
            if (deal.getId()==null){
                mDatabaseReference.push().setValue(deal);
            }
       else{
           mDatabaseReference.child(deal.getId()).setValue(deal);
            }
    }
    private void deleteDeal(){
        if(deal==null){
            Toast.makeText(this, "Please save the deal before deleting", Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabaseReference.child(deal.getId()).removeValue();

    }
    private void backToList(){
        Intent intent= new Intent(this,ListActivity.class);
        startActivity(intent);
    }
    private void enableEditText(boolean isEnabled){
        txtTitle.setEnabled(isEnabled);
        txtPrice.setEnabled(isEnabled);
        txtDescription.setEnabled(isEnabled);
    }

}
