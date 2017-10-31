package net.flow9.firebasebasic2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.List;

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
                // 데이터 변경 내역이 있으면 배열에 담고
                List<User> data = new ArrayList<>();
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    String id = item.getKey();
                    String token = (String) item.getValue();
                    data.add(new User(id,token));
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
