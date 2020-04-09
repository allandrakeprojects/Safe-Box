package com.uy.safeboxv3app;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity {

    private TextView TV_Status;
    private ImageView IV_Fingerprint;

    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;

    private KeyStore keyStore;
    private Cipher cipher;
    private String KEY_NAME = "AndroidKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("IS_UNLOCK_VAULT", "1");
        editor.commit();

        TV_Status = findViewById(R.id.TV_Status);
        IV_Fingerprint = findViewById(R.id.IV_Fingerprint);

        // Check 1: Android Version >= Marshmallow
        // Check 2: Device has Fingerprint Scanner
        // Check 3: Permission to use Scanner in the App
        // Check 4: Lock screen is secured with at least 1 type of lock
        // Check 5: At least 1 Fingerprint is registered

        UserStatus();
    }

    public void Fingerprint() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
            keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

            if(!fingerprintManager.isHardwareDetected()) {
                TV_Status.setText("Fingerprint scanner not detected in this device.");
            }
            else if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                TV_Status.setText("Permission not granted to use fingerprint scanner.");
            }
            else if(!keyguardManager.isKeyguardSecure()) {
                TV_Status.setText("Add fingerprint to your phone in settings.");
            }
            else if(!fingerprintManager.hasEnrolledFingerprints()) {
                TV_Status.setText("You should add at least 1 fingerprint.");
            }
            else {
                TV_Status.setText("Place your finger to access the App.");

                GenerateKey();

                if (CipherInit()){

                    FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                    FingerprintHandler fingerprintHandler = new FingerprintHandler(this);
                    fingerprintHandler.startAuth(fingerprintManager, cryptoObject);

                }
            }
        }
    }

    public void UserStatus() {
        // detect Device table exists
        final Intent intent = new Intent(this, MainActivity.class);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String android_id = Settings.Secure.getString(getApplication().getContentResolver(), Settings.Secure.ANDROID_ID);
                if (dataSnapshot.hasChild("Users")) {
                    // loop all users if deviceID match

                    // if exists, count if 3 show toast 2 users required
                    DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference yourRef = rootRef.child("Users");
                    ValueEventListener eventListener = new ValueEventListener() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            long count = dataSnapshot.getChildrenCount();

                            if(count == 2) {
                                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                                DatabaseReference yourRef = rootRef.child("Users");
                                ValueEventListener eventListener = new ValueEventListener() {
                                    @RequiresApi(api = Build.VERSION_CODES.M)
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        boolean detected = false;

                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            if(snapshot.child("deviceID").getValue().equals(android_id)) {
                                                detected = true;
                                                Fingerprint();

                                                break;
                                            }
                                        }

                                        if(!detected) {
                                            Toast.makeText(getApplication(), "Limit 2 users only. Auto close initiated.", Toast.LENGTH_SHORT).show();

                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    finish();
                                                    moveTaskToBack(true);
                                                }
                                            }, 2000);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {}
                                };
                                yourRef.addListenerForSingleValueEvent(eventListener);
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    Fingerprint();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    };
                    yourRef.addListenerForSingleValueEvent(eventListener);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Fingerprint();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // leave blank
            }
        });
    }

    public void next(View v){
        Intent intent = new Intent(this, StatusVault.class);
        startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void GenerateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();

        } catch (IOException | CertificateException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException | KeyStoreException e) {

            e.printStackTrace();

        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean CipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }


        try {

            keyStore.load(null);

            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);

            cipher.init(Cipher.ENCRYPT_MODE, key);

            return true;

        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }

    }
}