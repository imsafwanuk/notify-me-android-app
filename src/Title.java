package pattern.decorator.alarm;

import java.util.Calendar;

class Title extends AlarmDecorator {

	Title(AlarmInterface simpleAlarm, String title) {
		alarm = simpleAlarm;
		alarmContent = title;
	}
	
	@Override
	public String getAlarm() {
		return alarm.getAlarm()+"$title#"+alarmContent+"|";
	}
	
	@Override
	public void setAlarmRemind(Boolean val) {
		alarm.setAlarmRemind(val);
	}
	
	@Override
	public void setAlarmTime(Calendar time) {
		alarm.setAlarmTime(time);
		
	}
	
	@Override
	public void setChangeWithDayLight(Boolean val) {
		alarm.setChangeWithDayLight(val);
	}
	
	@Override
	public boolean removeDecoration(String decoration) {
		alarm.removeDecoration(decoration);
		return false;
	}
	
}