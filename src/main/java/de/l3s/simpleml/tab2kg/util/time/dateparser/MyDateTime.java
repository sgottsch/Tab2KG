package de.l3s.simpleml.tab2kg.util.time.dateparser;

import java.util.Date;

public class MyDateTime {

	private Date date;

	private Boolean hasDate;
	private Boolean hasTime;

	public MyDateTime(Date date, boolean hasDate, boolean hasTime) {
		super();
		this.date = date;
		this.hasDate = hasDate;
		this.hasTime = hasTime;
	}

	public Date getDate() {
		return date;
	}

	public boolean hasDate() {
		return hasDate;
	}

	public boolean hasTime() {
		return hasTime;
	}

}
