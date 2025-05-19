package com.example.qrcardproject;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

public class HomeFragment extends Fragment {

    private TextView txtResult;
    private GestureDetectorCompat gestureDetector;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() == null) {
                    Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Scanned: " + result.getContents(), Toast.LENGTH_SHORT).show();
                    txtResult.setText(result.getContents());
                }
            });

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_main_screen, container, false);

        txtResult = new TextView(getContext());  // 필요 시 결과 출력용
        gestureDetector = new GestureDetectorCompat(getContext(), new SwipeGestureListener());
        view.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        return view;
    }

    private void launchScanner() {
        ScanOptions options = new ScanOptions();
        options.setCaptureActivity(PortraitCaptureActivity.class);  // 세로 고정된 커스텀 액티비티
        barcodeLauncher.launch(options);
    }

    // 제스처 리스너 클래스
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
