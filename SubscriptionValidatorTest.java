import org.junit.jupiter.api.Test;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

public class SubscriptionValidatorTest {

    @Test
    public void testValidInput() {
        assertDoesNotThrow(() -> SubscriptionValidator.validate("Бат Энх", "99119911", "09-25-2025", "$35.00"));
    }

    @Test
    public void testInvalidCustomer() {
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                SubscriptionValidator.validate("", "99119911", "09-25-2025", "$35.00"));
        assertEquals("Хэрэглэгчийн нэр дутуу байна.", e.getMessage());
    }

    @Test
    public void testInvalidPhone() {
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                SubscriptionValidator.validate("Бат Энх", "99111", "09-25-2025", "$35.00"));
        assertEquals("Утасны дугаарыг зөв оруулна уу (8 оронтой).", e.getMessage());
    }

    @Test
    public void testInvalidRecurringFormat() {
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                SubscriptionValidator.validate("Бат Энх", "99119911", "09-25-2025", "35.00"));
        assertEquals("Дахин төлбөрийг зөв оруулна уу (жишээ: $35.00).", e.getMessage());
    }

    @Test
    public void testRecurringTooSmall() {
        Exception e = assertThrows(IllegalArgumentException.class, () ->
                SubscriptionValidator.validate("Бат Энх", "99119911", "09-25-2025", "$0.00"));
        assertEquals("Дахин төлбөрийн дүн $0.01-аас бага байж болохгүй.", e.getMessage());
    }

    @Test
    public void testInvalidDateFormat() {
        Exception e = assertThrows(DateTimeParseException.class, () ->
                SubscriptionValidator.validate("Бат Энх", "99119911", "2025-09-25", "$35.00"));
        assertTrue(e.getMessage().contains("Дараагийн огноог зөв оруулна уу"));
    }
}
