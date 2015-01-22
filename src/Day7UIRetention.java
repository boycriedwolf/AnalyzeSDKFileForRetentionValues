import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;

import com.opencsv.CSVReader;


/**
 * @desc holds functions to query 7 day retention data from csv file with filters
 * contains helper methods to validate input dates, as well as methods to evaluate data
 * examples include queryDay7UIRetention, querySingleDay, and combination of parameters
 * taking different filtering options (os, sdk version, single day of period of time)
 * @author Byron Tang byronyugontang@gmail.com
 */

public class Day7UIRetention {

  //nested map containing data from csv file
  static HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, 
  Event>>>> mapWithYearMonthDay;

  public static void main(String[] args) {
    String csvFile = "Analytics Coding Challenge Data.csv";
    
    //outer most integer represents year, then month, then day, then eventId
    mapWithYearMonthDay = new HashMap<Integer, HashMap<Integer, 
        HashMap<Integer, HashMap<Integer,Event>>>>();

    CSVReader reader; //library we use to parse cvs file
    
    try {
      reader = new CSVReader(new FileReader(csvFile));
      String [] nextLine;
      int eventId, eventCount, userId;
      String sdkVersion;
      String eventName, eventTime, osName;
      nextLine = reader.readNext();  //skip the first line of file

      while ((nextLine = reader.readNext()) != null) {
        //grab all the values from the line
        eventId = Integer.parseInt(nextLine[0]);
        eventCount = Integer.parseInt(nextLine[1]);
        eventName = nextLine[2];
        eventTime = nextLine[3];
        osName = nextLine[4];
        sdkVersion = nextLine[5];
        userId = Integer.parseInt(nextLine[6]);

        //populate nested map
        Event event = new Event(eventId, eventCount, eventName, eventTime, 
            osName, sdkVersion, userId);
        int[] dateAsIntegers = event.convertTimeToInts(); //private method to parse date
        int year = dateAsIntegers[0];
        int month = dateAsIntegers[1];
        int day = dateAsIntegers[2];

        //either updates the map with new data, or creates new maps for unvisited dates
        if(mapWithYearMonthDay.get(year) != null) {
          if(mapWithYearMonthDay.get(year).get(month) != null) {
            if(mapWithYearMonthDay.get(year).get(month).get(day) != null) {
              mapWithYearMonthDay.get(year).get(month).get(day).put(eventId, event);
            }
            else {
              HashMap<Integer, Event> eventIdMap = 
                  new HashMap<Integer, Event>();
              eventIdMap.put(eventId, event);
              mapWithYearMonthDay.get(year).get(month).put(day, eventIdMap);
            }
          }
          else {
            HashMap<Integer, Event> eventIdMap = new HashMap<Integer, Event>();
            eventIdMap.put(eventId, event);
            HashMap<Integer, HashMap<Integer, Event>> dayMap = 
                new HashMap<Integer, HashMap<Integer, Event>>();
            dayMap.put(day, eventIdMap);
            mapWithYearMonthDay.get(year).put(month, dayMap);
          }
        }
        else {
          HashMap<Integer, Event> eventIdMap = new HashMap<Integer, Event>();
          eventIdMap.put(eventId, event);
          HashMap<Integer, HashMap<Integer, Event>> dayMap = 
              new HashMap<Integer, HashMap<Integer, Event>>();
          dayMap.put(day, eventIdMap);
          HashMap<Integer, HashMap<Integer, HashMap<Integer, Event>>> monthMap = 
              new HashMap<Integer, HashMap<Integer, HashMap<Integer, Event>>>();
          monthMap.put(month, dayMap);
          mapWithYearMonthDay.put(year, monthMap);
        }

      }
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    //testing filtering on multiple days
    System.out.println("Retention for month of September: " 
        + queryDay7UIRetention(2014, 9, 1, 2014, 9, 30));
    System.out.println("Retention for month of September on IOS: " 
        + queryDay7UIRetentionWithOS(2014, 9, 1, 2014, 9, 30, "IOS"));
    System.out.println("Retention for month of September on android: " 
        + queryDay7UIRetentionWithOS(2014, 9, 1, 2014, 9, 30, "android"));
    System.out.println("Retention for month of September on SDK version 1.7.5: "
        + queryDay7UIRetentionWithSDK(2014, 9, 1, 2014, 9, 30, "1.7.0"));
    System.out.println("Retention for month of September on IOS and SDK version 1.7.5: " 
        + queryDay7UIRetentionWithOSAndSDK(2014, 9, 1, 2014, 9, 30, "IOS", "1.7.0"));

    //testing filtering on single days
    System.out.println("Retention for Sep8: " + querySingleDay(2014, 9, 8));
    System.out.println("Retention for Sep8 on android: " 
        + querySingleDayWithOS(2014, 9, 8, "android"));
    System.out.println("Retention for Sep8 on IOS: " 
        + querySingleDayWithOS(2014, 9, 8, "IOS"));
    System.out.println("Retention for Sep29 on sdk version 1.7.5: " 
        + querySingleDayWithSDK(2014, 9, 8, "1.7.5"));
    System.out.println("Retention for Sep29 on android and sdk version 1.7.5: "
        + querySingleDayWithOSAndSDK(2014, 9, 8, "android", "1.7.5"));

    //testing some edge cases
    System.out.println("Retention for Feb30: " + querySingleDay(2014, 2, 30));
    System.out.println("Retention for 13/1: " + querySingleDay(2014, 13, 1));
    System.out.println("Retention for 9/1 - 9/33: "
        + queryDay7UIRetention(2014, 9, 1, 2014, 9, 33));
    System.out.println("Retention for Feb28 2015: " + querySingleDay(2015, 2, 28));


  }

  /**
   * @desc calculates 7day retention value using values from queryDay7UIRetention method
   * @param int year - the year of the date to evaluate
   * @param int month - the month of the date to evaluate
   * @param int day - the day of the date to evaluate
   * @return double - the decimal value of rate of retention 7 days later
   */

  private static double querySingleDay(int year, int month, int day) {
    int[] returnArray = queryDay7UIRetention(year, month, day);
    if(returnArray[0]==0 || returnArray[1] == 0) {  //if no users on date or 7days later
      return 0.0;
    }
    else return (double) returnArray[0]/ (double) returnArray[1];
  }

  /**
   * @desc calculates 7day retention value using queryDay7UIRetentionWithOS
   * @param int year - the year of the date to evaluate
   * @param int month - the month of the date to evaluate
   * @param int day - the day of the date to evaluate
   * @param string osName - filtering out events not using the OS inputted
   * @return double - the decimal value of rate of retention 7 days later
   */

  private static double querySingleDayWithOS(int year, int month, int day, String osName) {
    int[] returnArray = queryDay7UIRetentionWithOS(year, month, day, osName);
    if(returnArray[0]==0 || returnArray[1] == 0) {
      return 0.0;
    }
    else return (double) returnArray[0]/ (double) returnArray[1];
  }

  /**
   * @desc calculates 7day retention value using queryDay7UIRetentionWithSDK
   * @param int year - the year of the date to evaluate
   * @param int month - the month of the date to evaluate
   * @param int day - the day of the date to evaluate
   * @param string sdk - filtering out events not using the sdk version inputted
   * @return double - the decimal value of rate of retention 7 days later
   */

  private static double querySingleDayWithSDK(int year, int month, int day, String sdk) {
    int[] returnArray = queryDay7UIRetentionWithSDK(year, month, day, sdk);
    if(returnArray[0]==0 || returnArray[1] == 0) {
      return 0.0;
    }
    else return (double) returnArray[0]/ (double) returnArray[1];
  }

  /**
   * @desc calculates 7day retention value using queryDay7UIRetentionWithOSAndSDK
   * @param int year - the year of the date to evaluate
   * @param int month - the month of the date to evaluate
   * @param int day - the day of the date to evaluate
   * @param string osName - filtering out events not using the os inputted
   * @param string sdk - filtering out events not using the sdk version inputted
   * @return double - the decimal value of rate of retention 7 days later
   */

  private static double querySingleDayWithOSAndSDK(int year, int month, int day, 
      String osName, String sdk) {
    int[] returnArray =   queryDay7UIRetentionWithOSAndSDK(year, month, day, osName, sdk);
    if(returnArray[0]==0 || returnArray[1] == 0) {
      return 0.0;
    }
    else return (double) returnArray[0]/ (double) returnArray[1];
  }

  /**
   * @desc searches nested map to determine the users who use the UI on the input date 
   * as well as the date 7days later, then compares the two user lists to return how
   * many users returned 7days later
   * @param int year - the year of the date to evaluate
   * @param int month - the month of the date to evaluate
   * @param int day - the day of the date to evaluate
   * @return int[], index 1 holding the number of users using UI on original date
   * index 0 holding the number of same users using UI again 7days later
   */

  private static int[] queryDay7UIRetention(int year, int month, int day) {
    int[] returnArray = new int[2];
    for(int i=0; i< returnArray.length; i++) {
      returnArray[i] = 0;
    }

    HashMap<Integer, Integer> userIdList = new HashMap<Integer, Integer>(); 
    HashMap<Integer, Integer> userIdListWeekLater = new HashMap<Integer, Integer>(); 

    //look for event objects corresponding to date and event_name
    if(mapWithYearMonthDay.get(year) != null) {
      if(mapWithYearMonthDay.get(year).get(month) != null) {
        if(mapWithYearMonthDay.get(year).get(month).get(day) != null) {
          for(Integer eventId : mapWithYearMonthDay.get(year).get(month).get(day).keySet()) {
            Event event = mapWithYearMonthDay.get(year).get(month).get(day).get(eventId);
            if(event.getEventName().equals("UI_OPEN_COUNT")) {
              userIdList.put(event.getUserId(), 1);
            }
          }
        }
        else returnArray[1] = 0; //if no users used UI on this date, return 0.0
      }
      else returnArray[1] = 0;
    }
    else returnArray[1] = 0;

    Calendar calendar = null;

    if(checkIfDatesValid(year, month, day)) {
      calendar = new GregorianCalendar(year, month, day);
      calendar.add(Calendar.DATE, 7); //get the date 7days from now
    }
    else {
      System.out.println("Invalid date");
      return returnArray;
    }

    int newYear = calendar.get(Calendar.YEAR);
    int newMonth = calendar.get(Calendar.MONTH);
    int newDay = calendar.get(Calendar.DAY_OF_MONTH);;

    //look for event objects corresponding to date 7days later
    if(mapWithYearMonthDay.get(newYear) != null) {
      if(mapWithYearMonthDay.get(newYear).get(newMonth) != null) {
        if(mapWithYearMonthDay.get(newYear).get(newMonth).get(newDay) != null) {
          for(Integer eventId : mapWithYearMonthDay.get(newYear)
              .get(newMonth).get(newDay).keySet()) {
            Event event = mapWithYearMonthDay.get(newYear)
                .get(newMonth).get(newDay).get(eventId);
            if(event.getEventName().equals("UI_OPEN_COUNT")) {
              userIdListWeekLater.put(event.getUserId(), 1);
            }
          }
        }
        else returnArray[0] = 0; //if no users used UI on these new dates, return 0.0
      }
      else returnArray[0] = 0;
    }
    else returnArray[0] = 0;

    int counter = 0; //variable represents number of users from 7days ago using UI
    for (Map.Entry<Integer, Integer> map : userIdList.entrySet()) { 
      if(userIdListWeekLater.containsKey(map.getKey())) {
        counter++;
      }
    }

    returnArray[0] = counter; //how many 7 days later
    returnArray[1] = userIdList.size(); //how many on the day passed in
    return returnArray; 

  }

  /**
   * @desc searches nested map to determine the users who use the UI on the input date 
   * along with filtering out OS's that don't match the one the input,
   * as well as the date 7days later, then compares the two user lists to return how
   * many users returned 7days later
   * @param int year - the year of the date to evaluate
   * @param int month - the month of the date to evaluate
   * @param int day - the day of the date to evaluate
   * @param string osName - the os type we want to evaluate
   * @return int[], index 1 holding the number of users using UI on original date
   * index 0 holding the number of same users using UI again 7days later
   */

  private static int[] queryDay7UIRetentionWithOS(int year, int month, 
      int day, String osName) {
    int[] returnArray = new int[2];
    for(int i=0; i< returnArray.length; i++) {
      returnArray[i] = 0;
    }

    HashMap<Integer, Integer> userIdList = new HashMap<Integer, Integer>(); 
    HashMap<Integer, Integer> userIdListWeekLater = new HashMap<Integer, Integer>(); 

    if(mapWithYearMonthDay.get(year) != null) {
      if(mapWithYearMonthDay.get(year).get(month) != null) {
        if(mapWithYearMonthDay.get(year).get(month).get(day) != null) {
          for(Integer eventId : mapWithYearMonthDay.get(year).get(month)
              .get(day).keySet()) {
            Event event = mapWithYearMonthDay.get(year).get(month)
                .get(day).get(eventId);
            if(event.getEventName().equals("UI_OPEN_COUNT") && 
                event.getOsName().equals(osName)) {
              userIdList.put(event.getUserId(), 1);
            }
          }
        }
        else returnArray[1] = 0; //if no users used UI on this date, return 0.0
      }
      else returnArray[1] = 0;
    }
    else returnArray[1] = 0;

    Calendar calendar = null;

    if(checkIfDatesValid(year, month, day)) {
      calendar = new GregorianCalendar(year, month, day);
      calendar.add(Calendar.DATE, 7);
    }
    else {
      System.out.println("Invalid date");
      return returnArray;
    }

    int newYear = calendar.get(Calendar.YEAR);
    int newMonth = calendar.get(Calendar.MONTH);
    int newDay = calendar.get(Calendar.DAY_OF_MONTH);;

    if(mapWithYearMonthDay.get(newYear) != null) {
      if(mapWithYearMonthDay.get(newYear).get(newMonth) != null) {
        if(mapWithYearMonthDay.get(newYear).get(newMonth).get(newDay) != null) {
          for(Integer eventId : mapWithYearMonthDay.get(newYear).get(newMonth).get(newDay).keySet()) {
            Event event = mapWithYearMonthDay.get(newYear).get(newMonth).get(newDay).get(eventId);
            if(event.getEventName().equals("UI_OPEN_COUNT")) {
              userIdListWeekLater.put(event.getUserId(), 1);
            }
          }
        }
        else returnArray[0] = 0; //if no users used UI on these new dates, return 0.0
      }
      else returnArray[0] = 0;
    }
    else returnArray[0] = 0;

    int counter = 0;
    for (Map.Entry<Integer, Integer> map : userIdList.entrySet()) { 
      if(userIdListWeekLater.containsKey(map.getKey())) {
        counter++;
      }
    }

    returnArray[0] = counter; //how many 7 days later
    returnArray[1] = userIdList.size(); //how many on the day passed in
    return returnArray; 

  }

  /**
   * @desc searches nested map to determine the users who use the UI on the input date 
   * along with filtering out sdk version's that don't match the one the input,
   * as well as the date 7days later, then compares the two user lists to return how
   * many users returned 7days later
   * @param int year - the year of the date to evaluate
   * @param int month - the month of the date to evaluate
   * @param int day - the day of the date to evaluate
   * @param string sdkVersion - the sdk version we want to evaluate
   * @return int[], index 1 holding the number of users using UI on original date
   * index 0 holding the number of same users using UI again 7days later
   */

  private static int[] queryDay7UIRetentionWithSDK(int year, int month, int day, String sdkVersion) {
    int[] returnArray = new int[2];
    for(int i=0; i< returnArray.length; i++) {
      returnArray[i] = 0;
    }

    HashMap<Integer, Integer> userIdList = new HashMap<Integer, Integer>(); 
    HashMap<Integer, Integer> userIdListWeekLater = new HashMap<Integer, Integer>(); 


    if(mapWithYearMonthDay.get(year) != null) {
      if(mapWithYearMonthDay.get(year).get(month) != null) {
        if(mapWithYearMonthDay.get(year).get(month).get(day) != null) {
          for(Integer eventId : mapWithYearMonthDay.get(year).get(month).get(day).keySet()) {
            Event event = mapWithYearMonthDay.get(year).get(month).get(day).get(eventId);
            if(event.getEventName().equals("UI_OPEN_COUNT") && event.getSdkVersion().equals(sdkVersion)) {
              userIdList.put(event.getUserId(), 1);
            }
          }
        }
        else returnArray[1] = 0; //if no users used UI on this date, return 0.0
      }
      else returnArray[1] = 0;
    }
    else returnArray[1] = 0;

    Calendar calendar = null;

    if(checkIfDatesValid(year, month, day)) {
      calendar = new GregorianCalendar(year, month, day);
      calendar.add(Calendar.DATE, 7);
    }
    else {
      System.out.println("Invalid date");
      return returnArray;
    }

    int newYear = calendar.get(Calendar.YEAR);
    int newMonth = calendar.get(Calendar.MONTH);
    int newDay = calendar.get(Calendar.DAY_OF_MONTH);;

    if(mapWithYearMonthDay.get(newYear) != null) {
      if(mapWithYearMonthDay.get(newYear).get(newMonth) != null) {
        if(mapWithYearMonthDay.get(newYear).get(newMonth).get(newDay) != null) {
          for(Integer eventId : mapWithYearMonthDay.get(newYear).get(newMonth).get(newDay).keySet()) {
            Event event = mapWithYearMonthDay.get(newYear).get(newMonth).get(newDay).get(eventId);
            if(event.getEventName().equals("UI_OPEN_COUNT")) {
              userIdListWeekLater.put(event.getUserId(), 1);
            }
          }
        }
        else returnArray[0] = 0; //if no users used UI on these new dates, return 0.0
      }
      else returnArray[0] = 0;
    }
    else returnArray[0] = 0;

    int counter = 0;
    for (Map.Entry<Integer, Integer> map : userIdList.entrySet()) { 
      if(userIdListWeekLater.containsKey(map.getKey())) {
        counter++;
      }
    }

    returnArray[0] = counter; //how many 7 days later
    returnArray[1] = userIdList.size(); //how many on the day passed in
    return returnArray; 

  }

  /**
   * @desc searches nested map to determine the users who use the UI on the input date 
   * along with filtering out os's and sdk version's that don't match the one the input,
   * as well as the date 7days later, then compares the two user lists to return how
   * many users returned 7days later
   * @param int year - the year of the date to evaluate
   * @param int month - the month of the date to evaluate
   * @param int day - the day of the date to evaluate
   * @param string osName - the os we want to evaluate
   * @param string sdkVersion - the sdk version we want to evaluate
   * @return int[], index 1 holding the number of users using UI on original date
   * index 0 holding the number of same users using UI again 7days later
   */

  private static int[] queryDay7UIRetentionWithOSAndSDK(int year, int month, int day, String osName, String sdkVersion) {
    int[] returnArray = new int[2];
    for(int i=0; i< returnArray.length; i++) {
      returnArray[i] = 0;
    }

    HashMap<Integer, Integer> userIdList = new HashMap<Integer, Integer>(); 
    HashMap<Integer, Integer> userIdListWeekLater = new HashMap<Integer, Integer>(); 


    if(mapWithYearMonthDay.get(year) != null) {
      if(mapWithYearMonthDay.get(year).get(month) != null) {
        if(mapWithYearMonthDay.get(year).get(month).get(day) != null) {
          for(Integer eventId : mapWithYearMonthDay.get(year).get(month).get(day).keySet()) {
            Event event = mapWithYearMonthDay.get(year).get(month).get(day).get(eventId);
            if(event.getEventName().equals("UI_OPEN_COUNT") && 
                event.getOsName().equals(osName) && event.getSdkVersion().equals(sdkVersion)) {
              userIdList.put(event.getUserId(), 1);
            }
          }
        }
        else returnArray[1] = 0; //if no users used UI on this date, return 0.0
      }
      else returnArray[1] = 0;
    }
    else returnArray[1] = 0;

    Calendar calendar = null;

    if(checkIfDatesValid(year, month, day)) {
      calendar = new GregorianCalendar(year, month, day);
      calendar.add(Calendar.DATE, 7);
    }
    else {
      System.out.println("Invalid date");
      return returnArray;
    }

    int newYear = calendar.get(Calendar.YEAR);
    int newMonth = calendar.get(Calendar.MONTH);
    int newDay = calendar.get(Calendar.DAY_OF_MONTH);;

    if(mapWithYearMonthDay.get(newYear) != null) {
      if(mapWithYearMonthDay.get(newYear).get(newMonth) != null) {
        if(mapWithYearMonthDay.get(newYear).get(newMonth).get(newDay) != null) {
          for(Integer eventId : mapWithYearMonthDay.get(newYear).get(newMonth).get(newDay).keySet()) {
            Event event = mapWithYearMonthDay.get(newYear).get(newMonth).get(newDay).get(eventId);
            if(event.getEventName().equals("UI_OPEN_COUNT")) {
              userIdListWeekLater.put(event.getUserId(), 1);
            }
          }
        }
        else returnArray[0] = 0; //if no users used UI on these new dates, return 0.0
      }
      else returnArray[0] = 0;
    }
    else returnArray[0] = 0;

    int counter = 0;
    for (Map.Entry<Integer, Integer> map : userIdList.entrySet()) { 
      if(userIdListWeekLater.containsKey(map.getKey())) {
        counter++;
      }
    }

    returnArray[0] = counter; //how many 7 days later
    returnArray[1] = userIdList.size(); //how many on the day passed in
    return returnArray; 

  }

  /**
   * @desc loops through the number of days in the range provided, each iteration of
   * the loops calculating the day7 retention values of each date
   * @param int year1 - the year of the start date we evaluate
   * @param int month1 - the month of the start date we evaluate
   * @param int day1 - the day of the start date we evaluate
   * @param int year2 - the year of the end date we evaluate to
   * @param int month2 - the month of the end date we evaluate to
   * @param int day2 - the day of the end date we evaluate to
   * @return double - the double decimal value of total users who initially use the UI
   * on each day in the range over the total users who come back 7days later
   */

  private static double queryDay7UIRetention(int year1, int month1, int day1, int year2, int month2, int day2) {
    int totalDayOneUsers = 0;
    int totalDaySevenUsersRetained = 0;

    //make sure end date is valid
    if(!checkIfDatesValid(year2, month2, day2)) {
      System.out.println("Invalid end date");
    }

    //calculate how many days are in the date range
    Calendar calendar1 = new GregorianCalendar(year1, month1, day1);
    Date date1 = calendar1.getTime();
    Calendar calendar2 = new GregorianCalendar(year2, month2, day2);
    Date date2 = calendar2.getTime();
    long diff = date2.getTime() - date1.getTime();
    long diffDays = diff / (24 * 60 * 60 * 1000)+1;
    int daysDifferent = (int) diffDays;
    int daysCounter = 0;
    int currentYear = year1;
    int currentMonth = month1;
    int currentDay = day1;

    //loop through all the days to get total initial users and total users 7 days later
    while(daysCounter < daysDifferent) {
      int[] usersRemainingAndInitialUsers = queryDay7UIRetention(currentYear, currentMonth, currentDay);
      totalDaySevenUsersRetained += usersRemainingAndInitialUsers[0];
      totalDayOneUsers += usersRemainingAndInitialUsers[1];
      Calendar currentCalendar = new GregorianCalendar(currentYear, currentMonth, currentDay);
      currentCalendar.add(Calendar.DATE, 1);
      currentYear = currentCalendar.get(Calendar.YEAR);
      currentMonth = currentCalendar.get(Calendar.MONTH);
      currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);
      daysCounter++;
    }

    return (double) totalDaySevenUsersRetained/ (double) totalDayOneUsers;

  }

