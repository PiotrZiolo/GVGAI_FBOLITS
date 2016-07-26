package NextLevel;

public final class PerformanceMonitor
{
	private static long nanoTimeStart;
	private static long nanoTimeFinish;
	private static long miliTimeStart;
	private static long miliTimeFinish;
	
	private PerformanceMonitor()
	{
		
	}
	
	public static void startNanoMeasure(String sMessage, String sOrigin, int iWrite)
	{
		nanoTimeStart = System.nanoTime();
		LogHandler.writeLog(sMessage, "PerformanceMonitor>" + sOrigin, iWrite);
	}
	
	public static void finishNanoMeasure(String sMessage, String sOrigin, int iWrite)
	{
		nanoTimeFinish = System.nanoTime();
		LogHandler.writeLog(sMessage + " | Time [ns]: " + (nanoTimeFinish - nanoTimeStart), "PerformanceMonitor>" + sOrigin, iWrite);
	}
	
	public static void startMiliMeasure(String sMessage, String sOrigin, int iWrite)
	{
		miliTimeStart = System.currentTimeMillis();
		LogHandler.writeLog(sMessage, "PerformanceMonitor>" + sOrigin, iWrite);
	}
	
	public static void finishMiliMeasure(String sMessage, String sOrigin, int iWrite)
	{
		miliTimeFinish = System.currentTimeMillis();
		LogHandler.writeLog(sMessage + " | Time [ms]: " + (miliTimeFinish - miliTimeStart), "PerformanceMonitor>" + sOrigin, iWrite);
	}
}
