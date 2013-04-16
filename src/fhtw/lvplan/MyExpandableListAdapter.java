/**
 * FHTW App – CIS Plattform of the UAS Technikum Vienna 4 Android devices
 * Copyright (C) 2013  Stefan Leonhartsberger
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * License can be found in project root: LICENSE
 */
package fhtw.lvplan;

import java.util.ArrayList;
import java.util.List;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

/**
 * Own Implementation of an Expandable List Component
 */
public class MyExpandableListAdapter extends BaseExpandableListAdapter {
	// Sample data set. children[i] contains the children (String[]) for
	// groups[i].
	private List<String> groups = new ArrayList<String>();
	private List<String[]> children = new ArrayList<String[]>();
	private LV_PlanActivity mainActivity;
	
	public MyExpandableListAdapter(LV_PlanActivity mainActivity, List<String> groups, List<String[]> children)
	{
		this.mainActivity = mainActivity;
		this.groups = groups;
		this.children = children;
	}
	
	/**
	 * GET SPECIFIC CHILD FROM GROUPNODE
	 */
	public String getChild(int groupPosition, int childPosition) {
	    return children.get(groupPosition)[childPosition];
	}
	
	/**
	 * NOT USED --> HAS TO BE IMPLEMENTED... (
	 */
	public long getChildId(int groupPosition, int childPosition) {
	    return childPosition;
	}
	
	/**
	 * RETURNS NO of children for a groupnode.
	 */
	public int getChildrenCount(int groupPosition) {
	    return children.get(groupPosition).length;
	}
	
	/**
	 * 
	 * @return
	 */
	public TextView getGenericView() {
	    // Layout parameters for the ExpandableListView
	    AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 64);
	
	    TextView textView = new TextView(mainActivity);
	    textView.setLayoutParams(lp);
	    // Center the text vertically
	    textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
	    //textView.setTextColor(R.color.marcyred);
	    // Set the text starting position
	    textView.setPadding(36, 0, 0, 0);
	    return textView;
	}
	
	public View getChildView(int groupPosition, int childPosition,
	    boolean isLastChild, View convertView, ViewGroup parent) {
	    TextView textView = getGenericView();
	    textView.setText(getChild(groupPosition, childPosition));
	    return textView;
	}
	
	public Object getGroup(int groupPosition) {
	    return groups.get(groupPosition);
	}
	
	public int getGroupCount() {
	    return groups.size();
	}
	
	public long getGroupId(int groupPosition) {
	    return groupPosition;
	}
	
	public View getGroupView(int groupPosition, boolean isExpanded,
	    View convertView, ViewGroup parent) {
	    TextView textView = getGenericView();
	    try{
	    	textView.setText(getGroup(groupPosition).toString());
	    } catch(Exception ignore){}
	    return textView;
	}
	
	public boolean isChildSelectable(int groupPosition, int childPosition) {
	    return true;
	}
	
	public boolean hasStableIds() {
	    return true;
	}

}