  /**
   * @desc loops through the number of days in the range provided, each iteration of
   * the loops calculating the day7 retention values of each date while filtering out os
   * @param int year1 - the year of the start date we evaluate
   * @param int month1 - the month of the start date we evaluate
   * @param int day1 - the day of the start date we evaluate
   * @param int year2 - the year of the end date we evaluate to
   * @param int month2 - the month of the end date we evaluate to
   * @param int day2 - the day of the end date we evaluate to
   * @param string osName - the name of the OS we filter for
   * @return double - the double decimal value of total users who initially use the UI
   * on each day in the range over the total users who come back 7days later
   */

  private static double queryDay7UIRetentionWithOS(int year1, int month1, int day1,
      int year2, int month2, int day2, String osName) {
    int totalDayOneUsers = 0;
    int totalDaySevenUsersRetained = 0;

    //make sure end date is valid
    if(!checkIfDatesValid(year2, month2, day2)) {
      System.out.println("Invalid end date");
    }

    //calculate how many days are in the date range
    Calendar calendar1 = new GregorianCalendar(year1, month1, day1);
    Date date1 = calendar1.getTime();
    Calendar calendar2 = new GregorianCalendar(year2, month2, day2);
    Date date2 = calendar2.getTime();
    long diff = date2.getTime() - date1.getTime();
    long diffDays = diff / (24 * 60 * 60 * 1000)+1;
    int daysDifferent = (int) diffDays;
    int daysCounter = 0;
    int currentYear = year1;
    int currentMonth = month1;
    int currentDay = day1;

    //loop through all the days to get total initial users and total users 7 days later
    while(daysCounter < daysDifferent) {
      int[] usersRemainingAndInitialUsers = queryDay7UIRetentionWithOS(currentYear, currentMonth, currentDay, osName);
      totalDaySevenUsersRetained += usersRemainingAndInitialUsers[0];
      totalDayOneUsers += usersRemainingAndInitialUsers[1];
      Calendar currentCalendar = new GregorianCalendar(currentYear, currentMonth, currentDay);
      currentCalendar.add(Calendar.DATE, 1);
      currentYear = currentCalendar.get(Calendar.YEAR);
      currentMonth = currentCalendar.get(Calendar.MONTH);
      currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);
      daysCounter++;
    }

