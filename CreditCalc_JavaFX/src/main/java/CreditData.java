/**
 * Класс данных кредита (<<Data>> в архитектуре MVC).
 * Хранит входные параметры и результаты расчёта.
 */
public class CreditData {
    public double rate;           // процентная ставка (% годовых)
    public double amount;         // сумма кредита (руб.)
    public int termMonths;        // срок кредита (в месяцах)
    public double monthlyPayment; // ежемесячный платёж
    public double overpayRatio;   // во сколько раз переплата банку
    public double totalPayment;   // общая сумма выплат

    public CreditData() {}

    public CreditData(double rate, double amount, int termMonths) {
        this.rate = rate;
        this.amount = amount;
        this.termMonths = termMonths;
    }
}
