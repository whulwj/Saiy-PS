package ai.saiy.android.diagnostic;

public interface DiagnosticInfoListener {
    void onComplete(DiagnosticsInfo diagnosticsInfo, boolean forceStop);

    void appendDiagnosticInfo(String str);

    void setDiagnosticInfo(String str);

    void replaceDiagnosticInfo(String str);

    void setASRCount(String str);

    void setTTSCount(String str);

    void setErrorCount(String str);

    void setPassedCount(String str);
}
