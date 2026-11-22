package com.ahmed.mousekeyboardhud.animation;

/** Easing curves applied to normalized animation progress (0..1). */
public enum Easing {
    LINEAR("Linear"),
    EASE_IN("Ease In"),
    EASE_OUT("Ease Out"),
    EASE_IN_OUT("Ease In-Out");

    private final String label;

    Easing(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public float apply(float t) {
        if (t <= 0f) return 0f;
        if (t >= 1f) return 1f;
        return switch (this) {
            case LINEAR -> t;
            case EASE_IN -> t * t * t;
            case EASE_OUT -> {
                float u = 1f - t;
                yield 1f - u * u * u;
            }
            case EASE_IN_OUT -> t < 0.5f ? 4f * t * t * t : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
        };
    }
}
