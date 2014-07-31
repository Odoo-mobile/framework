package com.odoo.orm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.odoo.orm.OModel.Command;

public class ORelIds {
	HashMap<String, RelData> mRelationIds = new HashMap<String, RelData>();
	Integer base_id = null;

	public ORelIds(Integer base_id) {
		this.base_id = base_id;
	}

	public ORelIds add(List<Integer> ids, Command command) {
		String key = "KEY_" + command.toString();
		mRelationIds.put(key, new RelData(ids, command));
		return this;
	}

	public List<String> keys() {
		List<String> keys = new ArrayList<String>();
		keys.addAll(mRelationIds.keySet());
		return keys;
	}

	public RelData get(String key) {
		if (mRelationIds.containsKey(key)) {
			return mRelationIds.get(key);
		}
		return null;
	}

	public Integer getBaseId() {
		return base_id;
	}

	public class RelData {
		Command command = null;
		List<Integer> ids = new ArrayList<Integer>();

		public RelData(List<Integer> ids, Command command) {
			this.ids.addAll(ids);
			this.command = command;
		}

		public Command getCommand() {
			return command;
		}

		public void setCommand(Command command) {
			this.command = command;
		}

		public List<Integer> getIds() {
			return ids;
		}

		public void setIds(List<Integer> ids) {
			this.ids = ids;
		}

	}

}
