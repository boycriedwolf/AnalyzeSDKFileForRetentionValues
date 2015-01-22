
public class Event {

  private int eventId;
  private int eventCount;
  private String eventName;
  private String eventTime;
  private String osName;
  private String sdkVersion;
  private int userId;

  public Event(int eventId, int eventCount, String eventName,
      String eventTime, String osName, String sdkVersion, int userId) {
    super();
    this.eventId = eventId;
    this.eventCount = eventCount;
    this.eventName = eventName;
    this.eventTime = eventTime;
    this.osName = osName;
    this.sdkVersion = sdkVersion;
    this.userId = userId;
  }

  public int[] convertTimeToInts() {
    int[] timeArray = new int[3];
    for(int i=0; i< timeArray.length; i++) {
      timeArray[i] = 0;
    }

    String[] stringArray = this.eventTime.split("-"); //first two elements should represent year/month
    String[] dayArray = stringArray[2].split(" "); //first element represents day, rest is hh/mm/ss

    timeArray[0] = Integer.parseInt(stringArray[0]);
    timeArray[1] = Integer.parseInt(stringArray[1]);
    timeArray[2] = Integer.parseInt(dayArray[0]);

    return timeArray;
  }

  public int getEventId() {
    return eventId;
  }
  public void setEventId(int eventId) {
    this.eventId = eventId;
  }
  public int getEventCount() {
    return eventCount;
  }
  public void setEventCount(int eventCount) {
    this.eventCount = eventCount;
  }
  public String getEventName() {
    return eventName;
  }
  public void setEventName(String eventName) {
    this.eventName = eventName;
  }
  public String getEventTime() {
    return eventTime;
  }
  public void setEventTime(String eventTime) {
    this.eventTime = eventTime;
  }
  public String getOsName() {
    return osName;
  }
  public void setOsName(String osName) {
    this.osName = osName;
  }
  public String getSdkVersion() {
    return sdkVersion;
  }
  public void setSdkVersion(String sdkVersion) {
    this.sdkVersion = sdkVersion;
  }
  public int getUserId() {
    return userId;
  }
  public void setUserId(int userId) {
    this.userId = userId;
  }

  @Override
  public String toString() {
    return "Event [eventId=" + eventId + ", eventCount=" + eventCount
        + ", eventName=" + eventName + ", eventTime=" + eventTime
        + ", osName=" + osName + ", sdkVersion=" + sdkVersion
        + ", userId=" + userId + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + eventCount;
    result = prime * result + eventId;
    result = prime * result + ((eventName == null) ? 0 : eventName.hashCode());
    result = prime * result + ((eventTime == null) ? 0 : eventTime.hashCode());
    result = prime * result + ((osName == null) ? 0 : osName.hashCode());
    result = prime * result
        + ((sdkVersion == null) ? 0 : sdkVersion.hashCode());
    result = prime * result + userId;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Event other = (Event) obj;
    if (eventCount != other.eventCount)
      return false;
    if (eventId != other.eventId)
      return false;
    if (eventName == null) {
      if (other.eventName != null)
        return false;
    } else if (!eventName.equals(other.eventName))
      return false;
    if (eventTime == null) {
      if (other.eventTime != null)
        return false;
    } else if (!eventTime.equals(other.eventTime))
      return false;
    if (osName == null) {
      if (other.osName != null)
        return false;
    } else if (!osName.equals(other.osName))
      return false;
    if (sdkVersion == null) {
      if (other.sdkVersion != null)
        return false;
    } else if (!sdkVersion.equals(other.sdkVersion))
      return false;
    if (userId != other.userId)
      return false;
    return true;
  }

}
