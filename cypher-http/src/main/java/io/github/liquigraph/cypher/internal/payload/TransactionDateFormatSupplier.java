package io.github.liquigraph.cypher.internal.payload;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class TransactionDateFormatSupplier  {

    private static final ThreadLocal<DateFormat> INSTANCES = new ThreadLocal<>();

    public static DateFormat get() {
        DateFormat result = INSTANCES.get();
        if (result == null) {
            result = newDateFormat();
            INSTANCES.set(result);
        }
        return result;
    }

    private static DateFormat newDateFormat() {
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format;
    }
}
