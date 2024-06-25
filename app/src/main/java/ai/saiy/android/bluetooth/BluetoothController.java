package ai.saiy.android.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import java.util.List;

import ai.saiy.android.utils.BluetoothConstants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.SPH;
import ai.saiy.android.utils.UtilsString;

public class BluetoothController {
    private BluetoothDevice bluetoothDevice;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothHeadset mConnectedHeadset;
    private final Context mContext;
    private final AudioManager audioManager;

    private final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = BluetoothController.class.getSimpleName();

    private boolean profileRegistered = false;
    private boolean receiverRegistered = false;
    private final BluetoothProfile.ServiceListener mHeadsetProfileListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile bluetoothProfile) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onServiceConnected");
            }
            mConnectedHeadset = (BluetoothHeadset) bluetoothProfile;
            List<BluetoothDevice> connectedDevices = mConnectedHeadset.getConnectedDevices();
            if (connectedDevices.isEmpty()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onServiceConnected mConnectedHeadset not currently registered");
                }
                profileRegistered = false;
                return;
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onServiceConnected mConnectedHeadset registered");
            }
            bluetoothDevice = connectedDevices.get(0);
            if (bluetoothDevice != null) {
                profileRegistered = true;
                return;
            }
            if (DEBUG) {
                MyLog.w(CLS_NAME, "onServiceConnected: mConnectedHeadset null");
            }
            profileRegistered = false;
        }

        @Override
        public void onServiceDisconnected(int profile) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "mHeadsetProfileListener onServiceDisconnected");
            }
            profileRegistered = false;
            switch (SPH.getHeadsetConnectionType(mContext)) {
                case BluetoothConstants.CONNECTION_A2DP:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stopBluetoothAudio: BluetoothConstants.CONNECTION_A2DP");
                    }
                    stopBluetoothVoiceRecognition();
                    break;
                case BluetoothConstants.CONNECTION_SCO:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "stopBluetoothAudio: BluetoothConstants.CONNECTION_SCO");
                    }
                    stopBluetoothSco();
                    break;
                default:
                    break;
            }
        }
    };
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onReceive");
            }
            if (!SPH.isAutoConnectHeadset(mContext)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onReceive: auto connect disabled");
                }
                detachHeadset();
                return;
            }
            final String action = intent.getAction();
            if (!UtilsString.notNaked(action)) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "onReceive: missing action");
                }
                return;
            }

            if (DEBUG) {
                MyLog.i(CLS_NAME, "onReceive: " + action);
            }
            if (SPH.getHeadsetConnectionType(mContext) == BluetoothConstants.CONNECTION_SCO) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "onReceive: BluetoothConstants.CONNECTION_SCO");
                }
            }
            switch (action) {
                case AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onReceive: ACTION_SCO_AUDIO_STATE_UPDATED");
                    }
                    scoState(intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_ERROR),
                            intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_PREVIOUS_STATE, AudioManager.SCO_AUDIO_STATE_ERROR));
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onReceive: ACTION_ACL_CONNECTED");
                    }
                    bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (bluetoothDevice == null && DEBUG) {
                        MyLog.w(CLS_NAME, "connectionState: STATE_CONNECTING: mConnectedHeadset null");
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onReceive: ACTION_ACL_DISCONNECTED");
                    }
                    stopBluetoothSco();
                    break;
                case BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onReceive: ACTION_AUDIO_STATE_CHANGED");
                    }
                    audioState(intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_DISCONNECTED));
                    break;
                case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onReceive: ACTION_CONNECTION_STATE_CHANGED");
                    }
                    connectionState(intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED), intent);
                    break;
                default:
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "onReceive: DEFAULT: " + action);
                    }
                    break;
            }
        }
    };
    private final SaiyCountDownTimer countDownTimer = new SaiyCountDownTimer(10 * 1000, 1000) {
        @Override
        public void onFinish() {
            super.onFinish();
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onComplete");
            }
            switch (SPH.getHeadsetConnectionType(mContext)) {
                case BluetoothConstants.CONNECTION_A2DP:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onComplete: BluetoothConstants.CONNECTION_A2DP");
                    }
                    stopBluetoothVoiceRecognition();
                    break;
                case BluetoothConstants.CONNECTION_SCO:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onComplete: BluetoothConstants.CONNECTION_SCO");
                    }
                    stopBluetoothSco();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {
            super.onTick(millisUntilFinished);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "onTick");
            }
            switch (SPH.getHeadsetConnectionType(mContext)) {
                case BluetoothConstants.CONNECTION_A2DP:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onTicked: BluetoothConstants.CONNECTION_A2DP");
                    }
                    startBluetoothVoiceRecognition();
                    break;
                case BluetoothConstants.CONNECTION_SCO:
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "onTicked: BluetoothConstants.CONNECTION_SCO");
                    }
                    startBluetoothSco();
                    break;
                default:
                    break;
            }
        }
    };

    public BluetoothController(Context context) {
        this.mContext = context;
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    private void audioState(int state) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "audioState");
        }
        switch (state) {
            case BluetoothHeadset.STATE_AUDIO_DISCONNECTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "audioState: STATE_AUDIO_DISCONNECTED");
                }
                break;
            case BluetoothHeadset.STATE_AUDIO_CONNECTING:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "audioState: STATE_AUDIO_CONNECTING");
                }
                break;
            case BluetoothHeadset.STATE_AUDIO_CONNECTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "audioState: STATE_AUDIO_CONNECTED");
                }
                if (SPH.getHeadsetConnectionType(mContext) == BluetoothConstants.CONNECTION_A2DP) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "connectionState: BluetoothConstants.CONNECTION_A2DP");
                    }
                    countDownTimer.stopTick();
                }
                break;
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "audioState: DEFAULT: " + state);
                }
                break;
        }
    }

    private void scoState(int state, int previousState) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "scoState");
        }
        switch (state) {
            case AudioManager.SCO_AUDIO_STATE_ERROR:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "scoState: SCO_AUDIO_STATE_ERROR");
                }
                break;
            case AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "scoState: SCO_AUDIO_STATE_DISCONNECTED");
                }
                stopBluetoothSco();
                break;
            case AudioManager.SCO_AUDIO_STATE_CONNECTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "scoState: SCO_AUDIO_STATE_CONNECTED");
                }
                countDownTimer.stopTick();
                break;
            case AudioManager.SCO_AUDIO_STATE_CONNECTING:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "scoState: SCO_AUDIO_STATE_CONNECTING");
                }
                break;
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "scoState: DEFAULT: " + state);
                }
                break;
        }
        scoPreviousState(previousState);
    }

    private void connectionState(int state, Intent intent) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "connectionState");
        }
        switch (state) {
            case BluetoothProfile.STATE_DISCONNECTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "connectionState: STATE_DISCONNECTED");
                }
                detachHeadset();
                break;
            case BluetoothProfile.STATE_CONNECTING:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "connectionState: STATE_CONNECTING");
                }
                break;
            case BluetoothProfile.STATE_CONNECTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "connectionState: STATE_CONNECTED");
                }
                bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (bluetoothDevice == null) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "connectionState: STATE_CONNECTING: mConnectedHeadset null");
                    }
                } else if (profileRegistered) {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "connectionState: STATE_CONNECTED: profile already registered");
                    }
                } else {
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "connectionState: STATE_CONNECTED: registering profile");
                    }
                    registerProfile();
                }
                break;
            case BluetoothProfile.STATE_DISCONNECTING:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "connectionState: STATE_DISCONNECTING");
                }
                break;
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "connectionState: DEFAULT: " + state);
                }
                break;
        }
    }

    private void scoPreviousState(int state) {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "scoPreviousState");
        }
        switch (state) {
            case AudioManager.SCO_AUDIO_STATE_ERROR:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "scoPreviousState: SCO_AUDIO_STATE_ERROR");
                }
                break;
            case AudioManager.SCO_AUDIO_STATE_DISCONNECTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "scoPreviousState: SCO_AUDIO_STATE_DISCONNECTED");
                }
                break;
            case AudioManager.SCO_AUDIO_STATE_CONNECTED:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "scoPreviousState: SCO_AUDIO_STATE_CONNECTED");
                }
                break;
            case AudioManager.SCO_AUDIO_STATE_CONNECTING:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "scoPreviousState: SCO_AUDIO_STATE_CONNECTING");
                }
                break;
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "scoPreviousState: DEFAULT: " + state);
                }
                break;
        }
    }

    private boolean startBluetoothVoiceRecognition() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startBluetoothVoiceRecognition");
            MyLog.i(CLS_NAME, "startBluetoothVoiceRecognition: isBluetoothA2dpOn: " + audioManager.isBluetoothA2dpOn());
            MyLog.i(CLS_NAME, "startBluetoothVoiceRecognition: isBluetoothScoOn: " + audioManager.isBluetoothScoOn());
        }
        try {
            setAudioMode();
            boolean recognitionStarted;
            if (mConnectedHeadset == null || bluetoothDevice == null) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "startBluetoothVoiceRecognition: mBluetoothHeadset/mConnectedHeadset: null");
                }
                recognitionStarted = false;
            } else {
                recognitionStarted = mConnectedHeadset.startVoiceRecognition(bluetoothDevice);
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "startBluetoothVoiceRecognition: recognitionStarted: " + recognitionStarted);
                }
            }
            if (recognitionStarted) {
                countDownTimer.stopTick();
            }
            return recognitionStarted;
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "startBluetoothVoiceRecognition: Exception");
                e.printStackTrace();
            }
            return false;
        }
    }

    private boolean stopBluetoothVoiceRecognition() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "stopBluetoothVoiceRecognition");
            MyLog.i(CLS_NAME, "stopBluetoothVoiceRecognition: isBluetoothA2dpOn: " + audioManager.isBluetoothA2dpOn());
            MyLog.i(CLS_NAME, "stopBluetoothVoiceRecognition: isBluetoothScoOn: " + audioManager.isBluetoothScoOn());
        }
        try {
            resetAudioMode();
            if (mConnectedHeadset == null || bluetoothDevice == null) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "stopBluetoothVoiceRecognition: mBluetoothHeadset/mConnectedHeadset: null");
                }
                return false;
            }
            boolean recognitionStopped;
            if (mConnectedHeadset.isAudioConnected(bluetoothDevice)) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "stopBluetoothVoiceRecognition: headset remains connected");
                }
                recognitionStopped = mConnectedHeadset.stopVoiceRecognition(bluetoothDevice);
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "stopBluetoothVoiceRecognition: headset no longer connected");
                }
                recognitionStopped = false;
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "stopBluetoothVoiceRecognition: recognitionStopped: " + recognitionStopped);
            }
            return recognitionStopped;
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "stopBluetoothVoiceRecognition: Exception");
                e.printStackTrace();
            }
            return false;
        }
    }

    private boolean startBluetoothSco() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startBluetoothSco");
            MyLog.i(CLS_NAME, "startBluetoothSco: isBluetoothA2dpOn: " + audioManager.isBluetoothA2dpOn());
            MyLog.i(CLS_NAME, "startBluetoothSco: isBluetoothScoOn: " + audioManager.isBluetoothScoOn());
        }
        if (audioManager.isBluetoothScoOn()) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "startBluetoothSco: already connected: returning");
            }
            countDownTimer.stopTick();
            return true;
        }
        try {
            setAudioMode();
            audioManager.startBluetoothSco();
            return true;
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "startBluetoothSco: Exception");
                e.printStackTrace();
            }
            return false;
        }
    }

    private boolean stopBluetoothSco() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "stopBluetoothSco");
            MyLog.i(CLS_NAME, "stopBluetoothSco: isBluetoothA2dpOn: " + audioManager.isBluetoothA2dpOn());
            MyLog.i(CLS_NAME, "stopBluetoothSco: isBluetoothScoOn: " + audioManager.isBluetoothScoOn());
        }
        try {
            resetAudioMode();
            audioManager.stopBluetoothSco();
            return true;
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "stopBluetoothSco: Exception");
                e.printStackTrace();
            }
            return false;
        }
    }

    private void setAudioMode() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "setAudioMode");
            audioMode();
        }
        switch (SPH.getHeadsetStreamType(mContext)) {
            case BluetoothConstants.STREAM_CALL:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setAudioMode: BluetoothConstants.STREAM_CALL");
                }
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                break;
            case BluetoothConstants.STREAM_VOICE_CALL:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setAudioMode: BluetoothConstants.STREAM_VOICE_CALL");
                }
                audioManager.setMode(AudioManager.MODE_NORMAL);
                break;
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "setAudioMode: BluetoothConstants.STREAM_COMMUNICATION");
                }
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                break;
        }
        if (DEBUG) {
            audioMode();
        }
    }

    private void resetAudioMode() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "resetAudioMode");
            audioMode();
        }
        audioManager.setMode(AudioManager.MODE_NORMAL);
        if (DEBUG) {
            audioMode();
        }
    }

    private void audioMode() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "audioMode");
        }
        final int mode = audioManager.getMode();
        switch (mode) {
            case AudioManager.MODE_NORMAL:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "audioMode: MODE_NORMAL");
                }
                break;
            case AudioManager.MODE_CALL_SCREENING:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "audioMode: MODE_CALL_SCREENING");
                }
                break;
            case AudioManager.MODE_RINGTONE:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "audioMode: MODE_RINGTONE");
                }
                return;
            case AudioManager.MODE_IN_CALL:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "audioMode: MODE_IN_CALL");
                }
                break;
            case AudioManager.MODE_IN_COMMUNICATION:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "audioMode: MODE_IN_COMMUNICATION");
                }
                break;
            default:
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "audioMode: DEFAULT: " + mode);
                }
                break;
        }
    }

    private boolean registerReceiver() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "registerReceiver");
        }
        mContext.registerReceiver(broadcastReceiver, new IntentFilter(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED));
        mContext.registerReceiver(broadcastReceiver, new IntentFilter(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED));
        mContext.registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        mContext.registerReceiver(broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        mContext.registerReceiver(broadcastReceiver, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
        return true;
    }

    private boolean registerProfile() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "registerProfile");
        }
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            if (DEBUG) {
                MyLog.w(CLS_NAME, "registerProfile: mBluetoothAdapter null");
            }
            return false;
        }
        if (bluetoothAdapter.getProfileProxy(mContext, mHeadsetProfileListener, BluetoothProfile.HEADSET)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "registerProfile: registered mHeadsetProfileListener: true");
            }
            return true;
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "registerProfile: registered mHeadsetProfileListener: false");
        }
        return false;
    }

    private void detachHeadset() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "detachHeadset");
        }
        countDownTimer.stopTick();
        switch (SPH.getHeadsetConnectionType(mContext)) {
            case BluetoothConstants.CONNECTION_A2DP:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "detachHeadset: BluetoothConstants.CONNECTION_A2DP");
                }
                stopBluetoothVoiceRecognition();
                break;
            case BluetoothConstants.CONNECTION_SCO:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "detachHeadset: BluetoothConstants.CONNECTION_SCO");
                }
                stopBluetoothSco();
                break;
        }
        unregisterProfile();
    }

    private void unregisterReceiver() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "unregisterReceiver");
        }
        if (!receiverRegistered) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "unregisterReceiver: receiverRegistered false");
            }
            return;
        }
        receiverRegistered = false;
        try {
            mContext.unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "unregisterReceiver: IllegalArgumentException");
                e.printStackTrace();
            }
        }
    }

    private void unregisterProfile() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "unregisterProfile");
        }
        if (!profileRegistered) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "unregisterProfile: profileRegistered false");
            }
            return;
        }
        profileRegistered = false;
        if (mConnectedHeadset == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "unregisterProfile: mBluetoothHeadset null");
            }
            return;
        }
        try {
            bluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mConnectedHeadset);
        } catch (Exception e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "unregisterProfile closeProfileProxy Exception");
                e.printStackTrace();
            }
        }
    }

    public boolean startController() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startController");
        }
        if (!SPH.isAutoConnectHeadset(mContext)) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "startController: auto connect disabled");
            }
            receiverRegistered = registerReceiver();
            return false;
        }
        if (audioManager.isBluetoothScoAvailableOffCall()) {
            profileRegistered = registerProfile();
            receiverRegistered = registerReceiver();
            return profileRegistered && receiverRegistered;
        }
        if (DEBUG) {
            MyLog.w(CLS_NAME, "startController: isBluetoothScoAvailableOffCall: false");
        }
        return false;
    }

    public void startBluetoothAudio() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "startBluetoothAudio");
            MyLog.i(CLS_NAME, "startBluetoothAudio: isBluetoothA2dpOn: " + audioManager.isBluetoothA2dpOn());
            MyLog.i(CLS_NAME, "startBluetoothAudio: isBluetoothScoOn: " + audioManager.isBluetoothScoOn());
        }
        if (SPH.getHeadsetSystem(mContext) == BluetoothConstants.SYSTEM_THREE) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "startBluetoothAudio: BluetoothConstants.SYSTEM_THREE");
            }
            if (audioManager.isBluetoothA2dpOn() && audioManager.isBluetoothScoOn()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "startBluetoothAudio: BluetoothConstants.SYSTEM_THREE: already connected: returning");
                }
                return;
            }
        }
        if (profileRegistered && receiverRegistered && bluetoothDevice != null && mConnectedHeadset != null) {
            countDownTimer.startTick();
        } else if (DEBUG) {
            MyLog.i(CLS_NAME, "startBluetoothAudio: not connected");
        }
    }

    public void stopBluetoothAudio() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "stopBluetoothAudio");
            MyLog.i(CLS_NAME, "stopBluetoothAudio: isBluetoothA2dpOn: " + audioManager.isBluetoothA2dpOn());
            MyLog.i(CLS_NAME, "stopBluetoothAudio: isBluetoothScoOn: " + audioManager.isBluetoothScoOn());
        }
        if (!profileRegistered || !receiverRegistered || bluetoothDevice == null || mConnectedHeadset == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "stopBluetoothAudio: not connected");
            }
            return;
        }
        switch (SPH.getHeadsetConnectionType(mContext)) {
            case BluetoothConstants.CONNECTION_A2DP:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "stopBluetoothAudio: BluetoothConstants.CONNECTION_A2DP");
                }
                stopBluetoothVoiceRecognition();
                break;
            case BluetoothConstants.CONNECTION_SCO:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "stopBluetoothAudio: BluetoothConstants.CONNECTION_SCO");
                }
                stopBluetoothSco();
                break;
            default:
                break;
        }
    }

    public void unregister() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "unregister");
        }
        countDownTimer.stopTick();
        switch (SPH.getHeadsetConnectionType(mContext)) {
            case BluetoothConstants.CONNECTION_A2DP:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "unregister: BluetoothConstants.CONNECTION_A2DP");
                }
                stopBluetoothVoiceRecognition();
                break;
            case BluetoothConstants.CONNECTION_SCO:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "unregister: BluetoothConstants.CONNECTION_SCO");
                }
                stopBluetoothSco();
                break;
        }
        unregisterReceiver();
        unregisterProfile();
    }

    public int getTTSAudioStream() {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getTTSAudioStream: isBluetoothA2dpOn: " + audioManager.isBluetoothA2dpOn());
            MyLog.i(CLS_NAME, "getTTSAudioStream: isBluetoothScoOn: " + audioManager.isBluetoothScoOn());
        }
        if (!profileRegistered || !receiverRegistered || bluetoothDevice == null || mConnectedHeadset == null) {
            if (DEBUG) {
                MyLog.i(CLS_NAME, "getTTSAudioStream: STREAM_MUSIC");
            }
            return AudioManager.STREAM_MUSIC;
        }
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getTTSAudioStream: STREAM_VOICE_CALL");
        }
        switch (SPH.getHeadsetStreamType(mContext)) {
            case BluetoothConstants.STREAM_CALL:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getTTSAudioStream: BluetoothConstants.STREAM_CALL");
                }
                return AudioManager.STREAM_RING;
            case BluetoothConstants.STREAM_VOICE_CALL:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getTTSAudioStream: BluetoothConstants.STREAM_VOICE_CALL");
                }
                return AudioManager.STREAM_VOICE_CALL;
            default:
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "getTTSAudioStream: BluetoothConstants.STREAM_COMMUNICATION");
                }
                return AudioManager.STREAM_MUSIC;
        }
    }
}
