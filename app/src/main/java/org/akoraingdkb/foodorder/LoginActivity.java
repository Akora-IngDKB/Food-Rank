package org.akoraingdkb.foodorder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private SignInButton signInButton;

    private static final int RC_SIGN_IN = 1604;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user is logged in
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        if (mUser != null) {
            // User is already logged in
            // Redirect to MainActivity
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            LoginActivity.this.finish();
        }

        // Get the last signed out method
        SharedPreferences mSharedPreferences = getSharedPreferences(getString(R.string.pref_file_name), Context.MODE_PRIVATE);
        boolean fromMain = mSharedPreferences.getBoolean(getString(R.string.user_signed_out), false);

        setContentView(R.layout.activity_login);
        signInButton = findViewById(R.id.btn_google_login);

        // Configure sign-in to request the user's ID, email address, and basic profile.
        // ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestProfile()
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        final GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Check if the user last signed out through the nav drawer
        if (fromMain) {
            // If yes, show the Sign In Button
            signInButton.setVisibility(View.VISIBLE);
        } else {
            // If no, call the Google Sign In Client for automatic login
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }

        // Set the on click listener for the Google Sign In Button
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null)
                    firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.i("Google_Login", "Google sign in failed", e);
                signInButton.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Sign In Failed\nPlease try again", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Sign the user in with Firebase after successful Google Sign In
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("FB_Google", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.i("Google_Login", "signInWithCredential:success");
                            mUser = mAuth.getCurrentUser();

                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            LoginActivity.this.finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.i("Google Sign In", "signInWithCredential:failure", task.getException());
                            signInButton.setVisibility(View.VISIBLE);
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Sign In Failed\nPlease try again",
                                    Toast.LENGTH_LONG).show();

                        }

                        // ...
                    }
                });
    }

}
