package pattern.decorator.alarm;

import java.util.Calendar;

public interface AlarmInterface {
	public String getAlarm();
	public void setAlarmRemind(Boolean val);
	public void setAlarmTime(Calendar time);
	public void setChangeWithDayLight(Boolean val);
	public boolean removeDecoration(String decoration);

}