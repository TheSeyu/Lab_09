package payment.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Kho lưu giao dịch trong bộ nhớ (thay cho cơ sở dữ liệu).
 */
public final class TransactionStore {
    private static final List<String> RECORDS = new ArrayList<>();

    private TransactionStore() { }

    public static void save(String record) {
        RECORDS.add(record);
    }

    public static List<String> findAll() {
        return Collections.unmodifiableList(RECORDS);
    }

    public static void clear() {
        RECORDS.clear();
    }
}
