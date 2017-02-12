package us.bridgeses.Minder.util.Scanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import us.bridgeses.Minder.util.vandy.LifecycleLoggingActivity;

/**
 * Created by tbrid on 2/10/2017.
 */

public class BarcodeScanner extends LifecycleLoggingActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        Intent result = new Intent();
        result.putExtra("SCAN_RESULT", rawResult.getText());
        setResult(RESULT_OK, result);
        finish();
    }
}
