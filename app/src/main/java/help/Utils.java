package help;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utils {

    public static boolean isEmpty(Object method) {

        return ((String) method).isEmpty();
    }

    public static boolean isNumber(String mimutes) {

        try {
            Integer.parseInt(mimutes);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    public static Calendar parseStrToCld(String str) {
        Calendar calendar = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date;

            date = sdf.parse(str);

            calendar = Calendar.getInstance();
            calendar.setTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
    }

    public static Object getFormatCld(Calendar calendar) {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = sdf.format(calendar.getTime());
        return dateStr;
    }

}
