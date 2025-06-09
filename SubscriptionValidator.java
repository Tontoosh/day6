import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class SubscriptionValidator {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    public static void validate(String customer, String phone, String nextDate, String recurring) {
        if (customer == null || customer.trim().length() < 2) {
            throw new IllegalArgumentException("Хэрэглэгчийн нэр дутуу байна.");
        }
        if (phone == null || !phone.matches("\\d{8}")) {
            throw new IllegalArgumentException("Утасны дугаарыг зөв оруулна уу (8 оронтой).");
        }
        if (recurring == null || !recurring.matches("\\$\\d+(\\.\\d{2})?")) {
            throw new IllegalArgumentException("Дахин төлбөрийг зөв оруулна уу (жишээ: $35.00).");
        }
        double amount = Double.parseDouble(recurring.replace("$", ""));
        if (amount < 0.01) {
            throw new IllegalArgumentException("Дахин төлбөрийн дүн $0.01-аас бага байж болохгүй.");
        }
        try {
            LocalDate.parse(nextDate, dateFormatter);
        } catch (DateTimeParseException ex) {
            throw new DateTimeParseException("Дараагийн огноог зөв оруулна уу (жишээ: 09-25-2025).", nextDate, ex.getErrorIndex());
        }
    }
}
