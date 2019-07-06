package com.example.myfirebase;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {
    static final int GOOGLE_SIGN=123;
    FirebaseAuth mAuth;
    Button btn_login,btn_logout;
    GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_login=findViewById(R.id.sign_in);
        btn_logout=findViewById(R.id.log_out);

        mAuth=FirebaseAuth.getInstance();

        GoogleSignInOptions googleSignInOptions=new GoogleSignInOptions
                .Builder()
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

         mGoogleSignInClient= GoogleSignIn.getClient(this,googleSignInOptions);

        btn_login.setOnClickListener(v->SignInGoogle());
        btn_logout.setOnClickListener(v->Logout());

        if(mAuth.getCurrentUser()!=null){
            FirebaseUser user=mAuth.getCurrentUser();
            updateUI(user);
        }

    }

    void SignInGoogle(){
        Intent signIntent=mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signIntent,GOOGLE_SIGN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GOOGLE_SIGN){
            Task<GoogleSignInAccount> task=GoogleSignIn.getSignedInAccountFromIntent(data);

            try{

                GoogleSignInAccount account=task.getResult(ApiException.class);
                if(account !=null) firebaseAuthWithGoogle(account);

            }catch(ApiException e){
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.i("tag","firebaseAuthWithGoogle:" + account.getId());

        AuthCredential credential= GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this,task->{
                        if(task.isSuccessful()){
                            Log.i("tag","sign in success");

                            startActivity(new Intent(getApplicationContext(),ChatActivity.class));
                            FirebaseUser user= mAuth.getCurrentUser();
                        }else{

                            Log.i("tag","Sign in failure ",task.getException());

                            Toast.makeText(this,"SignIn Failed",Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                });
    }

    private void updateUI(FirebaseUser user) {
        if(user!=null){
            String email=user.getEmail();
            btn_login.setVisibility(View.INVISIBLE);
            btn_logout.setVisibility(View.VISIBLE);

            Toast.makeText(this,"Added"+email,Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(),ChatActivity.class));

        }else{

            btn_login.setVisibility(View.VISIBLE);
            btn_logout.setVisibility(View.INVISIBLE);
        }
    }
    void Logout(){

        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, task -> {
                   updateUI(null);
                });
    }
}
