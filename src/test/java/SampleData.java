import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class SampleData {

    private Random random = new Random();

    public String nationalId() {
        // TODO: Also have some D-numbers
        LocalDate birthDate = LocalDate.now().minusYears(80)
                .plusDays(random.nextInt(80*365));
        return birthDate.format(DateTimeFormatter.ofPattern("ddMMyy"))
                + randomNumericString(5); // TODO: Calculate checksum
    }

    private String randomNumericString(int length) {
        String result = "";
        for (int i=0; i<length; i++) {
            result += String.valueOf(random.nextInt(10));
        }
        return result;
    }
}
