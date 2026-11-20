# Lab 09 – Mẫu thiết kế Template Method

**Bài toán:** Xử lý thanh toán đơn hàng với 4 phương thức (thẻ tín dụng, ví điện tử, chuyển khoản ngân hàng, thanh toán khi nhận hàng – COD).
Mọi phương thức dùng chung một quy trình 7 bước do lớp trừu tượng `PaymentProcessor` định nghĩa trong template method `processPayment()` (khai báo `final`); mỗi lớp con chỉ cài đặt các bước khác biệt.

## Sản phẩm nộp

| Yêu cầu | File |
| --- | --- |
| Mã nguồn Java | [`src/`](src), [`test/`](test) |
| Sơ đồ UML lớp | [`uml/class-diagram.png`](uml/class-diagram.png) (nguồn PlantUML: [`uml/class-diagram.puml`](uml/class-diagram.puml)) |
| Kết quả thực nghiệm | [`ket-qua/01-demo-cac-truong-hop.png`](ket-qua/01-demo-cac-truong-hop.png), [`ket-qua/02-kiem-thu.png`](ket-qua/02-kiem-thu.png) |
| Báo cáo ngắn | [`bao-cao/BaoCao_TemplateMethod.docx`](bao-cao/BaoCao_TemplateMethod.docx) |

## Cấu trúc mã nguồn

```
src/payment/core/PaymentProcessor.java    Lớp trừu tượng – khung thuật toán (Template Method)
src/payment/methods/CreditCardPayment.java
src/payment/methods/EWalletPayment.java
src/payment/methods/BankTransferPayment.java
src/payment/methods/CodPayment.java       4 lớp con cài đặt các bước thay đổi
src/payment/model/                        Order, PaymentResult
src/payment/core/                         PaymentException, OtpService, TransactionStore, Money
src/payment/app/Main.java                 Chương trình minh hoạ (7 trường hợp)
test/payment/PaymentProcessorTest.java    14 ca kiểm thử
```

## Quy trình trong `processPayment()`

| Bước | Phương thức | Loại |
| --- | --- | --- |
| 1 | `validateOrder()` | Bước chung |
| 2 | `calculateFee()` | Abstract – lớp con cài đặt |
| 3 | `authenticate()` | Abstract – lớp con cài đặt |
| 4 | `if (requiresOtp()) verifyOtp()` | Hook + bước chung |
| 5 | `executeTransaction()` | Abstract – lớp con cài đặt |
| 6 | `saveTransaction()` | Bước chung |
| 7 | `sendNotification()` | Hook (mặc định email, COD gửi SMS) |

## Cách chạy

Yêu cầu JDK 17 trở lên. Trên Windows chạy `run.bat`, hoặc:

```
javac -encoding UTF-8 -d out src/payment/*/*.java test/payment/*.java
java -Dstdout.encoding=UTF-8 -cp out payment.app.Main
java -Dstdout.encoding=UTF-8 -cp out payment.PaymentProcessorTest
```
