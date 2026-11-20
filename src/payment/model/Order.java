package payment.model;

/**
 * Đơn hàng cần thanh toán.
 */
public class Order {
    private final String orderId;
    private final String customerName;
    private final String email;
    private final String phone;
    private final String shippingAddress;
    private final long amount; // tổng tiền hàng (VND)

    public Order(String orderId, String customerName, String email,
                 String phone, String shippingAddress, long amount) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.email = email;
        this.phone = phone;
        this.shippingAddress = shippingAddress;
        this.amount = amount;
    }

    public String getOrderId()         { return orderId; }
    public String getCustomerName()    { return customerName; }
    public String getEmail()           { return email; }
    public String getPhone()           { return phone; }
    public String getShippingAddress() { return shippingAddress; }
    public long getAmount()            { return amount; }
}
