package payment.methods;

import java.util.Set;

import payment.core.PaymentException;
import payment.core.PaymentProcessor;
import payment.model.Order;

/**
 * Chuyển khoản ngân hàng (qua mã QR / số tài khoản).
 *  - Phí: cố định 3.300 đ
 *  - Xác thực: mã ngân hàng được hỗ trợ và số tài khoản 8–14 chữ số
 *  - Không yêu cầu OTP tại hệ thống bán hàng (khách xác nhận trên app ngân hàng)
 */
public class BankTransferPayment extends PaymentProcessor {
    private static final Set<String> SUPPORTED_BANKS = Set.of("VCB", "TCB", "BIDV", "ACB", "MB");

    private final String bankCode;
    private final String accountNumber;

    public BankTransferPayment(String bankCode, String accountNumber) {
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
    }

    @Override
    protected String getMethodName() {
        return "Chuyển khoản ngân hàng";
    }

    @Override
    protected long calculateFee(Order order) {
        return 3_300;
    }

    @Override
    protected String authenticate(Order order, long total) throws PaymentException {
        if (bankCode == null || !SUPPORTED_BANKS.contains(bankCode)) {
            throw new PaymentException("Ngân hàng " + bankCode + " chưa được hỗ trợ");
        }
        if (accountNumber == null || !accountNumber.matches("\\d{8,14}")) {
            throw new PaymentException("Số tài khoản không hợp lệ");
        }
        return "TK " + bankCode + " - " + accountNumber;
    }

    @Override
    protected String executeTransaction(Order order, long total) {
        // Giả lập: tạo lệnh chuyển với nội dung "TT <mã đơn>" và nhận xác nhận từ ngân hàng
        return "BANK-" + order.getOrderId();
    }
}
