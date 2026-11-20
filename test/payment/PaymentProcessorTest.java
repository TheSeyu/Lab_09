package payment;

import java.util.ArrayList;
import java.util.List;

import payment.core.OtpService;
import payment.core.PaymentProcessor;
import payment.core.TransactionStore;
import payment.methods.BankTransferPayment;
import payment.methods.CodPayment;
import payment.methods.CreditCardPayment;
import payment.methods.EWalletPayment;
import payment.model.Order;
import payment.model.PaymentResult;

/**
 * Bộ kiểm thử đơn giản (không cần thư viện ngoài) cho quy trình thanh toán.
 * Chạy: java -cp out payment.PaymentProcessorTest
 */
public class PaymentProcessorTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("================ KIỂM THỬ PaymentProcessor ================");

        test("TC01 Thẻ hợp lệ + OTP đúng -> thành công, phí 2%", () -> {
            PaymentResult r = new CreditCardPayment("4111111111111111", "A", "12/30",
                    OtpService.generate("T01")).processPayment(order("T01", 1_000_000));
            check(r.isSuccess(), "phải thành công");
            check(r.getFee() == 20_000, "phí phải là 20.000, thực tế " + r.getFee());
            check("CARD-T01".equals(r.getTransactionId()), "sai mã giao dịch");
        });

        test("TC02 Thẻ đơn nhỏ -> áp dụng phí tối thiểu 2.000", () -> {
            PaymentResult r = new CreditCardPayment("4111111111111111", "A", "12/30",
                    OtpService.generate("T02")).processPayment(order("T02", 50_000));
            check(r.getFee() == 2_000, "phí tối thiểu phải là 2.000");
        });

        test("TC03 Thẻ sai số (Luhn) -> thất bại ở bước xác thực", () -> {
            PaymentResult r = new CreditCardPayment("4111111111111112", "A", "12/30",
                    OtpService.generate("T03")).processPayment(order("T03", 500_000));
            check(!r.isSuccess(), "phải thất bại");
            check(r.getMessage().contains("Số thẻ"), "sai lý do: " + r.getMessage());
        });

        test("TC04 Thẻ hết hạn -> thất bại", () -> {
            PaymentResult r = new CreditCardPayment("4111111111111111", "A", "01/20",
                    OtpService.generate("T04")).processPayment(order("T04", 500_000));
            check(!r.isSuccess() && r.getMessage().contains("hết hạn"), "phải báo thẻ hết hạn");
        });

        test("TC05 Thẻ nhập sai OTP -> thất bại, không lưu giao dịch", () -> {
            int before = TransactionStore.findAll().size();
            PaymentResult r = new CreditCardPayment("4111111111111111", "A", "12/30", "999999")
                    .processPayment(order("T05", 500_000));
            check(!r.isSuccess() && r.getMessage().contains("OTP"), "phải báo sai OTP");
            check(TransactionStore.findAll().size() == before, "không được lưu giao dịch lỗi");
        });

        test("TC06 Ví đủ tiền -> thành công, phí 1%, trừ số dư", () -> {
            EWalletPayment wallet = new EWalletPayment("0912345678", 1_000_000,
                    OtpService.generate("T06"));
            PaymentResult r = wallet.processPayment(order("T06", 300_000));
            check(r.isSuccess(), "phải thành công");
            check(r.getFee() == 3_000, "phí phải là 3.000");
            check(wallet.getBalance() == 697_000, "số dư còn lại sai: " + wallet.getBalance());
        });

        test("TC07 Ví không đủ số dư -> thất bại", () -> {
            PaymentResult r = new EWalletPayment("0912345678", 100_000, OtpService.generate("T07"))
                    .processPayment(order("T07", 300_000));
            check(!r.isSuccess() && r.getMessage().contains("Số dư"), "phải báo thiếu số dư");
        });

        test("TC08 Ví vượt hạn mức 20 triệu -> thất bại ở bước kiểm tra đơn", () -> {
            PaymentResult r = new EWalletPayment("0912345678", 100_000_000, OtpService.generate("T08"))
                    .processPayment(order("T08", 25_000_000));
            check(!r.isSuccess() && r.getMessage().contains("hạn mức"), "phải báo vượt hạn mức");
            check(r.getSteps().size() == 1, "phải dừng ngay ở bước 1");
        });

        test("TC09 Chuyển khoản -> thành công, phí cố định 3.300, không cần OTP", () -> {
            PaymentResult r = new BankTransferPayment("TCB", "19034567890")
                    .processPayment(order("T09", 12_000_000));
            check(r.isSuccess(), "phải thành công");
            check(r.getFee() == 3_300, "phí phải là 3.300");
            check(r.getSteps().get(3).contains("bỏ qua"), "bước OTP phải được bỏ qua");
        });

        test("TC10 Chuyển khoản ngân hàng không hỗ trợ -> thất bại", () -> {
            PaymentResult r = new BankTransferPayment("XYZ", "19034567890")
                    .processPayment(order("T10", 1_000_000));
            check(!r.isSuccess() && r.getMessage().contains("chưa được hỗ trợ"), "sai lý do");
        });

        test("TC11 COD -> thành công, phí 25.000, thông báo qua SMS (hook)", () -> {
            PaymentResult r = new CodPayment().processPayment(order("T11", 2_000_000));
            check(r.isSuccess(), "phải thành công");
            check(r.getFee() == 25_000, "phí phải là 25.000");
            check(r.getSteps().get(6).contains("SMS"), "COD phải gửi SMS thay vì email");
        });

        test("TC12 COD vượt hạn mức 5 triệu (hook getMaxAmount) -> thất bại", () -> {
            PaymentResult r = new CodPayment().processPayment(order("T12", 7_500_000));
            check(!r.isSuccess() && r.getMessage().contains("hạn mức"), "phải báo vượt hạn mức");
        });

        test("TC13 Đơn số tiền 0 -> mọi phương thức đều từ chối ở bước chung", () -> {
            Order bad = order("T13", 0);
            for (PaymentProcessor p : List.of(new CodPayment(),
                    new BankTransferPayment("VCB", "12345678"),
                    new EWalletPayment("0912345678", 1_000, "x"),
                    new CreditCardPayment("4111111111111111", "A", "12/30", "x"))) {
                PaymentResult r = p.processPayment(bad);
                check(!r.isSuccess() && r.getMessage().contains("lớn hơn 0"),
                        p.getClass().getSimpleName() + " không từ chối đơn 0 đ");
            }
        });

        test("TC14 Template Method luôn gọi các bước ĐÚNG THỨ TỰ", () -> {
            SpyProcessor spy = new SpyProcessor();
            PaymentResult r = spy.processPayment(order("T14", 100_000));
            check(r.isSuccess(), "phải thành công");
            check(spy.calls.equals(List.of("fee", "auth", "otp?", "otp", "execute", "notify")),
                    "thứ tự gọi sai: " + spy.calls);
            check(r.getSteps().size() == 7, "phải đủ 7 bước");
        });

        System.out.println("-----------------------------------------------------------");
        System.out.printf("KẾT QUẢ: %d đạt, %d lỗi / tổng %d ca kiểm thử%n",
                passed, failed, passed + failed);
        if (failed > 0) System.exit(1);
    }

    // ------------------------------------------------------------------
    /** Lớp con "gián điệp" ghi lại thứ tự các bước mà Template Method gọi. */
    static class SpyProcessor extends PaymentProcessor {
        final List<String> calls = new ArrayList<>();
        @Override protected String getMethodName() { return "Spy"; }
        @Override protected long calculateFee(Order o) { calls.add("fee"); return 0; }
        @Override protected String authenticate(Order o, long t) { calls.add("auth"); return "spy"; }
        @Override protected boolean requiresOtp() { calls.add("otp?"); return true; }
        @Override protected String getEnteredOtp() { calls.add("otp"); return OtpService.generate("T14"); }
        @Override protected String executeTransaction(Order o, long t) { calls.add("execute"); return "SPY"; }
        @Override protected String sendNotification(Order o, String tx) { calls.add("notify"); return "ok"; }
    }

    interface TestBody { void run() throws Exception; }

    private static void test(String name, TestBody body) {
        try {
            body.run();
            passed++;
            System.out.println("[ĐẠT] " + name);
        } catch (AssertionError | Exception e) {
            failed++;
            System.out.println("[LỖI] " + name + " -> " + e.getMessage());
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static Order order(String id, long amount) {
        return new Order(id, "Khách test", "test@example.com", "0900000000", "1 Đường A", amount);
    }
}
