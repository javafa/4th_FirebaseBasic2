package net.flow9.firebasebasic2;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    // 파이어베이스 인증모듈 사용 전역변수
    private FirebaseAuth mAuth;

    EditText editEmail;
    EditText editPassword;
    EditText signEmail;
    EditText signPassword;

    TextView infoEmail;
    TextView infoPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        signEmail = findViewById(R.id.signEmail);
        signPassword = findViewById(R.id.signPassword);

        infoEmail = findViewById(R.id.infoEmail);
        infoPassword = findViewById(R.id.infoPassword);
    }

    @Override
    public void onStart() {
        super.onStart();
        // 이미 로그인되어 있어면 사용자를 파이어베이스에서 가져온다.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    // 사용자 등록
    public void signup(View view) {
        String email = editEmail.getText().toString();
        String password = editPassword.getText().toString();
        // validation check
        // 정규식
        if(!isValidEmail(email) || !isValidPassword(password)){
            if(!isValidEmail(email)) {
                infoEmail.setText("이메일 형식이 잘못됬습니다");
                infoEmail.setVisibility(View.VISIBLE);
            }
            if(!isValidPassword(password)) {
                infoPassword.setText("비밀번호 형식이 잘못됬습니다");
                infoPassword.setVisibility(View.VISIBLE);
            }
            return;
        }

        // 파이어베이스의 인증모듈로 사용자를 생성
        mAuth.createUserWithEmailAndPassword(email, password)
                // 완료확인 리스너
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // 이메일 유효성을 확인하기 위해 해당 이메일로 메일이 발송된다.
                        user.sendEmailVerification()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(MainActivity.this
                                            ,"이메일을 발송하였습니다. 확인해주세요"
                                            , Toast.LENGTH_SHORT).show();
                                }
                            });

                    } else {
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    /**
     * Comment  : 정상적인 이메일 인지 검증.
     */
    public static boolean isValidEmail(String email) {
        boolean err = false;
        String regex = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        if(m.matches()) {
            err = true;
        }
        return err;
    }

    /**
     * 영문 숫자를 포함하는 8자리 비밀번호 체크
     * @param password
     * @return
     */
    public static boolean isValidPassword(String password) {
        boolean err = false;
        // 영문자와 숫자만 허용
        String regex = "^[A-Za-z0-9]{8,}$";

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(password);
        if(m.matches()) {
            err = true;
        }
        return err;
    }

    // 사용자 로그인
    public void signin(View view){
        String email = signEmail.getText().toString();
        String password = signPassword.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        // 이메일 검증 확인
                        if(user.isEmailVerified()){
                            // 다음 페이지로 이동
                            Intent intent = new Intent(MainActivity.this,StorageActivity.class);
                            startActivity(intent);
                            finish();
                        }else{
                            Toast.makeText(MainActivity.this
                                    ,"이메일을 확인하셔야 합니다"
                                    , Toast.LENGTH_SHORT ).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    public void getUserInfo(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // 이메일 검증이 완료
            boolean emailVerified = user.isEmailVerified();

            String uid = user.getUid();
        }
    }
}
