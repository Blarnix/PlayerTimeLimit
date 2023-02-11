package blarnix.utils;

import java.util.Calendar;

import blarnix.managers.MessagesManager;

public class UtilsTime {

	public static String getTime(long segundos,MessagesManager msgManager) {
		long esperatotalmin = segundos/60;
		long esperatotalhour = esperatotalmin/60;
		long esperatotalday = esperatotalhour/24;
		if(segundos > 59){
			segundos = segundos - 60*esperatotalmin;
		}
		String time = segundos+msgManager.getTimeSeconds();
		if(esperatotalmin > 59){
			esperatotalmin = esperatotalmin - 60*esperatotalhour;
		}
		if(esperatotalmin > 0){
			time = esperatotalmin+msgManager.getTimeMinutes()+" "+time;
		}
		if(esperatotalhour > 24) {
			esperatotalhour = esperatotalhour - 24*esperatotalday;
		}
		if(esperatotalhour > 0){
			time = esperatotalhour+msgManager.getTimeHours()+" " + time;
		}
		if(esperatotalday > 0) {
			time = esperatotalday+msgManager.getTimeDays()+" " + time;
		}

		return time;
	}

	//Returns the millis of the next time reset
	public static long getNextResetMillis(String resetTimeHour) {
		long currentMillis = System.currentTimeMillis();

		//Bukkit.getConsoleSender().sendMessage("reset time: "+resetTimeHour);
		String[] sep = resetTimeHour.split(":");
		String hour = sep[0];
		if(hour.startsWith("0")) {
			hour = hour.charAt(1)+"";
		}
		String minute = sep[1];
		if(minute.startsWith("0")) {
			minute = minute.charAt(1)+"";
		}

		Calendar calendar = Calendar.getInstance();
	    calendar.setTimeInMillis(currentMillis);
	    calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hour));
	    calendar.set(Calendar.MINUTE, Integer.valueOf(minute));
	    calendar.set(Calendar.SECOND, 0);

	    if(calendar.getTimeInMillis() >= currentMillis) {
	    	//It still hasn't reached the restart time on the day
	    	//Bukkit.getConsoleSender().sendMessage("Reset time: "+hour+":"+minute+"   | Not completed");
	    	return calendar.getTimeInMillis();
	    }else {
	    	//La hora de reinicio ya paso en el dia
	    	//Bukkit.getConsoleSender().sendMessage("Reset time: "+hour+":"+minute+"   | Completed");
	    	calendar.add(Calendar.DAY_OF_YEAR, 1);
	    	//Bukkit.getConsoleSender().sendMessage("New date: "+calendar.toString());
	    	return calendar.getTimeInMillis();
	    }
	}
}
