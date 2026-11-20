package payment.app;

import payment.core.Money;
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
 * Chương trình minh hoạ: chạy cùng một quy trình processPayment() với
 * 4 phương thức thanh toán khác nhau, gồm cả trường hợp thành công và thất bại.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   HỆ THỐNG XỬ LÝ THANH TOÁN ĐƠN HÀNG - MẪU TEMPLATE METHOD");
        System.out.println("=================================================================");

        Order o1 = new Order("DH001", "Nguyễn Văn An", "an@example.com", "0901234567",
                "12 Lê Lợi, Q.1, TP.HCM", 1_250_000);
        Order o2 = new Order("DH002", "Trần Thị Bình", "binh@example.com", "0912345678",
                "45 Trần Phú, Hà Đông, Hà Nội", 450_000);
        Order o3 = new Order("DH003", "Lê Minh Châu", "chau@example.com", "0987654321",
                "8 Nguyễn Huệ, TP. Huế", 12_000_000);
        Order o4 = new Order("DH004", "Phạm Quốc Dũng", "dung@example.com", "0933111222",
                "21 Hùng Vương, TP. Đà Nẵng", 3_200_000);
        Order o5 = new Order("DH005", "Võ Thị Em", "em@example.com", "0977888999",
                "99 Cách Mạng Tháng 8, Cần Thơ", 7_500_000);

        // ---- Các trường hợp THÀNH CÔNG ----
        run("Thẻ tín dụng hợp lệ, nhập đúng OTP", o1,
                new CreditCardPayment("4111 1111 1111 1111", "NGUYEN VAN AN", "12/30",
                        OtpService.generate("DH001")));

        run("Ví điện tử đủ số dư, nhập đúng OTP", o2,
                new EWalletPayment("0912345678", 2_000_000, OtpService.generate("DH002")));

        run("Chuyển khoản ngân hàng", o3,
                new BankTransferPayment("VCB", "0071000123456"));

        run("Thanh toán khi nhận hàng (COD)", o4,
                new CodPayment());

        // ---- Các trường hợp THẤT BẠI ----
        run("COD nhưng đơn vượt hạn mức 5 triệu", o5,
                new CodPayment());

        run("Thẻ tín dụng sai số thẻ", o5,
                new CreditCardPayment("4111 1111 1111 1112", "VO THI EM", "12/30",
                        OtpService.generate("DH005")));

        run("Ví điện tử nhập sai OTP", o2,
                new EWalletPayment("0912345678", 2_000_000, "000000"));

        // ---- Tổng kết ----
        System.out.println();
        System.out.println("=================== CÁC GIAO DỊCH ĐÃ LƯU ========================");
        System.out.printf("%-10s %-6s %-24s %15s%n", "Mã GD", "Đơn", "Phương thức", "Tổng tiền");
        System.out.println("-----------------------------------------------------------------");
        TransactionStore.findAll().forEach(System.out::println);
        System.out.println("=================================================================");
    }

    private static int caseNo = 0;

    private static void run(String title, Order order, PaymentProcessor processor) {
        caseNo++;
        System.out.println();
        System.out.println("--- Trường hợp " + caseNo + ": " + title + " ---");
        System.out.println("Đơn " + order.getOrderId() + " | " + order.getCustomerName()
                + " | " + Money.format(order.getAmount())
                + " | Bộ xử lý: " + processor.getClass().getSimpleName());

        PaymentResult result = processor.processPayment(order); // gọi TEMPLATE METHOD

        result.getSteps().forEach(s -> System.out.println("   " + s));
        if (result.isSuccess()) {
            System.out.println("   ==> KẾT QUẢ: THÀNH CÔNG | Mã GD " + result.getTransactionId()
                    + " | Đã thu " + Money.format(result.getTotal()));
        } else {
            System.out.println("   ==> KẾT QUẢ: THẤT BẠI | Lý do: " + result.getMessage());
        }
    }
}
