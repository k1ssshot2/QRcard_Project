package com.example.qrcardproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

public class ScanQRFragment extends Fragment {

    private TextView txtResult;
    private Button btnScan;

    // Scan launcher
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() == null) {
                    Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                    txtResult.setText(result.getContents());
                }
            });

    public ScanQRFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_scan_qr, container, false);

        txtResult = view.findViewById(R.id.txtResult);
        btnScan = view.findViewById(R.id.btnScan);

        btnScan.setOnClickListener(v -> onScanButtonClicked());

        return view;
    }

    private void onScanButtonClicked() {
        barcodeLauncher.launch(new ScanOptions());
    }
}
