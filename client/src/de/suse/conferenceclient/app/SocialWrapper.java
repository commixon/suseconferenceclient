package de.suse.conferenceclient.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.text.format.DateUtils;
import de.suse.conferenceclient.Config;
import de.suse.conferenceclient.R;
import de.suse.conferenceclient.models.SocialItem;

public class SocialWrapper {
	public static class DateCompare implements Comparator<SocialItem> {
	    @Override
	    public int compare(SocialItem one, SocialItem two) {
	        return one.getDate().compareTo(two.getDate());
	    }
	}
	
	public static List<SocialItem> getTwitterItems(Context context, String tag) {
		String twitterSearch = "http://search.twitter.com/search.json?q=" + tag;
		List<SocialItem> socialItems = new ArrayList<SocialItem>();
		SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
		Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.twitter_icon);
		try {
			JSONObject result = HTTPWrapper.get(twitterSearch);
			JSONArray items = result.getJSONArray("results");
			int len = items.length();
			for (int i = 0; i < len; i++) {
				JSONObject jsonItem = items.getJSONObject(i);
				Bitmap image = HTTPWrapper.getImage(jsonItem.getString("profile_image_url"));			
				Date formattedDate = new Date();
				try {
					formattedDate = formatter.parse(jsonItem.getString("created_at"));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				SocialItem newItem = new SocialItem(SocialItem.TWITTER,
													jsonItem.getString("from_user"),
													jsonItem.getString("text"),
													formattedDate,
													DateUtils.formatDateTime(context,
												 	         formattedDate.getTime(),
												 	         DateUtils.FORMAT_SHOW_WEEKDAY
												 	        |DateUtils.FORMAT_NUMERIC_DATE
															|DateUtils.FORMAT_SHOW_TIME
															|DateUtils.FORMAT_SHOW_DATE),
													image,
													icon);

				socialItems.add(newItem);
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return socialItems;
	}

	public static List<SocialItem> getGooglePlusItems(Context context, String tag) {
		String twitterSearch = "https://www.googleapis.com/plus/v1/activities?orderBy=recent&query=" + tag + "&key=" + Config.PLUS_KEY;
		List<SocialItem> socialItems = new ArrayList<SocialItem>();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

		Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.google_icon);

		try {
			JSONObject result = HTTPWrapper.get(twitterSearch);
			JSONArray items = result.getJSONArray("items");
			int len = items.length();
			for (int i = 0; i < len; i++) {
				JSONObject jsonItem = items.getJSONObject(i);
				JSONObject actorItem = jsonItem.getJSONObject("actor");
				JSONObject imageItem = actorItem.getJSONObject("image");
				JSONObject objectItem = jsonItem.getJSONObject("object");
				Bitmap image = HTTPWrapper.getImage(imageItem.getString("url"));
				String content = Html.fromHtml(objectItem.getString("content")).toString();
				Date formattedDate = new Date();
				try {
					formattedDate = formatter.parse(jsonItem.getString("published"));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				SocialItem newItem = new SocialItem(SocialItem.GOOGLE,
													actorItem.getString("displayName"),
													content,
													formattedDate,
													DateUtils.formatDateTime(context,
												 	         formattedDate.getTime(),
												 	         DateUtils.FORMAT_SHOW_WEEKDAY
												 	        |DateUtils.FORMAT_NUMERIC_DATE
															|DateUtils.FORMAT_SHOW_TIME
															|DateUtils.FORMAT_SHOW_DATE),
													image,
													icon);

				socialItems.add(newItem);
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return socialItems;
	}
}
