import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Главное окно (Controller + View в архитектуре MVC).
 * Отображает карточки данных, подписывается на события CreditModel и открывает диалог ввода.
 */
public class MainWindow extends Stage implements PropertyChangeListener {

    private final CreditModel model;

    // Отображение входных параметров
    private final Label lblRate = new Label("—");
    private final Label lblAmount = new Label("—");
    private final Label lblTerm = new Label("—");

    // Отображение результатов расчёта
    private final Label lblMonthlyPayment = new Label("0.00 руб.");
    private final Label lblOverpayRatio = new Label("—");
    private final Label lblTotalPayment = new Label("Итого выплат: —");
    private final ProgressBar progressOverpay = new ProgressBar(0.0);
    private final Label lblStatusText = new Label("Нажмите «Ввести данные» для расчёта");

    // Запоминание последних введённых данных (по условию задания)
    private String lastRate = "12.5";
    private String lastAmount = "150000";
    private String lastTerm = "120";

    public MainWindow(CreditModel model) {
        this.model = model;
        this.model.addPropertyChangeListener(this); // Подписка на активную модель

        setTitle("Кредит на жильё");
        setMinWidth(700);
        setMinHeight(480);

        initUI();
    }

    private void initUI() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25, 30, 25, 30));
        root.getStyleClass().add("root-pane");

        // Заголовок
        Label icon = new Label("🏠");
        icon.getStyleClass().add("app-icon");
        Label title = new Label("Кредит на жильё");
        title.getStyleClass().add("app-title");
        HBox titleBox = new HBox(10, icon, title);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label subtitle = new Label("Расчёт ежемесячного платежа и коэффициента переплаты");
        subtitle.getStyleClass().add("app-subtitle");

        // Карточка 1: Параметры кредита
        VBox cardInputs = new VBox(12);
        cardInputs.getStyleClass().add("card");
        HBox.setHgrow(cardInputs, Priority.ALWAYS);

        Label headerInputs = new Label("ПАРАМЕТРЫ КРЕДИТА");
        headerInputs.getStyleClass().add("card-header");

        cardInputs.getChildren().addAll(
                headerInputs,
                createSubCard("Процентная ставка", lblRate),
                createSubCard("Сумма кредита", lblAmount),
                createSubCard("Срок кредита", lblTerm)
        );

        // Карточка 2: Результаты расчёта
        VBox cardResults = new VBox(12);
        cardResults.getStyleClass().addAll("card", "card-highlight");
        HBox.setHgrow(cardResults, Priority.ALWAYS);

        Label headerResults = new Label("ОЦЕНКА РАСХОДОВ");
        headerResults.getStyleClass().add("card-header");

        Label lblMonthlyTitle = new Label("Ежемесячный платёж");
        lblMonthlyTitle.getStyleClass().add("item-title");
        lblMonthlyPayment.getStyleClass().add("hero-value");

        lblOverpayRatio.getStyleClass().add("ratio-value");
        HBox ratioBox = new HBox(6, new Label("Переплата банку:"), lblOverpayRatio, new Label("раз(а)"));
        ratioBox.setAlignment(Pos.CENTER_LEFT);

        progressOverpay.setMaxWidth(Double.MAX_VALUE);
        progressOverpay.getStyleClass().add("custom-progress");
        lblStatusText.getStyleClass().add("status-label");
        lblTotalPayment.getStyleClass().add("total-label");

        cardResults.getChildren().addAll(
                headerResults,
                new VBox(4, lblMonthlyTitle, lblMonthlyPayment),
                ratioBox,
                progressOverpay,
                lblStatusText,
                lblTotalPayment
        );

        HBox cardsBox = new HBox(20, cardInputs, cardResults);
        VBox.setVgrow(cardsBox, Priority.ALWAYS);

        // Кнопка "Ввести данные"
        Button btnEnterData = new Button("Ввести данные →");
        btnEnterData.getStyleClass().add("btn-primary");
        btnEnterData.setOnAction(e -> openInputDialog());

        HBox bottomBox = new HBox(btnEnterData);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(new VBox(4, titleBox, subtitle), cardsBox, bottomBox);

        Scene scene = new Scene(root, 740, 500);
        if (getClass().getResource("style.css") != null) {
            scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        }
        setScene(scene);
    }

    private VBox createSubCard(String title, Label valueLabel) {
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("item-title");
        valueLabel.getStyleClass().add("item-value");
        VBox box = new VBox(2, lblTitle, valueLabel);
        box.getStyleClass().add("sub-card");
        return box;
    }

    private void openInputDialog() {
        InputDialog dialog = new InputDialog(this);
        // Восстанавливаем последние введённые данные
        dialog.setInitialValues(lastRate, lastAmount, lastTerm);
        dialog.showAndWait();

        if (dialog.isConfirmed()) {
            // Запоминаем введённые значения
            lastRate = dialog.getRateText();
            lastAmount = dialog.getAmountText();
            lastTerm = dialog.getTermText();

            // Передаём в модель для расчёта (активная модель уведомит нас)
            model.calculate(lastRate, lastAmount, lastTerm);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            if ("dataChanged".equals(evt.getPropertyName())) {
                lblRate.setText(String.format("%.2f %%", model.getRate()));
                lblAmount.setText(String.format("%,.2f руб.", model.getAmount()));
                lblTerm.setText(String.format("%d мес. (%.1f г.)", model.getTermMonths(), model.getTermMonths() / 12.0));

                lblMonthlyPayment.setText(String.format("%,.2f руб.", model.getMonthlyPayment()));
                lblOverpayRatio.setText(String.format("%.2f", model.getOverpayRatio()));
                lblTotalPayment.setText(String.format("Итого выплат: %,.2f руб.", model.getTotalPayment()));

                double ratio = model.getOverpayRatio();
                progressOverpay.setProgress(Math.min(1.0, Math.max(0.1, (ratio - 1.0) / 2.0)));

                if (ratio < 1.3) {
                    lblStatusText.setText("Низкая переплата (выгодные условия)");
                    lblStatusText.setStyle("-fx-text-fill: #00e5a3;");
                } else if (ratio < 2.0) {
                    lblStatusText.setText("Умеренная переплата");
                    lblStatusText.setStyle("-fx-text-fill: #ffbc00;");
                } else {
                    lblStatusText.setText("Высокая переплата из-за срока или ставки");
                    lblStatusText.setStyle("-fx-text-fill: #ff5e5e;");
                }
            } else if ("error".equals(evt.getPropertyName())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка ввода данных");
                alert.setHeaderText("Некорректные параметры");
                alert.setContentText((String) evt.getNewValue());
                try {
                    alert.getDialogPane().getStylesheets().add(getClass().getResource("style.css").toExternalForm());
                    alert.getDialogPane().getStyleClass().add("alert-dark");
                } catch (Exception ignored) {}
                alert.showAndWait();
            }
        });
    }
}
