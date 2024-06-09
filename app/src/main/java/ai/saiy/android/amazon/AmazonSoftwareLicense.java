package ai.saiy.android.amazon;

import android.content.Context;

import ai.saiy.android.R;
import de.psdev.licensesdialog.licenses.License;

public class AmazonSoftwareLicense extends License {
    @Override
    public String getName() {
        return "Amazon Software License";
    }

    @Override
    public String getUrl() {
        return "https://aws.amazon.com/asl/";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String readFullTextFromResources(Context context) {
        return getContent(context, R.raw.amazon_sl_full);
    }

    @Override
    public String readSummaryTextFromResources(Context context) {
        return getContent(context, R.raw.amazon_sl_summary);
    }
}
