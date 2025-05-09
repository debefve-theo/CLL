package ui;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.app_cll_livreur.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

/**
 * Fragment that handles QR code scanning using CameraX and ML Kit.
 * <p>
 * Displays a camera preview, analyzes frames for barcodes, and returns
 * the first scanned value to the calling fragment via setFragmentResult.
 * </p>
 */
public class QrCodeFragment extends Fragment {

    /** PreviewView for displaying the camera feed. */
    private PreviewView previewView;

    /** Button to close the scanner and return to the previous screen. */
    private ImageButton btnClose;

    /** Default empty constructor required for fragment instantiation. */
    public QrCodeFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Inflates the QR code scanner layout, initializes UI elements,
     * and starts the camera preview and analysis.
     *
     * @param inflater           LayoutInflater to inflate views
     * @param container          parent view that the fragment's UI should attach to
     * @param savedInstanceState if non-null, this fragment is being re-created
     * @return the root {@link View} for this fragment's UI
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr_code, container, false);
        previewView = view.findViewById(R.id.previewView);
        btnClose = view.findViewById(R.id.btn_close);

        // Close button returns to the previous fragment
        btnClose.setOnClickListener(v -> requireActivity()
                .getSupportFragmentManager()
                .popBackStack());

        startCamera();
        return view;
    }

    /**
     * Initializes the CameraX ProcessCameraProvider and binds the preview
     * and image analysis use cases.
     */
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCamera(cameraProvider);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    /**
     * Configures and binds the camera preview and image analysis to the
     * fragment lifecycle. Uses ML Kit's BarcodeScanner to process frames.
     *
     * @param cameraProvider the {@link ProcessCameraProvider} instance
     */
    @SuppressLint("UnsafeOptInUsageError")
    private void bindCamera(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis analysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        BarcodeScanner scanner = BarcodeScanning.getClient();

        analysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), imageProxy -> {
            InputImage image = InputImage.fromMediaImage(
                    imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

            scanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            String value = barcode.getRawValue();

                            // Envoyer le résultat au fragment appelant
                            Bundle result = new Bundle();
                            result.putString("qr_result", value);
                            getParentFragmentManager().setFragmentResult("qr_scan", result);

                            // Fermer ce fragment
                            requireActivity().getSupportFragmentManager().popBackStack();
                            break;
                        }
                    })
                    .addOnFailureListener(e -> Log.e("QR", "Scan error", e))
                    .addOnCompleteListener(task -> imageProxy.close());
        });

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, analysis);
    }
}