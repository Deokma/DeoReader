package by.popolamov.deoreader.views.impl;

import by.popolamov.deoreader.views.MainView;
import javafx.animation.Transition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class MainViewImpl implements MainView {
    @FXML
    private Pane leftPane;

    @FXML
    private Label label1;

    @FXML
    private Label label2;

    @FXML
    private Label label3;
    @FXML
    private Label label4;
    @FXML
    private Label label5;

    private boolean isExpanded = false;

    @FXML
    public void expandLeftPane() {
        double startWidth = leftPane.getWidth();
        double targetWidth = isExpanded ? 50.0 : 170.0;

        Transition transition = new Transition() {
            {
                setCycleDuration(Duration.seconds(0.2)); // Длительность анимации (в секундах)
            }

            @Override
            protected void interpolate(double frac) {
                double width = startWidth + (targetWidth - startWidth) * frac;
                leftPane.setPrefWidth(width);
            }
        };

        transition.play();

        if (!isExpanded) {
            label1.setPadding(new Insets(0, 0, 0, 10)); // Изменение отступов при расширении
            label2.setPadding(new Insets(0, 0, 0, 10));
            label3.setPadding(new Insets(0, 0, 0, 10));
            label4.setPadding(new Insets(0, 0, 0, 10));
            label5.setPadding(new Insets(0, 0, 0, 10));
        } else {
            label1.setPadding(new Insets(0)); // Изменение отступов при сжатии
            label2.setPadding(new Insets(0));
            label3.setPadding(new Insets(0));
            label4.setPadding(new Insets(0));
            label5.setPadding(new Insets(0));
        }

        label1.setVisible(!isExpanded);
        label2.setVisible(!isExpanded);
        label3.setVisible(!isExpanded);
        label4.setVisible(!isExpanded);
        label5.setVisible(!isExpanded);

        isExpanded = !isExpanded;
    }
}
