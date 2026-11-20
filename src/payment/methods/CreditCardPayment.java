package payment.methods;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import payment.core.PaymentException;
import payment.core.PaymentProcessor;
import payment.model.Order;

/**
 * Thanh toán bằng thẻ tín dụng / ghi nợ quốc tế.
 *  - Phí: 2% giá trị đơn, tối thiểu 2.000 đ
 *  - Xác thực: số thẻ hợp lệ theo thuật toán Luhn và thẻ chưa hết hạn
 *  - Có yêu cầu OTP (3-D Secure)
 */
public class CreditCardPayment extends PaymentProcessor {
    private static final DateTimeFormatter EXPIRY = DateTimeFormatter.ofPattern("MM/yy");

    private final String cardNumber;
    private final String cardHolder;
    private final String expiry;      // dạng MM/yy
    private final String enteredOtp;

    public CreditCardPayment(String cardNumber, String cardHolder, String expiry, String enteredOtp) {
        this.cardNumber = cardNumber == null ? "" : cardNumber.replace(" ", "");
        this.cardHolder = cardHolder;
        this.expiry = expiry;
        this.enteredOtp = enteredOtp;
    }

    @Override
    protected String getMethodName() {
        return "Thẻ tín dụng";
    }

    @Override
    protected long calculateFee(Order order) {
        return Math.max(2_000, Math.round(order.getAmount() * 0.02));
    }

    @Override
    protected String authenticate(Order order, long total) throws PaymentException {
        if (!cardNumber.matches("\\d{13,19}") || !passesLuhn(cardNumber)) {
            throw new PaymentException("Số thẻ không hợp lệ");
        }
        YearMonth exp;
        try {
            exp = YearMonth.parse(expiry, EXPIRY);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new PaymentException("Ngày hết hạn không đúng định dạng MM/yy");
        }
        if (exp.isBefore(YearMonth.now())) {
            throw new PaymentException("Thẻ đã hết hạn");
        }
        return "Thẻ " + mask() + " của " + cardHolder;
    }

    @Override
    protected String executeTransaction(Order order, long total) {
        // Giả lập gọi cổng thanh toán thẻ
        return "CARD-" + order.getOrderId();
    }

    // ---- Hook được ghi đè ----
    @Override
    protected boolean requiresOtp() {
        return true;
    }

    @Override
    protected String getEnteredOtp() {
        return enteredOtp;
    }

    // ---- Hàm phụ ----
    private String mask() {
        return "**** " + cardNumber.substring(cardNumber.length() - 4);
    }

    static boolean passesLuhn(String digits) {
        int sum = 0;
        boolean doubleIt = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            if (doubleIt) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            sum += d;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }
}
