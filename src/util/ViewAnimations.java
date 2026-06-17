package util;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public final class ViewAnimations {
    private ViewAnimations() {
    }

    public static void fadeSlideIn(Node node) {
        node.setOpacity(0);
        node.setTranslateY(12);

        FadeTransition fade = new FadeTransition(Duration.millis(260), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        TranslateTransition slide = new TranslateTransition(Duration.millis(260), node);
        slide.setFromY(12);
        slide.setToY(0);
        slide.play();
    }
}
