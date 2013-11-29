package com.openerp.util.drawer;

import java.io.Serializable;

import android.widget.AdapterView.OnItemClickListener;

public class DrawerItem implements Serializable {

	private static final long serialVersionUID = 1L;
	private String key_tag = null;
	private int id = 0;
	private String title = "";
	private int counter = 0;
	private boolean isGroupTitle = false;
	private int icon = 0;
	private String tagColor = null;
	private Object instance = null;

	public DrawerItem(String key, String title) {
		this.id = DrawerItemCounter.getNext();
		this.title = title;
		this.key_tag = key;
	}

	public DrawerItem(String key, String title, int counter) {
		this.id = DrawerItemCounter.getNext();
		this.title = title;
		this.counter = counter;
		this.key_tag = key;
	}

	public DrawerItem(String key, String title, int counter, int icon) {
		this.id = DrawerItemCounter.getNext();
		this.title = title;
		this.counter = counter;
		this.icon = icon;
		this.key_tag = key;
	}

	public DrawerItem(String key, String title, int counter, int icon,
			Object instance) {
		this.id = DrawerItemCounter.getNext();
		this.key_tag = key;
		this.title = title;
		this.counter = counter;
		this.icon = icon;
		this.instance = instance;
	}

	public DrawerItem(String key, String title, int counter, String tagColor,
			Object instance) {
		this.key_tag = key;
		this.id = DrawerItemCounter.getNext();
		this.title = title;
		this.counter = counter;
		this.tagColor = tagColor;
		this.instance = instance;
	}

	public DrawerItem(String key, String title, int counter, String tagColor) {
		this.key_tag = key;
		this.id = DrawerItemCounter.getNext();
		this.title = title;
		this.counter = counter;
		this.tagColor = tagColor;
	}

	public DrawerItem(String key, String title, boolean isGroupTitle) {
		this.id = DrawerItemCounter.getNext();
		this.key_tag = key;
		this.title = title;
		this.isGroupTitle = isGroupTitle;
	}

	public DrawerItem(String key, String title, int counter,
			boolean isGroupTitle) {
		this.key_tag = key;
		this.id = DrawerItemCounter.getNext();
		this.title = title;
		this.counter = counter;
		this.isGroupTitle = isGroupTitle;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	public void isGroupTitle(boolean flag) {
		this.isGroupTitle = flag;
	}

	public boolean isGroupTitle() {
		return this.isGroupTitle;
	}

	public void setIcon(int resource) {
		icon = resource;
	}

	public int getIcon() {
		return icon;
	}

	public void setTagColor(String tagColor) {
		this.tagColor = tagColor;
	}

	public String getTagColor() {
		return this.tagColor;
	}

	public void setFragmentInstace(Object instance) {
		this.instance = instance;
	}

	public Object getFragmentInstace() {
		return this.instance;
	}

	public void setKey(String key) {
		this.key_tag = key;
	}

	public String getKey() {
		return this.key_tag;
	}

	public interface DrawerItemClickListener extends OnItemClickListener {
	}
}

class DrawerItemCounter implements Serializable {

	private static final long serialVersionUID = 1L;
	public static int counter = 0;

	public static int getNext() {
		return counter++;
	}

}