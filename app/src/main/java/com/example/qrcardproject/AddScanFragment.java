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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class AddScanFragment extends Fragment {

    private static final String TAG = "AddScanFragment";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private EditText editName, editEmail, editPhone, editDepartment, editPosition;

    // 스캔된 사용자의 UID를 저장할 변수 추가
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

        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim(); // 추가
        String department = editDepartment.getText().toString().trim(); // 추가
        String position = editPosition.getText().toString().trim(); // 추가

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(getContext(), "이름과 이메일은 필수입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. 현재 사용자의 friends 컬렉션에서 스캔된 UID와 일치하는 문서가 있는지 확인
        db.collection("users").document(currentUser.getUid()).collection("friends")
                .whereEqualTo("uid", scannedUserUid) // 'uid' 필드로 중복 검사
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // 중복된 연락처가 이미 존재함
                        Toast.makeText(getContext(), "이미 저장된 연락처입니다.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Duplicate contact found for UID: " + scannedUserUid);
                        requireActivity().getSupportFragmentManager().popBackStack(); // 뒤로 가기
                    } else {
                        // 중복된 연락처가 없으므로 새 연락처 저장
                        Map<String, Object> contact = new HashMap<>();
                        contact.put("uid", scannedUserUid); // UID 추가
                        contact.put("name", name);
                        contact.put("email", email);
                        contact.put("phone", phone); // 추가
                        contact.put("department", department); // 추가
                        contact.put("position", position); // 추가

                        db.collection("users").document(currentUser.getUid()).collection("friends").add(contact)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(getContext(), "연락처가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                                    requireActivity().getSupportFragmentManager().popBackStack();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "연락처 저장 실패", e);
                                    Toast.makeText(getContext(), "저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "중복 확인 중 오류 발생", e);
                    Toast.makeText(getContext(), "연락처 저장 중 오류가 발생했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}