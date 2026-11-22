package com.ahmed.mousekeyboardhud.animation;

/**
 * Tiny allocation-free progress tracker: progress moves linearly toward 0/1 over a duration,
 * and the eased value is derived on demand.
 */
public final class Tween {
    private float progress;

    public float progress() {
        return progress;
    }

    public void set(float p) {
        progress = p;
    }

    /** Advances toward the target (0 or 1). durationSeconds <= 0 snaps instantly. */
    public void update(boolean target, float dt, float durationSeconds) {
        float goal = target ? 1f : 0f;
        if (durationSeconds <= 0f) {
            progress = goal;
            return;
        }
        float step = dt / durationSeconds;
        if (progress < goal) progress = Math.min(goal, progress + step);
        else if (progress > goal) progress = Math.max(goal, progress - step);
    }

    public float eased(Easing easing) {
        return easing.apply(progress);
    }
}
