package io.flutter.plugins.camera;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import io.flutter.plugin.common.EventChannel;

public class TorchController {

    private final CameraManager cameraManager;
    private final CaptureRequest.Builder captureRequestBuilder;
    private final CameraCaptureSession cameraCaptureSession;
    private CameraManager.TorchCallback torchCallbackListener;

    public TorchController(CameraManager cameraManager,
                           CaptureRequest.Builder captureRequestBuilder,
                           CameraCaptureSession cameraCaptureSession) {
        this.cameraManager = cameraManager;
        this.captureRequestBuilder = captureRequestBuilder;
        this.cameraCaptureSession = cameraCaptureSession;
    }

    public void toggleTorch(boolean isEnable) throws CameraAccessException {
        if (isEnable) {
            captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
        } else {
            captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
        }
        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void initTorchChannel(@NonNull final EventChannel torchStatusChannel) {
        torchStatusChannel.setStreamHandler(
                new EventChannel.StreamHandler() {
                    @Override
                    public void onListen(Object o, EventChannel.EventSink torchStreamSink) {
                        registerTorchStatusListener(torchStreamSink);
                    }

                    @Override
                    public void onCancel(Object o) {
                        if (torchCallbackListener != null)
                            cameraManager.unregisterTorchCallback(torchCallbackListener);
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void registerTorchStatusListener(@NonNull final EventChannel.EventSink torchStatusStreamListener) {
        if (torchCallbackListener == null)
            initTorchStatusListener(torchStatusStreamListener);
        cameraManager.registerTorchCallback(torchCallbackListener, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initTorchStatusListener(EventChannel.EventSink torchStatusStreamListener) {
        torchCallbackListener = new CameraManager.TorchCallback() {
            @Override
            public void onTorchModeUnavailable(String cameraId) {
                super.onTorchModeUnavailable(cameraId);
            }

            @Override
            public void onTorchModeChanged(String cameraId, boolean enabled) {
                super.onTorchModeChanged(cameraId, enabled);
                torchStatusStreamListener.success(enabled);
            }
        };
    }
}
