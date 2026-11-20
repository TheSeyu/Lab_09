package payment.methods;

import payment.core.PaymentException;
import payment.core.PaymentProcessor;
import payment.model.Order;

/**
 * Thanh toán khi nhận hàng (COD – Cash On Delivery).
 *  - Phí: phí thu hộ cố định 25.000 đ
 *  - Xác thực: có địa chỉ giao hàng và số điện thoại người nhận hợp lệ
 *  - Hạn mức COD: 5.000.000 đ; thông báo bằng SMS thay vì email
 */
public class CodPayment extends PaymentProcessor {

    @Override
    protected String getMethodName() {
        return "Thanh toán khi nhận hàng";
    }

    @Override
    protected long calculateFee(Order order) {
        return 25_000;
    }

    @Override
    protected String authenticate(Order order, long total) throws PaymentException {
        if (order.getShippingAddress() == null || order.getShippingAddress().isBlank()) {
            throw new PaymentException("Thiếu địa chỉ giao hàng");
        }
        if (order.getPhone() == null || !order.getPhone().matches("0\\d{9}")) {
            throw new PaymentException("Số điện thoại người nhận không hợp lệ");
        }
        return "Giao tới: " + order.getShippingAddress();
    }

    @Override
    protected String executeTransaction(Order order, long total) {
        // Giả lập: tạo vận đơn, shipper sẽ thu tiền khi giao
        return "COD-" + order.getOrderId();
    }

    // ---- Hook được ghi đè ----
    @Override
    protected long getMaxAmount() {
        return 5_000_000L;
    }

    @Override
    protected String sendNotification(Order order, String transactionId) {
        return "Đã gửi SMS tới " + order.getPhone() + ": chuẩn bị tiền mặt khi nhận hàng";
    }
}
