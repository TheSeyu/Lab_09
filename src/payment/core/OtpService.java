package payment.core;

/**
 * Giả lập dịch vụ OTP: sinh mã 6 chữ số cho mỗi đơn hàng và kiểm tra mã người dùng nhập.
 * (Trong hệ thống thật, mã được gửi qua SMS và có thời hạn.)
 */
public final class OtpService {
    private OtpService() { }

    public static String generate(String orderId) {
        long seed = orderId.hashCode() * 31L + 7;
        return String.format("%06d", Math.floorMod(seed, 1_000_000));
    }

    public static boolean verify(String orderId, String enteredCode) {
        return enteredCode != null && enteredCode.equals(generate(orderId));
    }
}
