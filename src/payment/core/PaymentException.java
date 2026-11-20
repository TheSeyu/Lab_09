package payment.core;

/**
 * Lỗi nghiệp vụ phát sinh ở một bước bất kỳ của quy trình thanh toán.
 */
public class PaymentException extends Exception {
    private static final long serialVersionUID = 1L;

    public PaymentException(String message) {
        super(message);
    }
}
