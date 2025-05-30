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

        EditText nameEdit = view.findViewById(R.id.editName);
        EditText emailEdit = view.findViewById(R.id.editEmail);
        EditText phoneEdit = view.findViewById(R.id.editPhone);
        EditText departmentEdit = view.findViewById(R.id.editDepartment);
        EditText positionEdit = view.findViewById(R.id.editPosition);

        // Cancel 버튼 기능
        Button cancelButton = view.findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        Bundle args = getArguments();
        if (args != null) {
            String scannedData = args.getString("scanned_info");
            if (scannedData != null && scannedData.contains("uid:")) {
                String uid = parseUidFromScannedData(scannedData);

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("users").document(uid).get()
                        .addOnSuccessListener(document -> {
                            if (document.exists()) {
                                nameEdit.setText(document.getString("name"));
                                emailEdit.setText(document.getString("email"));
                                phoneEdit.setText(document.getString("phone"));
                                departmentEdit.setText(document.getString("department"));
                                positionEdit.setText(document.getString("position"));
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "사용자 정보 불러오기 실패", Toast.LENGTH_SHORT).show();
                        });
            }
        }

        return view;
    }

    private String parseUidFromScannedData(String scannedData) {
        for (String line : scannedData.split("\n")) {
            if (line.startsWith("uid:")) {
                return line.substring(4).trim();
            }
        }
        return null;
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
