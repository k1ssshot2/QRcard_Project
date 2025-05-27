package com.example.qrcardproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.KeyEvent;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.qrcardproject.R;

public class MyInfoFragment extends Fragment {

    private static final String TAG = "MyInfoFragment";

    private Button btnSave;
    private Button btnLogout;
    private ImageView imageDefault;
    private EditText editName;
    private EditText editEmail;
    private EditText editPhone;
    private EditText editDepartment;
    private EditText editPosition;
    private EditText newPassword;
    private EditText confirmNewPassword;
    private ImageButton buttonShowNewPassword;
    private ImageButton buttonShowConfirmNewPassword;

    private boolean isNewPasswordVisible = false;
    private boolean isConfirmNewPasswordVisible = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_info, container, false);

        btnSave = view.findViewById(R.id.btnSave);
        btnLogout = view.findViewById(R.id.btnLogout);
        imageDefault = view.findViewById(R.id.imageDefault);
        editName = view.findViewById(R.id.editName);
        editEmail = view.findViewById(R.id.editEmail);
        editPhone = view.findViewById(R.id.editPhone);
        editDepartment = view.findViewById(R.id.editDepartment);
        editPosition = view.findViewById(R.id.editPosition);
        newPassword = view.findViewById(R.id.newPassword);
        confirmNewPassword = view.findViewById(R.id.confirmNewPassword);
        buttonShowNewPassword = view.findViewById(R.id.buttonShowNewPassword);
        buttonShowConfirmNewPassword = view.findViewById(R.id.buttonShowConfirmNewPassword);

        newPassword.setText("");
        confirmNewPassword.setText("");

        loadUserProfile();

        btnSave.setOnClickListener(v -> {
            saveUserProfile();
        });

        btnLogout.setOnClickListener(v -> {
            logoutUser();
        });

        buttonShowNewPassword.setOnClickListener(v -> {
            togglePasswordVisibility(newPassword, buttonShowNewPassword, !isNewPasswordVisible);
            isNewPasswordVisible = !isNewPasswordVisible;
        });

        buttonShowConfirmNewPassword.setOnClickListener(v -> {
            togglePasswordVisibility(confirmNewPassword, buttonShowConfirmNewPassword, !isConfirmNewPasswordVisible);
            isConfirmNewPasswordVisible = !isConfirmNewPasswordVisible;
        });

        // editPosition에서 엔터 누르면 키보드 내리기
        editPosition.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                hideKeyboard(v);
                return true;
            }
            return false;
        });

// confirmNewPassword에서 엔터 누르면 키보드 내리기
        confirmNewPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                hideKeyboard(v);
                return true;
            }
            return false;
        });
        return view;
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userRef = db.collection("users").document(userId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    editName.setText(documentSnapshot.getString("name"));
                    editEmail.setText(documentSnapshot.getString("email"));
                    editPhone.setText(documentSnapshot.getString("phone"));
                    editDepartment.setText(documentSnapshot.getString("department"));
                    editPosition.setText(documentSnapshot.getString("position"));
                } else {
                    Log.d(TAG, "사용자 문서가 존재하지 않습니다.");
                    Toast.makeText(getContext(), "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "사용자 정보 로드 실패", e);
                Toast.makeText(getContext(), "사용자 정보를 로드하는 데 실패했습니다: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        } else {
            Log.d(TAG, "현재 로그인된 사용자가 없습니다.");
            Toast.makeText(getContext(), "로그인된 사용자가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveUserProfile() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "로그인된 사용자가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String department = editDepartment.getText().toString().trim();
        String position = editPosition.getText().toString().trim();
        String newPass = newPassword.getText().toString();
        String confirmPass = confirmNewPassword.getText().toString();

        DocumentReference userRef = db.collection("users").document(currentUser.getUid());
        userRef.update(
                "name", name,
                "email", email,
                "phone", phone,
                "department", department,
                "position", position
        ).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "프로필 정보가 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "프로필 정보 업데이트 실패", e);
            Toast.makeText(getContext(), "프로필 정보 업데이트에 실패했습니다: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });

        if (!newPass.isEmpty()) {
            if (newPass.equals(confirmPass)) {
                if (newPass.length() >= 6) {
                    currentUser.updatePassword(newPass)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "비밀번호가 성공적으로 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                                    newPassword.setText("");
                                    confirmNewPassword.setText("");
                                } else {
                                    Log.e(TAG, "비밀번호 업데이트 실패", task.getException());
                                    Toast.makeText(getContext(), "비밀번호 업데이트 실패: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    Toast.makeText(getContext(), "비밀번호는 6자리 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(getContext(), "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), LoginScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void togglePasswordVisibility(EditText editText, ImageButton imageButton, boolean showPassword) {
        if (showPassword) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            imageButton.setImageResource(R.drawable.ic_visibility);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            imageButton.setImageResource(R.drawable.ic_visibility_off);
        }
        editText.setSelection(editText.getText().length());
    }
}