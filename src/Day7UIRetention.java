import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;

import com.opencsv.CSVReader;

public class Day7UIRetention {

  static HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, 
    Event>>>> mapWithYearMonthDay;
  
  public static void main(String[] args) {
    String csvFile = "Analytics Coding Challenge Data.csv";
    //outer most integer represents year, then month, then day, then eventId
     
    mapWithYearMonthDay = new HashMap<Integer, HashMap<Integer, 
    HashMap<Integer, HashMap<Integer,Event>>>>();


    CSVReader reader;
    try {
      reader = new CSVReader(new FileReader(csvFile));
      String [] nextLine;
      int eventId, eventCount, userId;
      String sdkVersion;
      String eventName, eventTime, osName;
      nextLine = reader.readNext(); //skip the first line
      
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
        int[] dateAsIntegers = event.convertTimeToInts();
        int year = dateAsIntegers[0];
        int month = dateAsIntegers[1];
        int day = dateAsIntegers[2];

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
    System.out.println("Retention for Sep8 on android: " + querySingleDayWithOS(2014, 9, 8, "android"));
    System.out.println("Retention for Sep8 on IOS: " + querySingleDayWithOS(2014, 9, 8, "IOS"));
    System.out.println("Retention for Sep29 on sdk version 1.7.5: " + querySingleDayWithSDK(2014, 9, 8, "1.7.5"));
    System.out.println("Retention for Sep29 on android and sdk version 1.7.5: " + querySingleDayWithOSAndSDK(2014, 9, 8, "android", "1.7.5"));
    
    
    //testing some edge cases
    System.out.println("Retention for Feb30: " + querySingleDay(2014, 2, 30));
    System.out.println("Retention for 13/1: " + querySingleDay(2014, 13, 1));
    System.out.println("Retention for 9/1 - 9/33: " + queryDay7UIRetention(2014, 9, 1, 2014, 9, 33));
    System.out.println("Retention for Feb28 2015: " + querySingleDay(2015, 2, 28));

    
  }
  
  
  //takes the values we got from queryDay7UIRetention method and calculates double
  private static double querySingleDay(int year, int month, int day) {
    int[] returnArray = queryDay7UIRetention(year, month, day);
    if(returnArray[0]==0 || returnArray[1] == 0) {
      return 0.0;
    }
    else return (double) returnArray[0]/ (double) returnArray[1];
  }
  
  //query single day retention with os filter
  private static double querySingleDayWithOS(int year, int month, int day, String osName) {
    int[] returnArray = queryDay7UIRetentionWithOS(year, month, day, osName);
    if(returnArray[0]==0 || returnArray[1] == 0) {
      return 0.0;
    }
    else return (double) returnArray[0]/ (double) returnArray[1];
  }
  
  //query single day retention with sdk filter
  private static double querySingleDayWithSDK(int year, int month, int day, String sdk) {
    int[] returnArray = queryDay7UIRetentionWithSDK(year, month, day, sdk);
    if(returnArray[0]==0 || returnArray[1] == 0) {
      return 0.0;
    }
    else return (double) returnArray[0]/ (double) returnArray[1];
  }
  
  //query single day retention with sdk filter
  private static double querySingleDayWithOSAndSDK(int year, int month, int day, String osName, String sdk) {
    int[] returnArray =   queryDay7UIRetentionWithOSAndSDK(year, month, day, osName, sdk);
    if(returnArray[0]==0 || returnArray[1] == 0) {
      return 0.0;
    }
    else return (double) returnArray[0]/ (double) returnArray[1];
  }
   
  
  
  
  
  
  
  
  
  
  
  //calculates the retention values we want 7 days from a specific date
  private static int[] queryDay7UIRetention(int year, int month, int day) {
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
      calendar.add(Calendar.DATE, 7);
    }
    else {
      System.out.println("Invalid date");
      return returnArray;
    }

    //int[] sevenDaysLater = giveDateSevenDaysLater(year, month, day);
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
  
  //added filtering for os
  private static int[] queryDay7UIRetentionWithOS(int year, int month, int day, String osName) {
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
            if(event.getEventName().equals("UI_OPEN_COUNT") && event.getOsName().equals(osName)) {
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

    //int[] sevenDaysLater = giveDateSevenDaysLater(year, month, day);
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
  
  //added filtering for sdk version
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

    //int[] sevenDaysLater = giveDateSevenDaysLater(year, month, day);
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
  
  //added filtering for os and sdk version
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

    //int[] sevenDaysLater = giveDateSevenDaysLater(year, month, day);
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
  
  //use previous queryDay7UIRetention method over all the days in the time frame
  private static double queryDay7UIRetention(int year1, int month1, int day1, int year2, int month2, int day2) {
    int totalDayOneUsers = 0;
    int totalDaySevenUsersRetained = 0;
    
    //make sure end date is valid
    if(!checkIfDatesValid(year2, month2, day2)) {
      System.out.println("Invalid end date");
    }
    
    
    //calculate how many times we use queryDay7UIRetention
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
  
  private static double queryDay7UIRetentionWithOS(int year1, int month1, int day1,
      int year2, int month2, int day2, String osName) {
    int totalDayOneUsers = 0;
    int totalDaySevenUsersRetained = 0;
    
    //make sure end date is valid
    if(!checkIfDatesValid(year2, month2, day2)) {
      System.out.println("Invalid end date");
    }
    
    
    //calculate how many times we use queryDay7UIRetention
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
  
  private static double queryDay7UIRetentionWithSDK(int year1, int month1, int day1,
      int year2, int month2, int day2, String sdk) {
    int totalDayOneUsers = 0;
    int totalDaySevenUsersRetained = 0;
    
    //make sure end date is valid
    if(!checkIfDatesValid(year2, month2, day2)) {
      System.out.println("Invalid end date");
    }
    
    
    //calculate how many times we use queryDay7UIRetention
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
  
  private static double queryDay7UIRetentionWithOSAndSDK(int year1, int month1, 
      int day1, int year2, int month2, int day2, String osName, String sdk) {
    int totalDayOneUsers = 0;
    int totalDaySevenUsersRetained = 0;
    
    //make sure end date is valid
    if(!checkIfDatesValid(year2, month2, day2)) {
      System.out.println("Invalid end date");
    }
    
    
    //calculate how many times we use queryDay7UIRetention
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
  
  //checks whether date inputted is valid
  private static boolean checkIfDatesValid(int year, int month, int day) {
    if(month == 2) {
      if(year%4==0) {
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
  
//  private static int[] giveDateTomorrow(int year, int month, int day) {
//    int[] date = new int[3];
//
//    boolean leapYear;
//    
//    if(year%4==0) {
//      leapYear = true;
//    }
//    else leapYear = false;
//    
//    for(int i=0; i<date.length;i++) {
//      date[i] = 0;
//    }
//    
//    if(day < 28) {
//      date[0] = year;
//      date[1] = month;
//      date[2] = day + 1;
//    }
//    else if(month == 2) {
//      if(leapYear) {
//        if(day == 28) {
//          date[2] = 29;
//          date[1] = month;
//          date[0] = year;
//        }
//        else {
//          date[2] = day+1-29;
//          date[1] = month+1;
//          date[0] = year;
//        }
//      }
//      else {
//        date[2] = 1;
//        date[1] = month + 1;
//        date[0] = year;
//      }
//    }
//    else if(month == 1 || month == 3 || month == 5 || month == 7 || month == 8
//        || month == 10 || month == 12) {
//      if(day<31) {
//        date[2] = day+1;
//        date[1] = month;
//        date[0] = year;
//      }
//      else {
//        date[2] = 1;
//        if(month == 12) {
//          date[1] = 1;
//          date[0] = year+1;
//        }
//        else {
//          date[1] = month+1;
//          date[0] = year;
//        }
//      }
//    }
//    else if(month == 4 || month == 6 || month == 9 || month == 11) {
//      if(day<30) {
//        date[2] = day +1;
//        date[1] = month;
//        date[0] = year;
//      }
//      else {
//        date[2] = 1;
//        date[1] = month+1;
//        date[0] = year;
//      }
//    }
//    
//    return date;
//  }
//  
//  
//  private static int[] giveDateSevenDaysLater(int year, int month, int day) {
//    int[] date = new int[3];
//
//    boolean leapYear;
//    
//    if(year%4==0) {
//      leapYear = true;
//    }
//    else leapYear = false;
//    
//    for(int i=0; i<date.length;i++) {
//      date[i] = 0;
//    }
//    
//    if(day < 22) {
//      date[0] = year;
//      date[1] = month;
//      date[2] = day + 7;
//    }
//    else if(month == 2) {
//      if(leapYear) {
//        if(day == 22) {
//          date[2] = 29;
//          date[1] = month;
//          date[0] = year;
//        }
//        else {
//          date[2] = day+7-29;
//          date[1] = month+1;
//          date[0] = year;
//        }
//      }
//      else {
//        date[2] = day+7-28;
//        date[1] = month + 1;
//        date[0] = year;
//      }
//    }
//    else if(month == 1 || month == 3 || month == 5 || month == 7 || month == 8
//        || month == 10 || month == 12) {
//      if(day<25) {
//        date[2] = day+7;
//        date[1] = month;
//        date[0] = year;
//      }
//      else {
//        date[2] = day+7-31;
//        if(month == 12) {
//          date[1] = 1;
//          date[0] = year+1;
//        }
//        else {
//          date[1] = month+1;
//          date[0] = year;
//        }
//      }
//    }
//    else if(month == 4 || month == 6 || month == 9 || month == 11) {
//      if(day<24) {
//        date[2] = day +7;
//        date[1] = month;
//        date[0] = year;
//      }
//      else {
//        date[2] = day+7-30;
//        date[1] = month+1;
//        date[0] = year;
//      }
//    }
//    
//    return date;
//  }
  

  

}
