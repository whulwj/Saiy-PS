package ai.saiy.android.algorithms;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.concurrent.Callable;

import ai.saiy.android.custom.CustomCommand;
import ai.saiy.android.nlu.local.AlgorithmicContainer;

public interface Resolvable {
    @NonNull Callable<AlgorithmicContainer> genericCallable();
    default @NonNull Callable<List<AlgorithmicContainer>> contactCallable() {
        return new Callable<List<AlgorithmicContainer>>() {
            @Override
            public List<AlgorithmicContainer> call() {
                return null;
            }
        };
    }
    @NonNull Callable<CustomCommand> customCommandCallable();
}
