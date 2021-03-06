/*******************************************************************************
 * Copyright (c) 2012 Matt Barringer <matt@incoherent.de>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Matt Barringer <matt@incoherent.de> - initial API and implementation
 ******************************************************************************/
package de.incoherent.suseconferenceclient.fragments;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

import de.incoherent.suseconferenceclient.SUSEConferences;
import de.incoherent.suseconferenceclient.activities.ScheduleDetailsActivity;
import de.incoherent.suseconferenceclient.adapters.ScheduleAdapter;
import de.incoherent.suseconferenceclient.adapters.ScheduleAdapter.ScheduleItem;
import de.incoherent.suseconferenceclient.app.Database;
import de.incoherent.suseconferenceclient.models.Event;
import de.incoherent.suseconferenceclient.R;

public class MyScheduleFragment extends SherlockListFragment {
	public MyScheduleFragment() { }

	private Database db;
    private long mConferenceId;
    private List<Event> mEventList;
    private DateFormat mHeaderFormatter;
    private ScheduleAdapter mAdapter;
    private int mIndex = -1;
    private int mTop = 0;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setRetainInstance(true);

		mHeaderFormatter = DateFormat.getDateInstance(DateFormat.LONG);		
		Bundle args = getArguments();
		this.mConferenceId = args.getLong("conferenceId");
		this.db = SUSEConferences.getDatabase();
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setDrawSelectorOnTop(true);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		try{
			mIndex = this.getListView().getFirstVisiblePosition();
			View v = this.getListView().getChildAt(0);
			mTop = (v == null) ? 0 : v.getTop();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public void onStart() {
		super.onStart();
		getListView().setFastScrollEnabled(true);
	}
	
	public void onResume() {
		super.onResume();
		setItems();
		if(mIndex!=-1){
			this.getListView().setSelectionFromTop(mIndex, mTop);
		}
		getListView().setFastScrollEnabled(true);
	}
	
	public void loadNewConference(long conferenceId) {
		this.mConferenceId = conferenceId;
		mIndex = -1;
		mTop = 0;
		setItems();
	}

	public void setItems() {
		mAdapter = new ScheduleAdapter(getActivity(),
				false,
				R.layout.schedule_list_item,
				getResources().getColor(R.color.dark_suse_green),
				getResources().getColor(R.color.suse_grey),
				getScheduleItems());
		setListAdapter(mAdapter);
	}
	
	private List<ScheduleItem> getScheduleItems() {
		boolean isEmpty = false;
		boolean conflict = false;

		List<ScheduleItem> items = new ArrayList<ScheduleItem>();
		this.mEventList = db.getScheduleTitles(mConferenceId);
		Collections.sort(mEventList);

		if (mEventList.size() > 0) {
			ScheduleItem newItem = new ScheduleItem(buildHeaderText(mEventList.get(0)), getDayString(mEventList.get(0)));
			items.add(newItem);
			Event event;
			Event previousEvent = null;
			
			int eventLen = mEventList.size();
			for (int i = 0; i < eventLen; i++) {
				event = mEventList.get(i);
				
				conflict = false;
				isEmpty = true;
				if (previousEvent != null) {
					// If the time slot is the same as the one before,
					// and the event is *not* in My Schedule or meta information, skip it
					if (sameTime(previousEvent.getDate(), event.getDate(), previousEvent.getTimeZone(), event.getTimeZone())) {
						if (!event.isInMySchedule() && !event.isMetaInformation()) {
							continue;
						}
					}
					
					// Now, check if the times overlap *and* both events are 
					// in the schedule
					if (timesOverlap(previousEvent.getDate(),
									 previousEvent.getEndDate(),
									 event.getDate(),
									 event.getEndDate()) && previousEvent.isInMySchedule()
									 && event.isInMySchedule()) {
						conflict = true;
						items.get(items.size() - 1).setConflict(true);
					}
					
					// If we've hit a new day, add a header item to the list
					if (!sameDay(previousEvent.getDate(), event.getDate(), previousEvent.getTimeZone(), event.getTimeZone())) {
						ScheduleItem newHeader= new ScheduleItem(buildHeaderText(event), getDayString(event));
						items.add(newHeader);
					}
				}
				
				previousEvent = event;

				if (event.isInMySchedule()) {
					isEmpty = false;
				}
				
				ScheduleItem newEvent = new ScheduleItem(event, isEmpty);
				newEvent.setConflict(conflict);
				items.add(newEvent);
			}
		}
		return items;
	}
	
	private String buildHeaderText(Event event) {
		mHeaderFormatter.setTimeZone(event.getTimeZone());
		return mHeaderFormatter.format(event.getDate());
	}
	
	private String getDayString(Event e) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(e.getDate());
		return String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
	}

	
	private boolean timesOverlap(Date s1, Date e1, Date s2, Date e2) {
		return (s1.before(e2) && s2.before(e1));
	}
	
	private boolean sameDay(Date day1, Date day2, TimeZone tz1, TimeZone tz2) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTimeZone(tz1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTimeZone(tz2);
		cal1.setTime(day1);
		cal2.setTime(day2);
		boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
				cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
		return sameDay;
	}

	private boolean sameTime(Date day1, Date day2, TimeZone tz1, TimeZone tz2) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(tz1);
		cal.setTime(day1);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		cal.setTimeZone(tz2);
		cal.setTime(day2);
		int hour2 = cal.get(Calendar.HOUR_OF_DAY);
		int minute2 = cal.get(Calendar.MINUTE);
		return (hour == hour2 && minute == minute2);
	}
	
	@Override
	public void onListItemClick (ListView l,
								 View v,
								 int position,
								 long id) {
		ScheduleItem item = (ScheduleItem) l.getItemAtPosition(position);
		if (item.isHeader() || item.isEmpty() || item.getEvent().isMetaInformation())
			return;
		
		Intent intent = new Intent(getActivity(), ScheduleDetailsActivity.class);
		intent.putExtra("eventId", item.getEvent().getSqlId());
		intent.putExtra("conferenceId", mConferenceId);
		startActivity(intent);
	}

}
