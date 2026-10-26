import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Диалоговое окно для ввода параметров кредита (View).
 */
public class InputDialog extends Stage {

    private final TextField tfRate = new TextField();
    private final TextField tfAmount = new TextField();
    private final TextField tfTerm = new TextField();

    private boolean confirmed = false;

    public InputDialog(Window owner) {
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);
        setTitle("Ввод параметров кредита");
        setResizable(false);

        VBox root = new VBox(16);
        root.setPadding(new Insets(20, 25, 20, 25));
        root.getStyleClass().add("dialog-pane");

        Label title = new Label("Ввод параметров кредита");
        title.getStyleClass().add("dialog-title");
        Label subtitle = new Label("Заполните ставку, сумму и срок кредитования");
        subtitle.getStyleClass().add("dialog-subtitle");

        tfRate.setPromptText("например: 12.5");
        tfAmount.setPromptText("например: 150000");
        tfTerm.setPromptText("например: 120");

        tfRate.getStyleClass().add("custom-text-field");
        tfAmount.getStyleClass().add("custom-text-field");
        tfTerm.getStyleClass().add("custom-text-field");

        VBox fieldsBox = new VBox(12,
                createFieldBox("Процентная ставка (% годовых):", tfRate),
                createFieldBox("Сумма кредита (руб.):", tfAmount),
                createFieldBox("Срок кредита (в месяцах):", tfTerm)
        );

        Button btnSave = new Button("Рассчитать");
        btnSave.getStyleClass().add("btn-primary");
        btnSave.setDefaultButton(true);

        Button btnCancel = new Button("Отмена");
        btnCancel.getStyleClass().add("btn-secondary");

        btnSave.setOnAction(e -> {
            confirmed = true;
            close();
        });

        btnCancel.setOnAction(e -> {
            confirmed = false;
            close();
        });

        HBox buttonsBox = new HBox(10, btnCancel, btnSave);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(new VBox(4, title, subtitle), fieldsBox, buttonsBox);

        Scene scene = new Scene(root, 380, 360);
        if (getClass().getResource("style.css") != null) {
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        }
        setScene(scene);
    }

    private VBox createFieldBox(String labelText, TextField field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("input-label");
        return new VBox(4, label, field);
    }

    public void setInitialValues(String rate, String amount, String term) {
        tfRate.setText(rate != null ? rate : "");
        tfAmount.setText(amount != null ? amount : "");
        tfTerm.setText(term != null ? term : "");
    }

    public boolean isConfirmed() { return confirmed; }
    public String getRateText() { return tfRate.getText(); }
    public String getAmountText() { return tfAmount.getText(); }
    public String getTermText() { return tfTerm.getText(); }
}
