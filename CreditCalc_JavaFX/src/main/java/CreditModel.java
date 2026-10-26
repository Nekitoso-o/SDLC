import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Модель кредита (<<Active Model>> в архитектуре MVC).
 * Отвечает за хранение объекта данных CreditData, валидацию и математический расчёт.
 * Самостоятельно уведомляет View через PropertyChangeSupport.
 */
public class CreditModel {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    // Объект данных (хранит параметры и результаты)
    private CreditData m_data = new CreditData();

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    // Геттер объекта данных
    public CreditData data() {
        return m_data;
    }

    // Совместимые геттеры для удобного обращения
    public double getRate() { return m_data.rate; }
    public double getAmount() { return m_data.amount; }
    public int getTermMonths() { return m_data.termMonths; }
    public double getMonthlyPayment() { return m_data.monthlyPayment; }
    public double getOverpayRatio() { return m_data.overpayRatio; }
    public double getTotalPayment() { return m_data.totalPayment; }

    /**
     * Валидация входных данных и расчёт аннуитетного кредита.
     */
    public void calculate(String rateStr, String amountStr, String termStr) {
        if (rateStr == null || rateStr.trim().isEmpty() ||
            amountStr == null || amountStr.trim().isEmpty() ||
            termStr == null || termStr.trim().isEmpty()) {
            notifyError("Пожалуйста, заполните все поля ввода!");
            return;
        }

        double r;
        double a;
        int t;

        try {
            r = Double.parseDouble(rateStr.trim().replace(',', '.'));
            a = Double.parseDouble(amountStr.trim().replace(',', '.'));
            t = Integer.parseInt(termStr.trim());
        } catch (NumberFormatException e) {
            notifyError("Некорректный формат! Введите числа (срок — целое число).");
            return;
        }

        if (r <= 0 || a <= 0 || t <= 0) {
            notifyError("Все значения (ставка, сумма, срок) должны быть больше 0!");
            return;
        }

        m_data.rate = r;
        m_data.amount = a;
        m_data.termMonths = t;

        // Расчёт аннуитета: P = S * (m * (1 + m)^n) / ((1 + m)^n - 1)
        double monthlyRate = (r / 100.0) / 12.0;
        double factor = Math.pow(1.0 + monthlyRate, t);
        m_data.monthlyPayment = a * (monthlyRate * factor) / (factor - 1.0);

        m_data.totalPayment = m_data.monthlyPayment * t;
        m_data.overpayRatio = m_data.totalPayment / a;

        // Активная модель: уведомляет слушателей об изменении
        support.firePropertyChange("dataChanged", null, this);
    }

    private void notifyError(String message) {
        support.firePropertyChange("error", null, message);
    }
}
