package de.suse.conferenceclient.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.widget.SimpleAdapter;

import com.actionbarsherlock.app.SherlockListFragment;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.SUSEConferences;
import de.suse.conferenceclient.adapters.WhatsOnAdapter;
import de.suse.conferenceclient.app.Database;
import de.suse.conferenceclient.models.Event;

public class WhatsOnFragment extends SherlockListFragment {
	private long mConferenceId;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void setConferenceId(long id) {
		this.mConferenceId = id;
		loadEvents();
	}
	
	private void loadEvents() {
		Database db = SUSEConferences.getDatabase();
		List<Event> eventList = db.getNextTwoEvents(mConferenceId);
		WhatsOnAdapter adapter = new WhatsOnAdapter(getActivity(), R.layout.whats_on_list_item, eventList);
		setListAdapter(adapter);
	}
}
