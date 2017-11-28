package pattern.decorator.alarm;

abstract class AlarmDecorator implements AlarmInterface{
	protected AlarmInterface alarm;
	protected String alarmContent;
	
}