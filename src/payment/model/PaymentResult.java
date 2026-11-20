package payment.model;

import java.util.Collections;
import java.util.List;

/**
 * Kết quả của một lần xử lý thanh toán, kèm nhật ký từng bước đã chạy.
 */
public class PaymentResult {
    private final boolean success;
    private final String method;
    private final String orderId;
    private final long amount;
    private final long fee;
    private final String transactionId;
    private final String message;
    private final List<String> steps;

    private PaymentResult(boolean success, String method, String orderId, long amount, long fee,
                          String transactionId, String message, List<String> steps) {
        this.success = success;
        this.method = method;
        this.orderId = orderId;
        this.amount = amount;
        this.fee = fee;
        this.transactionId = transactionId;
        this.message = message;
        this.steps = Collections.unmodifiableList(steps);
    }

    public static PaymentResult success(String method, Order order, long fee,
                                        String transactionId, List<String> steps) {
        return new PaymentResult(true, method, order.getOrderId(), order.getAmount(), fee,
                transactionId, "Thanh toán thành công", steps);
    }

    public static PaymentResult failure(String method, Order order, long fee,
                                        String reason, List<String> steps) {
        String id = order == null ? "?" : order.getOrderId();
        long amount = order == null ? 0 : order.getAmount();
        return new PaymentResult(false, method, id, amount, fee, null, reason, steps);
    }

    public boolean isSuccess()       { return success; }
    public String getMethod()        { return method; }
    public String getOrderId()       { return orderId; }
    public long getAmount()          { return amount; }
    public long getFee()             { return fee; }
    public long getTotal()           { return amount + fee; }
    public String getTransactionId() { return transactionId; }
    public String getMessage()       { return message; }
    public List<String> getSteps()   { return steps; }
}
