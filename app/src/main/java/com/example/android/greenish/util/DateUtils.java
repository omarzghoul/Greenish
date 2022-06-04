package com.example.android.greenish.util;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

   /**
    * E - The text format day of the week abbreviation.
    * dd - The numerical day of the month.
    * MMM - The text format month of the year abbreviation.
    * Y - the full year.
    */
   private static final String DATE_FORMAT = "EEE, MMM d, ''yy";
   private static final String FIREBASE_DATE_FORMAT = "yyyy/MM/dd";
   public static final String HIGH_NEED = "HIGH";
   public static final String NORMAL_NEED = "NORMAL";
   public static final String LOW_NEED = "LOW";
   
   private static final int HIGH_LEVEL = 3;
   private static final int NORMAL_LEVEL = 2;
   private static final int LOW_LEVEL = 1;
   private static final int DAY_IN_MONTH = 30;

   /**
    *
    * @param lastWateringDate the date of last watering.
    * @return How much the need of water.
    */
   public static String compareDate(String lastWateringDate)
   {
      // YYYY => the full year
      // MM => The numerical month of the year
      // dd => Day in month (1-31)
      String[] lastWateringFields = lastWateringDate.split("/");
      String[] sysDateFields = formatDateTo(FIREBASE_DATE_FORMAT, new Date(System.currentTimeMillis())).split("/");
      int diff;
      // [0] = YEAR
      // [1] = MONTH
      // [2] = DAY
      if (sysDateFields[0].compareTo(lastWateringFields[0]) > 0) // Compare years
      {
         // In case, they're in different year and same century.
         return HIGH_NEED;
      }
      else if(lastWateringFields[1].compareTo(sysDateFields[1]) == 0) // Compare months
      {
         // In case they're in the same month
         // Then compare days
         int dayInCurrentDate = Integer.parseInt(sysDateFields[2]);
         int dayOfLastWatering = Integer.parseInt(lastWateringFields[2]);
         diff = dayInCurrentDate - dayOfLastWatering;
         if (diff >= HIGH_LEVEL) {
            return HIGH_NEED;
         } else if (diff >= NORMAL_LEVEL) {
            return NORMAL_NEED;
         } else {
            return LOW_NEED;
         }

      } else {
         // dates are in different months and same year;
         int dayInCurrentDate = Integer.parseInt(sysDateFields[2]); // e.g (1)
         int dayOfLastWatering = Integer.parseInt(lastWateringFields[2]); // e.g. (30)
         diff = dayOfLastWatering - dayInCurrentDate; // 30 - 1 = 29
         if (DAY_IN_MONTH - diff >= HIGH_LEVEL) { // (30 - 29 = 1) >= (2 = HIGH_LEVEL)
            return HIGH_NEED;
         } else if (DAY_IN_MONTH - diff >= NORMAL_LEVEL) { // (30 - 29 = 1) >= (1 = HIGH_LEVEL)
            return NORMAL_NEED;
         } else {
            return LOW_NEED;
         }

      }
   }

   public static String formatDateTo(String pattern, Date date)
   {
      return new SimpleDateFormat(pattern, Locale.US).format(date);
   }


   public static final String DATE_USE_SLASH = "yyyy/MM/dd";
   /**
    *
    * @param pattern, the date formatter.
    * @return a String of the specified date.
    */
   public static String dateHelper(String pattern) {
      return simpleDateFormat(pattern).format(new Date());
   }

   /**
    *
    * @param pattern is a date formatter,  e.g. "dd/MM//YY"
    * @return a new SimpleDateFormat object.
    */
   public static SimpleDateFormat simpleDateFormat(String pattern) {
      return new SimpleDateFormat(pattern, Locale.getDefault());
   }

}