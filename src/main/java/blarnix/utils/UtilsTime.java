package blarnix.utils;

import java.util.Calendar;

import blarnix.managers.MessagesManager;

public class UtilsTime {

	public static String getTime(long seconds,MessagesManager msgManager) {
		long waittotalmin = seconds/60;
		long waittotalhour = waittotalmin/60;
		long waittotalday = waittotalhour/24;
		if(seconds > 59){
			seconds = seconds - 60*waittotalmin;
		}
		String time = seconds+msgManager.getTimeSeconds();
		if(waittotalmin > 59){
			waittotalmin = waittotalmin - 60*waittotalhour;
		}
		if(waittotalmin > 0){
			time = waittotalmin+msgManager.getTimeMinutes()+" "+time;
		}
		if(waittotalhour > 24) {
			waittotalhour = waittotalhour - 24*waittotalday;
		}
		if(waittotalhour > 0){
			time = waittotalhour+msgManager.getTimeHours()+" " + time;
		}
		if(waittotalday > 0) {
			time = waittotalday+msgManager.getTimeDays()+" " + time;
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
	    	//The reset time has already passed in the day
	    	//Bukkit.getConsoleSender().sendMessage("Reset time: "+hour+":"+minute+"   | Completed");
	    	calendar.add(Calendar.DAY_OF_YEAR, 1);
	    	//Bukkit.getConsoleSender().sendMessage("New date: "+calendar.toString());
	    	return calendar.getTimeInMillis();
	    }
	}
}
