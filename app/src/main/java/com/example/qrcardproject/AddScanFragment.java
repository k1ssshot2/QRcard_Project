package com.example.qrcardproject;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddScanFragment extends Fragment {

    private static final String TAG = "AddScanFragment";

    private FirebaseFirestore db;

    private EditText editName, editEmail, editPhone, editDepartment, editPosition;
    private Button btnSave, btnCancel;
    private String scannedUserId;
    private Map<String, Object> scannedUserData;

    public AddScanFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_scan, container, false);

        db = FirebaseFirestore.getInstance();

        // View 바인딩
        editName = view.findViewById(R.id.editName);
        editEmail = view.findViewById(R.id.editEmail);
        editPhone = view.findViewById(R.id.editPhone);
        editDepartment = view.findViewById(R.id.editDepartment);
        editPosition = view.findViewById(R.id.editPosition);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);

        // ✅ Bundle로부터 scannedUserId 추출
        Bundle args = getArguments();
        if (args != null) {
            scannedUserId = args.getString("scanned_info");
            if (scannedUserId != null) {
                loadUserData(scannedUserId); // Firestore에서 정보 불러오기
            } else {
                Toast.makeText(getContext(), "스캔된 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }

        btnSave.setOnClickListener(v -> saveContact());
        btnCancel.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }
    private void loadUserData(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        scannedUserData = documentSnapshot.getData();

                        editName.setText((String) scannedUserData.get("name"));
                        editEmail.setText((String) scannedUserData.get("email"));
                        editPhone.setText((String) scannedUserData.get("phone"));
                        editDepartment.setText((String) scannedUserData.get("department"));
                        editPosition.setText((String) scannedUserData.get("position"));
                    } else {
                        Toast.makeText(getContext(), "상대 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "유저 정보 로드 실패", e);
                    Toast.makeText(getContext(), "정보 불러오기 실패", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveContact() {
        Map<String, Object> contact = new HashMap<>();
        contact.put("name", editName.getText().toString());
        contact.put("email", editEmail.getText().toString());
        contact.put("phone", editPhone.getText().toString());
        contact.put("department", editDepartment.getText().toString());
        contact.put("position", editPosition.getText().toString());

        db.collection("contacts").add(contact)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "연락처가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed(); // 저장 후 이전 화면으로
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "연락처 저장 실패", e);
                    Toast.makeText(getContext(), "저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }
}
