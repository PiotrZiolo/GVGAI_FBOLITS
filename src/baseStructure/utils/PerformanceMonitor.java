package baseStructure.utils;

public class PerformanceMonitor
{
	private long nanoTimeStart;
	private long nanoTimeFinish;
	private long miliTimeStart;
	private long miliTimeFinish;
	
	public PerformanceMonitor()
	{
		nanoTimeStart = 0;
		nanoTimeFinish = 0;
		miliTimeStart = 0;
		miliTimeFinish = 0;
	}
	
	public void startNanoMeasure(String sMessage, String sOrigin, int iWrite)
	{
		nanoTimeStart = System.nanoTime();
		miliTimeStart = System.currentTimeMillis();
		LogHandler.writeLog(sMessage, "PerformanceMonitor|" + sOrigin, iWrite);
	}
	
	public void finishNanoMeasure(String sMessage, String sOrigin, int iWrite)
	{
		nanoTimeFinish = System.nanoTime();
		miliTimeFinish = System.currentTimeMillis();
		LogHandler.writeLog(sMessage + " | Time: " + (miliTimeFinish - miliTimeStart)  + " milis "  
				+ (((nanoTimeFinish - nanoTimeStart) / 1000) % 1000) + " mics " + ((nanoTimeFinish - nanoTimeStart) % 1000) + " nanos", "PerformanceMonitor|" + sOrigin, iWrite);
	}
	
	public void startMiliMeasure(String sMessage, String sOrigin, int iWrite)
	{
		miliTimeStart = System.currentTimeMillis();
		LogHandler.writeLog(sMessage, "PerformanceMonitor|" + sOrigin, iWrite);
	}
	
	public void finishMiliMeasure(String sMessage, String sOrigin, int iWrite)
	{
		miliTimeFinish = System.currentTimeMillis();
		LogHandler.writeLog(sMessage + " | Time: " + (miliTimeFinish - miliTimeStart) + " milis", "PerformanceMonitor|" + sOrigin, iWrite);
	}
}
