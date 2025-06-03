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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference; // DocumentReference import 추가

import java.util.HashMap;
import java.util.Map;

public class AddScanFragment extends Fragment {

    private static final String TAG = "AddScanFragment";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private EditText editName, editEmail, editPhone, editDepartment, editPosition;

    // 스캔된 사용자의 UID를 저장할 변수
    private String scannedUserUid = null;

    public AddScanFragment() {
        // Required empty public constructor
    }

    private String getSafeString(Object value) {
        return value != null ? value.toString() : "";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_scan, container, false);

        // Firestore 및 FirebaseAuth 초기화
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // 필드 변수에 뷰 연결
        editName = view.findViewById(R.id.editName);
        editEmail = view.findViewById(R.id.editEmail);
        editPhone = view.findViewById(R.id.editPhone);
        editDepartment = view.findViewById(R.id.editDepartment);
        editPosition = view.findViewById(R.id.editPosition);

        // 키보드 Enter 시 자판 내리기 설정
        setupKeyboardDismiss(editName);
        setupKeyboardDismiss(editEmail);
        setupKeyboardDismiss(editPhone);
        setupKeyboardDismiss(editDepartment);
        setupKeyboardDismiss(editPosition);

        // Cancel 버튼
        Button cancelButton = view.findViewById(R.id.btnCancel);
        cancelButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        // Save 버튼
        Button saveButton = view.findViewById(R.id.btnSave);
        saveButton.setOnClickListener(v -> saveContact());

        // QR 코드 데이터 처리
        Bundle args = getArguments();
        if (args != null) {
            String scannedData = args.getString("scanned_info");
            if (scannedData != null && scannedData.contains("uid:")) {
                scannedUserUid = parseUidFromScannedData(scannedData); // 스캔된 UID 저장
                if (scannedUserUid != null) {
                    loadUserData(scannedUserUid);
                } else {
                    Toast.makeText(getContext(), "유효한 QR 코드 데이터가 아닙니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "스캔된 데이터에 사용자 ID가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }

        return view;
    }

    public String getCurrentUserUid() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            return null;
        }
    }

    private void setupKeyboardDismiss(EditText editText) {
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                }
                editText.clearFocus();
                return true;
            }
            return false;
        });
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
        if (currentUser == null) {
            Toast.makeText(getContext(), "로그인된 사용자가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (scannedUserUid == null || scannedUserUid.isEmpty()) {
            Toast.makeText(getContext(), "저장할 사용자 정보(UID)가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 내 자신의 UID와 스캔된 친구의 UID가 같은 경우 저장하지 않음 (자신을 친구로 추가 방지)
        if (currentUser.getUid().equals(scannedUserUid)) {
            Toast.makeText(getContext(), "자기 자신은 친구로 추가할 수 없습니다.", Toast.LENGTH_SHORT).show();
            requireActivity().getSupportFragmentManager().popBackStack();
            return;
        }

        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String department = editDepartment.getText().toString().trim();
        String position = editPosition.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(getContext(), "이름과 이메일은 필수입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> contact = new HashMap<>();
        contact.put("uid", scannedUserUid); // UID 추가
        contact.put("name", name);
        contact.put("email", email);
        contact.put("phone", phone);
        contact.put("department", department);
        contact.put("position", position);

        // Firestore 문서 ID를 scannedUserUid로 설정하여 저장
        db.collection("users").document(currentUser.getUid()).collection("friends")
                .document(scannedUserUid) // 문서 ID를 친구의 UID로 설정
                .set(contact) // .add() 대신 .set() 사용
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "연락처가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "연락처 저장 실패", e);
                    Toast.makeText(getContext(), "저장에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}