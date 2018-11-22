package io.raspberrywallet.manager;

import com.stasbar.Logger;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.IntConsumer;

public class AutoLockTimer extends TimerTask {
    private int autoLockRemainingSeconds;
    private final int autoLockInitialSeconds;
    @NotNull
    private final Runnable onLockTriggered;
    @NotNull
    final private Timer timer;

    @Nullable
    @Setter
    private IntConsumer autoLockChannelListener;

    AutoLockTimer(int autoLockRemainingSeconds, @NotNull Timer timer, @NotNull Runnable onLockTriggered) {
        this.autoLockRemainingSeconds = autoLockRemainingSeconds;
        this.autoLockInitialSeconds = autoLockRemainingSeconds;
        this.onLockTriggered = onLockTriggered;
        this.timer = timer;
    }

    @Override
    public void run() {
        if (autoLockRemainingSeconds == 1) {
            Logger.d("Autolock triggered");
            onLockTriggered.run();
            timer.cancel();
        }
        --autoLockRemainingSeconds;

        if (autoLockChannelListener != null)
            autoLockChannelListener.accept(autoLockRemainingSeconds);
    }

    public void tap() {
        autoLockRemainingSeconds = autoLockInitialSeconds;
    }
}
