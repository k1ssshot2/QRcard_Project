package com.example.qrcardproject;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;


import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddScanFragment extends Fragment {

    private static final String TAG = "AddScanFragment";

    private FirebaseFirestore db;

    private EditText editName, editEmail, editPhone, editDepartment, editPosition;

    private String getSafeString(Object value) {
        return value != null ? value.toString() : "";
    }

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
        Button btnSave = view.findViewById(R.id.btnSave);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        // ✅ Bundle로부터 scannedUserId 추출
        Bundle args = getArguments();
        if (args != null) {
            String scannedUserId = args.getString("scanned_info");
            if (scannedUserId != null) {
                loadUserData(scannedUserId); // Firestore에서 정보 불러오기
            } else {
                Toast.makeText(getContext(), "스캔된 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }

        btnSave.setOnClickListener(v -> saveContact());
        btnCancel.setOnClickListener(v -> requireActivity());

        editPosition.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_NULL) {
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(editPosition.getWindowToken(), 0);
                }
                editPosition.clearFocus();
                return true;
            }
            return false;
        });

        return view;
    }
    private void loadUserData(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> scannedUserData = documentSnapshot.getData();

                        if (scannedUserData != null) {
                            editName.setText(getSafeString(scannedUserData.get("name")));
                            editEmail.setText(getSafeString(scannedUserData.get("email")));
                            editPhone.setText(getSafeString(scannedUserData.get("phone")));
                            editDepartment.setText(getSafeString(scannedUserData.get("department")));
                            editPosition.setText(getSafeString(scannedUserData.get("position")));
                        } else {
                            Toast.makeText(getContext(), "사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "존재하지 않는 사용자입니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "데이터 로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    requireActivity().onBackPressed();; // 저장 후 이전 화면으로
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "연락처 저장 실패", e);
                    Toast.makeText(getContext(), "저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }
}
