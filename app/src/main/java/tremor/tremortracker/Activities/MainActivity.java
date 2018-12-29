package tremor.tremortracker.Activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import tremor.tremortracker.R;

/**
 * Created by sonal on 03-03-2018.
 */

public class MainActivity extends AppCompatActivity {


    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private static final String TAG = "MainActivity";
    private EditText email;
    private EditText password;
    private Button login;
    private Button signout;
    private Button createaccount;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = (EditText)findViewById(R.id.EmailId);
        password=(EditText)findViewById(R.id.PasswordId);
        login=(Button)findViewById(R.id.LoginId);
        signout=(Button)findViewById(R.id.signoutid);
        createaccount=(Button)findViewById(R.id.createid);

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        databaseReference = database.getReference("message");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user!=null){
                    Log.d("","user signed in");
                    //startActivity(new Intent(MainActivity.this, MapsActivity.class));

                }else{
                    Log.d("","user signed out");
                }

            }
        };

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailString= email.getText().toString();
                String pwd=password.getText().toString();


                if (!emailString.equals("") && !pwd.equals(""))
                {
                  mAuth.signInWithEmailAndPassword(emailString,pwd)
                          .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                              @Override
                              public void onComplete(@NonNull Task<AuthResult> task) {

                                  if(!task.isSuccessful()){
                                      Toast.makeText(MainActivity.this, "Failed sign in",Toast.LENGTH_LONG)
                                              .show();

                                  }
                                  else
                                  {
                                      Toast.makeText(MainActivity.this, "Signed in",Toast.LENGTH_LONG)
                                              .show();
                                      startActivity(new Intent(MainActivity.this, MapsActivity.class));


                                  }
                              }
                          });
                }
            }
        });
              signout.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View view) {
                      mAuth.signOut();
                      Toast.makeText(MainActivity.this,"You signed out",Toast.LENGTH_LONG)
                              .show();

                  }
              });

              createaccount.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View view) {
                      String emailString = email.getText().toString();
                      String pwd = password.getText().toString();

                      if (!emailString.equals("") && !pwd.equals("")) {
                          mAuth.createUserWithEmailAndPassword(emailString, pwd).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                              @Override
                              public void onComplete(@NonNull Task<AuthResult> task) {
                                  if(!task.isSuccessful()) {
                                      Toast.makeText(MainActivity.this, "Failed to create accopunt", Toast.LENGTH_LONG)
                                              .show();
                                  }
                                  else {
                                      Toast.makeText(MainActivity.this, "Account Created", Toast.LENGTH_LONG)
                                              .show();

                                  }

                              }
                          });
                      }
                  }
              });



              }

    @Override
    protected  void onStart() {

        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }
    @Override
    protected  void onStop(){
        super.onStop();
        if(mAuthStateListener != null)
        {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }
}
