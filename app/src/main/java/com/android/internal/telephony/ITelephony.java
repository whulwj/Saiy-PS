package com.android.internal.telephony;

import android.os.IInterface;
import android.os.RemoteException;

public interface ITelephony extends IInterface {
    void answerRingingCall() throws RemoteException;

    boolean endCall() throws RemoteException;

    void silenceRinger() throws RemoteException;
}
