package lbd.FSNER.Utils;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Date;

public class SimpleStopWatch implements Serializable {

	private static final long serialVersionUID = 1L;
	protected Date startDate;
	protected Date endDate;

	public void start() {
		startDate = new Date();
	}

	public void stop() {
		endDate = new Date();
	}

	public void show(String title) {

		stop();

		double elipsedTime = endDate.getTime() - startDate.getTime();

		System.out.println(title + Symbol.SPACE + formatSimpleTime(elipsedTime) + "s");
	}

	protected String formatSimpleTime(double time) {
		return((new DecimalFormat("#.##")).format(time/1000));
	}

}
