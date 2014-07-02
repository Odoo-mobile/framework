package com.odoo.orm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.odoo.orm.OColumn.RelationType;

public class ORelationRecordList {
	HashMap<String, ORelationRecord> _relation_records = new HashMap<String, ORelationRecordList.ORelationRecord>();

	public boolean contains(String key) {
		return _relation_records.containsKey(key);
	}

	public void add(String key, ORelationRecord rel_record) {
		_relation_records.put(key, rel_record);
	}

	public ORelationRecord get(String key) {
		return _relation_records.get(key);
	}

	public Set<String> keys() {
		return _relation_records.keySet();
	}

	class ORelationRecord {

		RelationType type = null;
		OModel model = null;
		HashMap<String, List<Integer>> ref_ids = new HashMap<String, List<Integer>>();
		String ref_column = null;
		List<Integer> ids = new ArrayList<Integer>();

		public OModel getModel() {
			return model;
		}

		public void setModel(OModel model) {
			this.model = model;
		}

		public List<Integer> getIds() {
			Set<Integer> idsSet = new HashSet<Integer>(ids);
			List<Integer> ids = new ArrayList<Integer>();
			ids.addAll(idsSet);
			return ids;
		}

		public void addId(Integer id, Integer ref_id) {
			String key = model.getTableName() + "_";
			List<Integer> ref_ids_list = new ArrayList<Integer>();
			if (ref_ids.containsKey(key + id)) {
				ref_ids_list.addAll(ref_ids.get(key + id));
			}
			ref_ids_list.add(ref_id);
			ref_ids.put(key + id, ref_ids_list);
			this.ids.add(id);
		}

		public void addIds(List<Integer> ids, Integer ref_id) {
			for (Integer id : ids) {
				addId(id, ref_id);
			}
		}

		public HashMap<String, List<Integer>> getRefIds() {
			return ref_ids;
		}

		public String getRefColumn() {
			return ref_column;
		}

		public void setRefColumn(String ref_column) {
			this.ref_column = ref_column;
		}

		public RelationType getType() {
			return type;
		}

		public void setType(RelationType type) {
			this.type = type;
		}

	}

}
