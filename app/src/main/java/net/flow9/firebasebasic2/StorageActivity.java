package net.flow9.firebasebasic2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class StorageActivity extends AppCompatActivity implements UserAdapter.Callback{
    // 파일 저장소
    private StorageReference storageRef;
    // 실시간 데이터 베이스
    FirebaseDatabase database;
    DatabaseReference userRef;

    private EditText editMsg;
    private TextView textId;
    private TextView textToken;
    private RecyclerView userList;
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        // 파이어베이스 연결
        storageRef = FirebaseStorage.getInstance().getReference();

        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("users");

        initView();
    }

    // 메시지 전송
    public void send(View view){
        String token = textToken.getText().toString();
        String msg = editMsg.getText().toString();

        if(token == null || "".equals(token)){
            Toast.makeText(this, "받는사람을 선택하세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if(msg == null || "".equals(msg)){
            Toast.makeText(this, "메시지를 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        String json = "{\"to\":\""+token+"\", \"msg\":\""+msg+"\"}";

        // 레트로핏 선언
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.104:8090/")
                .build();
        // 인터페이스와 결합
        IRetro service = retrofit.create(IRetro.class);
        RequestBody body = RequestBody.create(MediaType.parse("plain/text"), json);
        // 서비스로 서버 연결준비
        Call<ResponseBody> remote = service.sendNotification(body);
        // 실제 연결후 데이터 처리
        remote.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    ResponseBody data = response.body();
                    try {
                        Toast.makeText(StorageActivity.this
                                , data.string()
                                , Toast.LENGTH_SHORT).show();
                    }catch(Exception e){e.printStackTrace();}
                }else{

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Retro",t.getMessage());
            }
        });

    }

    // 파일탐색기 호출
    public void chooseFile(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); // 갤러리 image/*   , 동영상 video/*
        startActivityForResult(intent.createChooser(intent, "Select App"), 999);
    }

    // 파일이 선택되면 호출
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
//            String realPath = RealPathUtil.getRealPath(this, uri);
//            upload(realPath);
            upload(uri);
        }
    }

    public void upload(Uri file) {
        // 파이어베이스의 스토리지 파일node
        String temp[] = file.getPath().split("/"); // 파일이름 꺼내는건 다시
        String filename = temp[temp.length - 1];
        StorageReference riversRef = storageRef.child("files/" + filename);

        riversRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Log.d("Storage", "downloadUrl=" + downloadUrl.getPath());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(StorageActivity.this
                                , exception.getMessage()
                                , Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void initView() {
        editMsg = (EditText) findViewById(R.id.editMsg);
        textId = (TextView) findViewById(R.id.textId);
        textToken = (TextView) findViewById(R.id.textToken);
        userList = (RecyclerView) findViewById(R.id.userList);
        adapter = new UserAdapter(this);
        userList.setAdapter(adapter);
        userList.setLayoutManager(new LinearLayoutManager(this));

        // 데이터 변경사항을 체크
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<User> data = new ArrayList<>();
                // 데이터 변경 내역이 있으면 반복문을 돌면서 처리
                for(DataSnapshot item : dataSnapshot.getChildren()){

                    // value 부분만 가져와서 User 클래스로 캐스팅한다
                    User user = item.getValue(User.class);
                    data.add(user);

                }
                // 아답터에 반영
                adapter.setDataAndRefresh(data);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // User Adapter에서 호출되는 콜백 함수
    @Override
    public void setIdAndToken(String id, String token) {
        textId.setText(id);
        textToken.setText(token);
    }
}
