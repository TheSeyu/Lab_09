package payment.core;

import java.util.ArrayList;
import java.util.List;

import payment.model.Order;
import payment.model.PaymentResult;

/**
 * LỚP TRỪU TƯỢNG – định nghĩa KHUNG THUẬT TOÁN xử lý thanh toán (mẫu Template Method).
 *
 * Quy trình chung (cố định cho mọi phương thức thanh toán):
 *   1. Kiểm tra đơn hàng            – bước chung        (validateOrder)
 *   2. Tính phí giao dịch           – bước thay đổi     (calculateFee)          [abstract]
 *   3. Xác thực phương thức TT      – bước thay đổi     (authenticate)          [abstract]
 *   4. Xác minh OTP (nếu cần)       – bước chung + hook (requiresOtp)
 *   5. Thực hiện giao dịch          – bước thay đổi     (executeTransaction)    [abstract]
 *   6. Lưu giao dịch                – bước chung        (saveTransaction)
 *   7. Gửi thông báo cho khách      – hook, mặc định gửi email (sendNotification)
 */
public abstract class PaymentProcessor {

    /** Hạn mức mặc định cho một giao dịch (VND). */
    protected static final long DEFAULT_MAX_AMOUNT = 500_000_000L;

    // =====================================================================
    //  TEMPLATE METHOD – khai báo final để lớp con KHÔNG thể đổi thứ tự các bước
    // =====================================================================
    public final PaymentResult processPayment(Order order) {
        List<String> steps = new ArrayList<>();
        String method = getMethodName();
        long fee = 0;
        int step = 0;
        try {
            step = 1;
            validateOrder(order);
            steps.add("[1] Kiểm tra đơn hàng ........ OK (" + Money.format(order.getAmount()) + ")");

            step = 2;
            fee = calculateFee(order);
            long total = order.getAmount() + fee;
            steps.add("[2] Tính phí giao dịch ....... " + Money.format(fee)
                    + "  => Tổng thanh toán: " + Money.format(total));

            step = 3;
            String authInfo = authenticate(order, total);
            steps.add("[3] Xác thực ................. OK - " + authInfo);

            step = 4;
            if (requiresOtp()) {
                verifyOtp(order);
                steps.add("[4] Xác minh OTP ............. OK");
            } else {
                steps.add("[4] Xác minh OTP ............. (bỏ qua - phương thức không yêu cầu)");
            }

            step = 5;
            String transactionId = executeTransaction(order, total);
            steps.add("[5] Thực hiện giao dịch ...... OK - Mã GD: " + transactionId);

            step = 6;
            saveTransaction(order, transactionId, total);
            steps.add("[6] Lưu giao dịch ............ OK");

            step = 7;
            steps.add("[7] Gửi thông báo ............ " + sendNotification(order, transactionId));

            return PaymentResult.success(method, order, fee, transactionId, steps);
        } catch (PaymentException e) {
            steps.add("[" + step + "] THẤT BẠI: " + e.getMessage() + " -> dừng quy trình");
            return PaymentResult.failure(method, order, fee, e.getMessage(), steps);
        }
    }

    // =====================================================================
    //  CÁC BƯỚC CHUNG (concrete) – dùng lại cho mọi lớp con
    // =====================================================================
    private void validateOrder(Order order) throws PaymentException {
        if (order == null) {
            throw new PaymentException("Đơn hàng không tồn tại");
        }
        if (order.getOrderId() == null || order.getOrderId().isBlank()) {
            throw new PaymentException("Thiếu mã đơn hàng");
        }
        if (order.getAmount() <= 0) {
            throw new PaymentException("Số tiền đơn hàng phải lớn hơn 0");
        }
        if (order.getAmount() > getMaxAmount()) {
            throw new PaymentException("Vượt hạn mức " + getMethodName() + " ("
                    + Money.format(getMaxAmount()) + ")");
        }
    }

    private void verifyOtp(Order order) throws PaymentException {
        if (!OtpService.verify(order.getOrderId(), getEnteredOtp())) {
            throw new PaymentException("Mã OTP không đúng");
        }
    }

    private void saveTransaction(Order order, String transactionId, long total) {
        TransactionStore.save(String.format("%-10s %-6s %-24s %15s",
                transactionId, order.getOrderId(), getMethodName(), Money.format(total)));
    }

    // =====================================================================
    //  CÁC BƯỚC THAY ĐỔI (abstract) – bắt buộc lớp con cài đặt
    // =====================================================================
    /** Tên phương thức thanh toán, dùng để hiển thị. */
    protected abstract String getMethodName();

    /** Bước 2: tính phí giao dịch theo chính sách của từng phương thức. */
    protected abstract long calculateFee(Order order);

    /** Bước 3: xác thực thông tin thanh toán; trả về mô tả ngắn khi thành công. */
    protected abstract String authenticate(Order order, long total) throws PaymentException;

    /** Bước 5: thực hiện giao dịch với đối tác (ngân hàng, ví, đơn vị vận chuyển...); trả về mã GD. */
    protected abstract String executeTransaction(Order order, long total) throws PaymentException;

    // =====================================================================
    //  HOOK METHODS – có cài đặt mặc định, lớp con CÓ THỂ ghi đè nếu cần
    // =====================================================================
    /** Hook: phương thức có cần xác minh OTP không? Mặc định: không. */
    protected boolean requiresOtp() {
        return false;
    }

    /** Hook: mã OTP người dùng đã nhập. */
    protected String getEnteredOtp() {
        return null;
    }

    /** Hook: hạn mức tối đa của một giao dịch. */
    protected long getMaxAmount() {
        return DEFAULT_MAX_AMOUNT;
    }

    /** Hook: gửi thông báo cho khách. Mặc định: gửi hoá đơn qua email. */
    protected String sendNotification(Order order, String transactionId) {
        return "Đã gửi hoá đơn điện tử tới email " + order.getEmail();
    }
}