    return (double) totalDaySevenUsersRetained/ (double) totalDayOneUsers;

  }

  /**
   * @desc loops through the number of days in the range provided, each iteration of
   * the loops calculating the day7 retention values of each date while filtering 
   * out sdk versions
   * @param int year1 - the year of the start date we evaluate
   * @param int month1 - the month of the start date we evaluate
   * @param int day1 - the day of the start date we evaluate
   * @param int year2 - the year of the end date we evaluate to
   * @param int month2 - the month of the end date we evaluate to
   * @param int day2 - the day of the end date we evaluate to
   * @param string sdk - the name of the sdk version we filter for
   * @return double - the double decimal value of total users who initially use the UI
   * on each day in the range over the total users who come back 7days later
   */

  private static double queryDay7UIRetentionWithSDK(int year1, int month1, int day1,
      int year2, int month2, int day2, String sdk) {
    int totalDayOneUsers = 0;
    int totalDaySevenUsersRetained = 0;

    //make sure end date is valid
    if(!checkIfDatesValid(year2, month2, day2)) {
      System.out.println("Invalid end date");
    }

    //calculate how many days are in the date range
    Calendar calendar1 = new GregorianCalendar(year1, month1, day1);
    Date date1 = calendar1.getTime();
    Calendar calendar2 = new GregorianCalendar(year2, month2, day2);
    Date date2 = calendar2.getTime();
    long diff = date2.getTime() - date1.getTime();
    long diffDays = diff / (24 * 60 * 60 * 1000)+1;
    int daysDifferent = (int) diffDays;
    int daysCounter = 0;
    int currentYear = year1;
    int currentMonth = month1;
    int currentDay = day1;

    //loop through all the days to get total initial users and total users 7 days later
    while(daysCounter < daysDifferent) {
      int[] usersRemainingAndInitialUsers = 
          queryDay7UIRetentionWithSDK(currentYear, currentMonth, currentDay, sdk);
      totalDaySevenUsersRetained += usersRemainingAndInitialUsers[0];
      totalDayOneUsers += usersRemainingAndInitialUsers[1];
      Calendar currentCalendar = new GregorianCalendar(currentYear, currentMonth, currentDay);
      currentCalendar.add(Calendar.DATE, 1);
      currentYear = currentCalendar.get(Calendar.YEAR);
      currentMonth = currentCalendar.get(Calendar.MONTH);
      currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);
      daysCounter++;
    }

    return (double) totalDaySevenUsersRetained/ (double) totalDayOneUsers;

  }

  /**
   * @desc loops through the number of days in the range provided, each iteration of
   * the loops calculating the day7 retention values of each date while filtering out os
   * and sdk versions we want
   * @param int year1 - the year of the start date we evaluate
   * @param int month1 - the month of the start date we evaluate
   * @param int day1 - the day of the start date we evaluate
   * @param int year2 - the year of the end date we evaluate to
   * @param int month2 - the month of the end date we evaluate to
   * @param int day2 - the day of the end date we evaluate to
   * @param string osName - the name of the OS we filter for
   * @param string sdk - the sdk version we filter for
   * @return double - the double decimal value of total users who initially use the UI
   * on each day in the range over the total users who come back 7days later
   */

  private static double queryDay7UIRetentionWithOSAndSDK(int year1, int month1, 
      int day1, int year2, int month2, int day2, String osName, String sdk) {
    int totalDayOneUsers = 0;
    int totalDaySevenUsersRetained = 0;

    //make sure end date is valid
    if(!checkIfDatesValid(year2, month2, day2)) {
      System.out.println("Invalid end date");
    }

    //calculate how many days are in the date range
    Calendar calendar1 = new GregorianCalendar(year1, month1, day1);
    Date date1 = calendar1.getTime();
    Calendar calendar2 = new GregorianCalendar(year2, month2, day2);
    Date date2 = calendar2.getTime();
    long diff = date2.getTime() - date1.getTime();
    long diffDays = diff / (24 * 60 * 60 * 1000)+1;
    int daysDifferent = (int) diffDays;
    int daysCounter = 0;
    int currentYear = year1;
    int currentMonth = month1;
    int currentDay = day1;

    //loop through all the days to get total initial users and total users 7 days later
    while(daysCounter < daysDifferent) {
      int[] usersRemainingAndInitialUsers = 
          queryDay7UIRetentionWithOSAndSDK(currentYear, currentMonth, currentDay, osName, sdk);
      totalDaySevenUsersRetained += usersRemainingAndInitialUsers[0];
      totalDayOneUsers += usersRemainingAndInitialUsers[1];
      Calendar currentCalendar = new GregorianCalendar(currentYear, currentMonth, currentDay);
      currentCalendar.add(Calendar.DATE, 1);
      currentYear = currentCalendar.get(Calendar.YEAR);
      currentMonth = currentCalendar.get(Calendar.MONTH);
      currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);
      daysCounter++;
    }

    return (double) totalDaySevenUsersRetained/ (double) totalDayOneUsers;

  }

  /**
   * @desc checks the date in parameters to make sure it is legal
   * ie month < 12 or Feb 29 etc
   * @param int year - the year of the date
   * @param int month - the month of the date
   * @param int day - the day of the date
   * @return bool - true or false depending on whether date input is valid
   */

  private static boolean checkIfDatesValid(int year, int month, int day) {
    if(month == 2) {
      if(year%4==0) { //check for leap year on febuarys
        if(day<0 || day>29) {
          return false;
        }
      }
      else if(day<0 || day>28) {
        return false;
      }
    }

    //could not figure out why this wouldn't work for feb
    Calendar testCalendar = new GregorianCalendar(year, month, day);
    int days = testCalendar.getActualMaximum(Calendar.DAY_OF_MONTH); // 28
    int presentYear = Calendar.getInstance().get(Calendar.YEAR);

    if(year>presentYear) {
      return false;
    }

    if(month<1 || month>12) {
      return false;
    }

    if(day<1 || day>days) {
      return false;
    }
    else {
      return true;
    }
  }


}
