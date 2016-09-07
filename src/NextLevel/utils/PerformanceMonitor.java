package NextLevel.utils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PerformanceMonitor
{
	public static final boolean bLoggingOn = true; // Turn on or off all logs
	private static final int iTarget = 1; // Select target for printing logs: 1 - screen, 2 - file, 3 - screen & file
	private static final boolean bStartANewLog = true;
	private static final boolean bPrintDateTime = true;
	
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
		writeLog(sMessage, "PerformanceMonitor|" + sOrigin, iWrite);
		nanoTimeStart = System.nanoTime();
		miliTimeStart = System.currentTimeMillis();
	}
	
	public void finishNanoMeasure(String sMessage, String sOrigin, int iWrite)
	{
		nanoTimeFinish = System.nanoTime();
		miliTimeFinish = System.currentTimeMillis();
		writeLog(sMessage + " | Time: " + (miliTimeFinish - miliTimeStart)  + " milis "  
				+ (((nanoTimeFinish - nanoTimeStart) / 1000) % 1000) + " mics " + ((nanoTimeFinish - nanoTimeStart) % 1000) + " nanos", "PerformanceMonitor|" + sOrigin, iWrite);
	}
	
	public void startMiliMeasure(String sMessage, String sOrigin, int iWrite)
	{
		writeLog(sMessage, "PerformanceMonitor|" + sOrigin, iWrite);
		miliTimeStart = System.currentTimeMillis();
	}
	
	public void finishMiliMeasure(String sMessage, String sOrigin, int iWrite)
	{
		miliTimeFinish = System.currentTimeMillis();
		writeLog(sMessage + " | Time: " + (miliTimeFinish - miliTimeStart) + " milis", "PerformanceMonitor|" + sOrigin, iWrite);
	}
	
	public static void clearLog()
	{
		if (bLoggingOn)
		{
			if (((iTarget >> 1) & 1) == 1)
			{
				if (bStartANewLog)
				{
					try (Writer writer = new BufferedWriter(
							new OutputStreamWriter(new FileOutputStream("src/NextLevel/NextLevel.log", false), "utf-8")))
					{

					}
					catch (Exception e)
					{

					}
				}
			}
		}
	}
	
	public static boolean writeLog(String sText, String sOrigin, int iWrite)
	{
		boolean bResult = true;

		if (bLoggingOn && (iWrite != 0))
		{
			if (((iTarget >> 1) & 1) == 1 && ((iWrite >> 1) & 1) == 1)
			{
				try (Writer writer = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream("src/NextLevel/NextLevel.log", true), "utf-8")))
				{
					DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss:SSSS");
					Date date = new Date();

					if (bPrintDateTime)
					{
						writer.write("[" + dateFormat.format(date) + "] ");
					}
					if (sOrigin != "")
					{
						writer.write("<" + sOrigin + "> ");
					}
					writer.write(sText);
					writer.write(System.getProperty("line.separator"));
				}
				catch (Exception e)
				{
					bResult = false;
				}
			}

			if ((iTarget & 1) == 1 && (iWrite & 1) == 1)
			{
				DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss:SSSS");
				Date date = new Date();

				if (bPrintDateTime)
				{
					System.out.print("[" + dateFormat.format(date) + "] ");
				}
				if (sOrigin != "")
				{
					System.out.print("<" + sOrigin + "> ");
				}
				System.out.println(sText);
			}
		}

		return bResult;
	}
}
