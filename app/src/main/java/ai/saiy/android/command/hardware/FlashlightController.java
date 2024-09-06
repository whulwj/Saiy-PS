package ai.saiy.android.command.hardware;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.apache.commons.lang3.SystemProperties;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import ai.saiy.android.utils.MyLog;

/**
 * Manages the flashlight.
 * @see FlashlightController in the SystemUI
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class FlashlightController {
    private static final boolean ENFORCE_AVAILABILITY_LISTENER = MyLog.DEBUG && SystemProperties.getBoolean("persist.sysui.flash.listener", new BooleanSupplier() {
        @Override
        public boolean getAsBoolean() {
            return false;
        }
    });
    private static final int DISPATCH_ERROR = 0;
    private static final int DISPATCH_OFF = 1;
    private static final int DISPATCH_AVAILABILITY_CHANGED = 2;
    @IntDef({DISPATCH_ERROR, DISPATCH_OFF, DISPATCH_AVAILABILITY_CHANGED})
    @Retention(RetentionPolicy.SOURCE)
    private @interface Message {}

    private final CameraManager mCameraManager;
    /** Call {@link #ensureHandler()} before using */
    private Handler mHandler;
    /** Lock on {@code this} when accessing */
    private boolean mFlashlightEnabled;
    private String mCameraId;
    private boolean mCameraAvailable;
    private CameraDevice mCameraDevice;
    private CaptureRequest mFlashlightRequest;
    private CameraCaptureSession mSession;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private boolean isFirstCallback;
    private AtomicInteger retryCount;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = FlashlightController.class.getSimpleName();

    /** Lock on mListeners when accessing */
    private final ArrayList<WeakReference<FlashlightListener>> mListeners = new ArrayList<>(1);
    private final CameraDevice.StateCallback mCameraCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            FlashlightController.this.mCameraDevice = camera;
            postUpdateFlashlight();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onDisconnected");
            }
            if (mCameraDevice == camera) {
                teardown();
                FlashlightController.this.dispatchOff();
            }
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "Camera error: camera=" + camera + " error=" + error);
            }
            if (camera == mCameraDevice || mCameraDevice == null) {
                handleError();
            }
        }
    };
    private final CameraCaptureSession.StateCallback mSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "Configure failed.");
            }
            if (mSession == null || mSession == session) {
                handleError();
            }
        }

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            FlashlightController.this.mSession = session;
            postUpdateFlashlight();
        }
    };
    private final Runnable mUpdateFlashlightRunnable = new Runnable() {
        @Override
        public void run() {
            updateFlashlight(false);
        }
    };
    private final Runnable mKillFlashlightRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                mFlashlightEnabled = false;
            }
            updateFlashlight(true /* forceDisable */);
            dispatchOff();
        }
    };
    private final CameraManager.AvailabilityCallback mAvailabilityCallback = new CameraManager.AvailabilityCallback() {
        @Override
        public void onCameraAvailable(@NonNull String cameraId) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "onCameraAvailable(" + cameraId + ")");
            }
            if (cameraId.equals(mCameraId)) {
                setCameraAvailable(true);
            }
        }

        @Override
        public void onCameraUnavailable(@NonNull String cameraId) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "onCameraUnavailable(" + cameraId + ")");
            }
            if (cameraId.equals(mCameraId)) {
                setCameraAvailable(false);
            }
        }
    };

    public interface FlashlightListener {
        /**
         * Called when the flashlight turns off unexpectedly.
         */
        void onFlashlightOff();
        /**
         * Called when there is a change in availability of the flashlight functionality
         * @param available true if the flashlight is currently available.
         */
        void onFlashlightAvailabilityChanged(boolean available);
        /**
         * Called when there is an error that turns the flashlight on/off.
         */
        void onFlashlightError();
    }

    public FlashlightController(Context context) {
        this.mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        tryInitCamera();
    }

    private Size getSmallestSize(@NonNull String cameraId) throws CameraAccessException, NullPointerException {
        final Size[] outputSizes = this.mCameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(SurfaceTexture.class);
        if (outputSizes == null || outputSizes.length == 0) {
            throw new IllegalStateException("Camera " + cameraId + "doesn't support any outputSize.");
        }
        Size size;
        Size chosen = outputSizes[0];
        for (int i = outputSizes.length - 1; i > 0; --i) {
            size = outputSizes[i];
            if (chosen.getWidth() >= size.getWidth() && chosen.getHeight() >= size.getHeight()) {
                chosen = size;
            }
        }
        return chosen;
    }

    private void dispatchListeners(final @Message int message, boolean argument) {
        synchronized (mListeners) {
            boolean cleanup = false;
            FlashlightListener listener;
            for (int i = 0, size = mListeners.size(); i < size; ++i) {
                listener = mListeners.get(i).get();
                if (listener == null) {
                    cleanup = true;
                    continue;
                }
                switch (message) {
                    case FlashlightController.DISPATCH_ERROR:
                        listener.onFlashlightError();
                        break;
                    case FlashlightController.DISPATCH_OFF:
                        listener.onFlashlightOff();
                        break;
                    case FlashlightController.DISPATCH_AVAILABILITY_CHANGED:
                        listener.onFlashlightAvailabilityChanged(argument);
                        break;
                }
            }
            if (cleanup) {
                cleanUpListenersLocked(null);
            }
        }
    }

    private void cleanUpListenersLocked(@Nullable FlashlightListener targetListener) {
        for (int i = mListeners.size() - 1; i >= 0; i--) {
            FlashlightListener listener = mListeners.get(i).get();
            if (listener == null || listener == targetListener) {
                mListeners.remove(i);
            }
        }
    }

    private void updateFlashlight(boolean forceDisable) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "updateFlashlight: retryCount: " + retryCount.get());
        }
        if (retryCount.incrementAndGet() > 3) {
            CommandHardware.sFlashlightController = null;
            teardown();
            return;
        }
        boolean enabled;
        synchronized (this) {
            enabled = (mFlashlightEnabled && !forceDisable);
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "updateFlashlight enable: " + enabled);
            MyLog.i(CLS_NAME, "updateFlashlight mCameraId: " + mCameraId);
        }
        if (mCameraId == null) {
            handleError();
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                if (enabled) {
                    if (mCameraDevice == null) {
                        try {
                            final String cameraId = getCameraId();
                            if (cameraId != null) {
                                startDevice(cameraId);
                            } else if (DEBUG) {
                                if (DEBUG) {
                                    MyLog.w(CLS_NAME, "no camera");
                                }
                            }
                        } catch (CameraAccessException | IllegalStateException |
                                 UnsupportedOperationException | IllegalArgumentException e) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "startDevice:" + e.getClass().getSimpleName() + ", " + e.getMessage());
                            }
                        }
                        return;
                    }
                    if (mSession == null) {
                        try {
                            startSession();
                        } catch (CameraAccessException | IllegalStateException |
                                 UnsupportedOperationException | IllegalArgumentException e) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "startSession:" + e.getClass().getSimpleName() + ", " + e.getMessage());
                            }
                        }
                        return;
                    }
                    if (mFlashlightRequest != null) {
                        return;
                    }

                    try {
                        final CaptureRequest.Builder requestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        requestBuilder.set(CaptureRequest.FLASH_MODE, android.hardware.camera2.CameraMetadata.FLASH_MODE_TORCH);
                        requestBuilder.addTarget(mSurface);
                        final CaptureRequest captureRequest = requestBuilder.build();
                        mSession.capture(captureRequest, null, mHandler);
                        FlashlightController.this.mFlashlightRequest = captureRequest;
                    } catch (CameraAccessException | IllegalStateException |
                             UnsupportedOperationException | IllegalArgumentException e) {
                        if (DEBUG) {
                            MyLog.w(CLS_NAME, "capture:" + e.getClass().getSimpleName() + ", " + e.getMessage());
                        }
                    }
                } else {
                    if (mCameraDevice != null) {
                        try {
                            mCameraDevice.close();
                        } catch (IllegalStateException e) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "close:" + e.getClass().getSimpleName() + ", " + e.getMessage());
                            }
                        }
                        try {
                            teardown();
                        } catch (IllegalStateException e) {
                            if (DEBUG) {
                                MyLog.w(CLS_NAME, "teardown:" + e.getClass().getSimpleName() + ", " + e.getMessage());
                            }
                        }
                    }
                }
            } catch (IllegalStateException | UnsupportedOperationException | IllegalArgumentException e) {
                if (DEBUG) {
                    MyLog.e(CLS_NAME, "Error in updateFlashlight");
                }
                handleError();
            }
        } else {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "updateFlashlight M");
            }
            try {
                mCameraManager.setTorchMode(mCameraId, enabled);
                retryCount.set(0);
            } catch (CameraAccessException | IllegalStateException e) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "updateFlashlight M:" + e.getClass().getSimpleName() + ", " + e.getMessage());
                }
                handleError();
            }
        }
    }

    private void setCameraAvailable(boolean available) {
        boolean changed;
        synchronized (FlashlightController.this) {
            changed = (FlashlightController.this.mCameraAvailable != available);
            FlashlightController.this.mCameraAvailable = available;
        }
        if (changed) {
            if (DEBUG) {
                MyLog.d(CLS_NAME, "dispatchAvailabilityChanged(" + available + ")");
            }
            dispatchListeners(FlashlightController.DISPATCH_AVAILABILITY_CHANGED, available);
        }
    }

    private synchronized void ensureHandler() {
        if (this.mHandler == null) {
            final HandlerThread handlerThread = new HandlerThread(CLS_NAME, android.os.Process.THREAD_PRIORITY_BACKGROUND);
            handlerThread.start();
            this.mHandler = new Handler(handlerThread.getLooper());
        }
    }

    private void startDevice(String cameraId) throws CameraAccessException, SecurityException {
        mCameraManager.openCamera(cameraId, mCameraCallback, mHandler);
    }

    private void startSession() throws CameraAccessException {
        this.mSurfaceTexture = new SurfaceTexture(0, false);
        final Size size = getSmallestSize(mCameraDevice.getId());
        mSurfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
        this.mSurface = new Surface(mSurfaceTexture);
        mCameraDevice.createCaptureSession(Collections.singletonList(mSurface), mSessionCallback, mHandler);
    }

    private void postUpdateFlashlight() {
        ensureHandler();
        mHandler.post(mUpdateFlashlightRunnable);
    }

    private String getCameraId() throws CameraAccessException {
        for (String cameraId : mCameraManager.getCameraIdList()) {
            CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId);
            Boolean flashAvailable = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            Integer lensFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
            if (flashAvailable != null && flashAvailable && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                return cameraId;
            }
        }
        return null;
    }

    private void teardown() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "teardown");
        }
        synchronized (this) {
            this.mFlashlightEnabled = false;
        }
        this.mCameraDevice = null;
        this.mSession = null;
        this.mFlashlightRequest = null;
        if (this.mSurface != null) {
            mSurface.release();
            mSurfaceTexture.release();
        }
        this.mSurface = null;
        this.mSurfaceTexture = null;
    }

    private void handleError() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "handleError");
        }
        synchronized (this) {
            this.mFlashlightEnabled = false;
        }
        dispatchError();
        dispatchOff();
        updateFlashlight(true);
    }

    private void dispatchOff() {
        dispatchListeners(FlashlightController.DISPATCH_OFF, false/* argument (ignored) */);
    }

    private void dispatchError() {
        dispatchListeners(FlashlightController.DISPATCH_ERROR, false/* argument (ignored) */);
    }

    private void tryInitCamera() {
        this.retryCount = new AtomicInteger();
        try {
            this.mCameraId = getCameraId();
            ensureHandler();
            this.mCameraAvailable = true;
            this.mCameraManager.registerAvailabilityCallback(mAvailabilityCallback, mHandler);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.registerTorchCallback(new CameraManager.TorchCallback() {
                    @Override
                    public void onTorchModeChanged(@NonNull String cameraId, boolean enabled) {
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "onTorchModeChanged(" + cameraId + ") ~ enabled: " + enabled);
                        }
                        if (isFirstCallback) {
                            synchronized (this) {
                                FlashlightController.this.mFlashlightEnabled = enabled;
                            }
                        } else {
                            if (DEBUG) {
                                MyLog.d(CLS_NAME, "onTorchModeChanged: ignoring first callback");
                            }
                            FlashlightController.this.isFirstCallback = true;
                        }
                    }

                    @Override
                    public void onTorchModeUnavailable(@NonNull String cameraId) {
                        if (DEBUG) {
                            MyLog.d(CLS_NAME, "onTorchModeUnavailable(" + cameraId + ")");
                        }
                        if (cameraId.equals(mCameraId)) {
                            setCameraAvailable(false);
                        }
                    }
                }, mHandler);
            }
        } catch (CameraAccessException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "Couldn't initialize");
                e.printStackTrace();
            }
        }
    }

    public synchronized void setFlashlight(boolean enabled) {
        if (this.mFlashlightEnabled != enabled) {
            this.mFlashlightEnabled = enabled;
            postUpdateFlashlight();
        }
    }

    public void killFlashlight() {
        boolean enabled;
        synchronized (this) {
            enabled = mFlashlightEnabled;
        }
        if (enabled) {
            mHandler.post(mKillFlashlightRunnable);
        }
    }

    public synchronized boolean isEnabled() {
        return mFlashlightEnabled;
    }

    public synchronized boolean isAvailable() {
        return ENFORCE_AVAILABILITY_LISTENER ? mCameraAvailable : mCameraId != null;
    }

    public void addListener(FlashlightListener l) {
        synchronized (mListeners) {
            if (mCameraId == null) {
                tryInitCamera();
            }
            cleanUpListenersLocked(l);
            mListeners.add(new WeakReference<>(l));
        }
    }

    public void removeListener(FlashlightListener l) {
        synchronized (mListeners) {
            cleanUpListenersLocked(l);
        }
    }
}
