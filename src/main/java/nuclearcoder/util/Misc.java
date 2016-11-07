package nuclearcoder.util;

public class Misc {
	
	public static final String beautifySeconds(int seconds)
	{
		int minutes = seconds / 60;
		int hours = minutes / 60;
		seconds %= 60;
		minutes %= 60;

		StringBuilder sb = new StringBuilder(hours > 0 ? 4 : 8);
		
		if (hours > 0)
		{
			sb.append(hours);
			sb.append(':');
			if (minutes < 10)
				sb.append('0');
		}
		sb.append(minutes);
		sb.append(':');
		if (seconds < 10)
			sb.append('0');
		sb.append(seconds);
		
		return sb.toString();
	}
	
}
