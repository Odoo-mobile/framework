package com.odoo.orm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.odoo.orm.OColumn.RelationType;

public class ORelationRecordList {
	HashMap<String, ORelationRecords> _relation_records = new HashMap<String, ORelationRecordList.ORelationRecords>();

	public boolean contains(String key) {
		return _relation_records.containsKey(key);
	}

	public void add(String key, ORelationRecords rel_record) {
		_relation_records.put(key, rel_record);
	}

	public ORelationRecords get(String key) {
		return _relation_records.get(key);
	}

	public Set<String> keys() {
		return _relation_records.keySet();
	}

	public class ORelationRecords {
		private OModel base_model = null;
		private OModel rel_model = null;
		private String ref_column = null;
		private RelationType relation_type = null;
		private HashMap<String, Integer> base_ids = new HashMap<String, Integer>();
		private HashMap<String, List<Integer>> base_rel_ids = new HashMap<String, List<Integer>>();

		public void setRelationType(RelationType type) {
			relation_type = type;
		}

		public RelationType getRelationType() {
			return relation_type;
		}

		public void addBaseRelId(Integer base_id, Integer rel_id) {
			List<Integer> rel_ids = new ArrayList<Integer>();
			rel_ids.add(rel_id);
			addBaseRelId(base_id, rel_ids);
		}

		public void addBaseRelId(Integer base_id, List<Integer> rel_id) {
			String key = base_model.getTableName() + "_base_" + base_id;
			base_ids.put(key, base_id);
			List<Integer> rel_ids = new ArrayList<Integer>();
			if (base_rel_ids.containsKey(key)) {
				rel_ids.addAll(base_rel_ids.get(key));
			}
			rel_ids.addAll(rel_id);
			base_rel_ids.put(key, rel_ids);
		}

		public void setBaseModel(OModel model) {
			base_model = model;
		}

		public OModel getBaseModel() {
			return base_model;
		}

		public void setRelModel(OModel model) {
			rel_model = model;
		}

		public OModel getRelModel() {
			return rel_model;
		}

		public List<String> getBaseIdsKeySet() {
			List<String> base_ids_keyset = new ArrayList<String>();
			base_ids_keyset.addAll(base_ids.keySet());
			return base_ids_keyset;
		}

		public Integer getBaseId(String base_key) {
			return base_ids.get(base_key);
		}

		public List<Integer> getBaseIds() {
			HashSet<Integer> ids = new HashSet<Integer>();
			List<Integer> base_ids = new ArrayList<Integer>();
			for (String key : getBaseIdsKeySet())
				ids.add(this.base_ids.get(key));
			base_ids.addAll(ids);
			return base_ids;
		}

		public List<Integer> getRelIds() {
			List<Integer> rel_ids = new ArrayList<Integer>();
			HashSet<Integer> ids = new HashSet<Integer>();
			for (String key : getBaseIdsKeySet()) {
				ids.addAll(getRelIds(key));
			}
			rel_ids.addAll(ids);
			return rel_ids;
		}

		public List<Integer> getRelIds(String base_key) {
			return base_rel_ids.get(base_key);
		}

		public void setRefColumn(String column) {
			ref_column = column;
		}

		public String getRefColumn() {
			return ref_column;
		}
	}
}
