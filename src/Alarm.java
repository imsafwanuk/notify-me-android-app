package pattern.decorator.alarm;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SimpleAlarm implements AlarmInterface {
	Calendar alarmTime;
	Boolean remind;	// alarm on or off
	/* 
		before day light alarm at 12:30pm, 
	 	if change is TRUE and time increases by 1hr, 
	 	then alarm changes to 1:30pm
	 */
	boolean changeWithDayLightSavings;	
	String alarmContent = "";


	SimpleAlarm(Calendar time, Boolean changeWithDayLight) {
		this.setAlarmRemind(true);
		this.setAlarmTime(time);
		this.setChangeWithDayLight(changeWithDayLight);
	}

	
	public void setAlarmRemind(Boolean val) {
		this.remind = val;
		this.removeDecoration("remind");
		alarmContent += "$remind@"+Boolean.toString(this.remind)+"|";
	}
	
	public void setChangeWithDayLight(Boolean val) {
		this.changeWithDayLightSavings = val;
		this.removeDecoration("changeWithDayLightSavings");
		alarmContent += "changeWithDayLightSavings@"+Boolean.toString(this.changeWithDayLightSavings)+"|";
	}
	
	public void setAlarmTime(Calendar time) {
		alarmTime = time;
		this.removeDecoration("alarmTime");
		alarmContent += "$alarmTime@"+alarmTime.get(Calendar.HOUR)+"."+alarmTime.get(Calendar.MINUTE)+"."+alarmTime.get(Calendar.AM_PM)+"|";
	}
	
	
	/**
	 * Funtion: returns the description/information of an alarm
	 * Stimuli: mostly called by decorators to get existing alarm content.
	 * Return: alarmContent:String
	 */
	public String getAlarm() {
		return alarmContent;
	}

	public boolean removeDecoration(String decoration) {
		System.out.println(alarmContent);
		/* \\$"+decoration+"@ = any that starts with $, has decor word and ends with @
		 * (?:)\\| = ends with | and before | there can be 1 of 2 things
		 * ((\\d+(?:\\.\\d+?)(?:\\.\\d+)) = 1 can be either numbers or decimals 
		 * |(\\w{1,}) = or 2, just letters
		 */ 
		Pattern regexExp = Pattern.compile("(\\$"+decoration+"@(?:((\\d+(?:\\.\\d+?)(?:\\.\\d+))|(\\w{1,}))))\\|");
		Matcher regexMatcher = regexExp.matcher(alarmContent);
		if(regexMatcher.find()) {
			System.out.println(regexMatcher.group());
			alarmContent = regexMatcher.replaceFirst("");
			System.out.println(alarmContent);
			return true;
		}
		return false;
	}
}

