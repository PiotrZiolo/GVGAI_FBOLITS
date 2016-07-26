package NextLevel;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class LogHandler
{
	public static final boolean bLoggingOn = true; // Turn on or off all logs
	private static final int iTarget = 3; // Select target for printing logs: 1 - screen, 2 - file, 3 - screen & file
	private static final boolean bStartANewLog = true;
	private static final boolean bPrintDateTime = true;

	private LogHandler()
	{

	}

	/**
	 * Erases contents of the log file if bStartANewLog is set.
	 * 
	 */
	public static void clearLog()
	{
		if (bLoggingOn)
		{
			if (((iTarget >> 1) & 1) == 1)
			{
				if (bStartANewLog)
				{
					try (Writer writer = new BufferedWriter(
							new OutputStreamWriter(new FileOutputStream("NextLevel.log", false), "utf-8")))
					{

					}
					catch (Exception e)
					{

					}
				}
			}
		}
	}

	/**
	 * Writes log.
	 * 
	 * @param sText
	 *            Text of the log message.
	 * @param sOrigin
	 *            Origin of the message. Convention: ClassName.methodName.
	 * @param iWrite
	 *            0 - do not write, 1 - write on screen, 2 - write to a file, 3
	 *            - write to both.
	 */
	public static boolean writeLog(String sText, String sOrigin, int iWrite)
	{
		boolean bResult = true;

		if (bLoggingOn && (iWrite != 0))
		{
			if (((iTarget >> 1) & 1) == 1 && ((iWrite >> 1) & 1) == 1)
			{
				try (Writer writer = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream("NextLevel.log", true), "utf-8")))
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
