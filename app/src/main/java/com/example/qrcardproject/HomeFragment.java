package com.example.qrcardproject;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {

    private ImageView qrImage;
    private TextView txtResult;
    private GestureDetectorCompat gestureDetector;
    private ActivityResultLauncher<ScanOptions> barcodeLauncher;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        qrImage = view.findViewById(R.id.qr_image);
        txtResult = view.findViewById(R.id.myEmail);

        FrameLayout qrFrame = view.findViewById(R.id.qr_frame);  // 클릭 대상
        qrImage.setVisibility(View.GONE);  // QR 안 보이게

        qrFrame.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                String email = user.getEmail();
                String uid = user.getUid();
                String qrData = "uid:" + uid + "/email:" + email;

                generateQRCode(qrData);  // QR 생성
                qrImage.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(getContext(), "사용자 확인이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // QR 코드 생성
        generateQRCode("https://example.com");

        // 스와이프 감지
        gestureDetector = new GestureDetectorCompat(requireContext(), new SwipeGestureListener());
        view.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true; // 중요: 이벤트를 소비
        });

        // 스캔 시
        barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() == null) {
                Toast.makeText(getContext(), "스캔이 취소되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                String scannedData = result.getContents();
                Toast.makeText(getContext(), "스캔 결과: " + scannedData, Toast.LENGTH_SHORT).show();

                // fragment_add_scan으로 이동
                AddScanFragment fragmentAddScan = new AddScanFragment();

                // 데이터 전달
                Bundle bundle = new Bundle();
                bundle.putString("scanned_info", scannedData);
                fragmentAddScan.setArguments(bundle);

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.mainFrameLayout, fragmentAddScan)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    private void generateQRCode(String data) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 250, 250);
            Bitmap bitmap = Bitmap.createBitmap(250, 250, Bitmap.Config.RGB_565);

            for (int x = 0; x < 250; x++) {
                for (int y = 0; y < 250; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            qrImage.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void launchScanner() {
        ScanOptions options = new ScanOptions();
        options.setCaptureActivity(PortraitCaptureActivity.class);
        barcodeLauncher.launch(options);
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffY = e1.getY() - e2.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffY) > Math.abs(diffX) &&
                    Math.abs(diffY) > SWIPE_THRESHOLD &&
                    Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0) {
                    launchScanner();  // 위로 스와이프
                    return true;
                }
            }
            return false;
        }
    }
}
