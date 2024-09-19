package ai.saiy.android.command.financial;

import java.util.List;

public class ResultSet {
    @com.google.gson.annotations.SerializedName("Result")
    private final List<Company> result;

    public ResultSet(List<Company> result) {
        this.result = result;
    }

    public List<Company> getResult() {
        return this.result;
    }
}
