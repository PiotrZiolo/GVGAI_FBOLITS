package FBOLITS.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public void startNanoMeasure(String message, Class origin, int iWrite)
	{
		writeLog(message, origin, iWrite);
		nanoTimeStart = System.nanoTime();
		miliTimeStart = System.currentTimeMillis();
	}

	public void finishNanoMeasure(String message, Class origin, int iWrite)
	{
		nanoTimeFinish = System.nanoTime();
		miliTimeFinish = System.currentTimeMillis();
		writeLog(message + " | Time: " + (miliTimeFinish - miliTimeStart) + " milis "
				+ (((nanoTimeFinish - nanoTimeStart) / 1000) % 1000) + " mics "
				+ ((nanoTimeFinish - nanoTimeStart) % 1000) + " nanos", origin, iWrite);
	}

	public void startMiliMeasure(String message, Class origin, int iWrite)
	{
		writeLog(message, origin, iWrite);
		miliTimeStart = System.currentTimeMillis();
	}

	public void finishMiliMeasure(String message, Class origin, int iWrite)
	{
		miliTimeFinish = System.currentTimeMillis();
		writeLog(message + " | Time: " + (miliTimeFinish - miliTimeStart) + " milis", origin, iWrite);
	}

	public void writeLog(String message, Class origin, int iWrite)
	{
		if (iWrite != 0)
		{
			Logger logger = LoggerFactory.getLogger(origin);
			logger.info(message);
		}
	}
}
