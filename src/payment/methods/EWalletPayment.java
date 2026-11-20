package payment.methods;

import payment.core.Money;
import payment.core.PaymentException;
import payment.core.PaymentProcessor;
import payment.model.Order;

/**
 * Thanh toán bằng ví điện tử.
 *  - Phí: 1% giá trị đơn
 *  - Xác thực: số điện thoại ví hợp lệ và số dư đủ để thanh toán
 *  - Có yêu cầu OTP; hạn mức 20.000.000 đ / giao dịch
 */
public class EWalletPayment extends PaymentProcessor {
    private final String walletPhone;
    private long balance;
    private final String enteredOtp;

    public EWalletPayment(String walletPhone, long balance, String enteredOtp) {
        this.walletPhone = walletPhone;
        this.balance = balance;
        this.enteredOtp = enteredOtp;
    }

    @Override
    protected String getMethodName() {
        return "Ví điện tử";
    }

    @Override
    protected long calculateFee(Order order) {
        return Math.round(order.getAmount() * 0.01);
    }

    @Override
    protected String authenticate(Order order, long total) throws PaymentException {
        if (walletPhone == null || !walletPhone.matches("0\\d{9}")) {
            throw new PaymentException("Số điện thoại ví không hợp lệ");
        }
        if (balance < total) {
            throw new PaymentException("Số dư ví không đủ (còn " + Money.format(balance) + ")");
        }
        return "Ví " + walletPhone + ", số dư " + Money.format(balance);
    }

    @Override
    protected String executeTransaction(Order order, long total) {
        balance -= total; // trừ tiền trong ví
        return "EWL-" + order.getOrderId();
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

    @Override
    protected long getMaxAmount() {
        return 20_000_000L;
    }

    public long getBalance() {
        return balance;
    }
}
