package com.openerp.orm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.openerp.orm.OEM2MIds.Operation;

public class OEFieldsHelper {
	JSONObject mFields = new JSONObject();
	List<OEValues> mValues = new ArrayList<OEValues>();
	List<OEColumn> mColumns = new ArrayList<OEColumn>();
	OERelRecord mRelRecord = new OERelRecord();

	public OEFieldsHelper(String[] fields) {
		addAll(fields);
	}

	public OEFieldsHelper(List<OEColumn> cols) {
		addAll(cols);
		mColumns.addAll(cols);
		mColumns.add(new OEColumn("id", "id", OEFields.integer()));
	}

	public OEFieldsHelper(JSONArray records) {
		addAll(records);
	}

	public void addAll(JSONArray records) {
		try {
			for (int i = 0; i < records.length(); i++) {
				JSONObject record = records.getJSONObject(i);
				OEValues cValue = new OEValues();
				for (OEColumn col : mColumns) {
					if (col.canSync()) {
						String key = col.getName();
						Object value = record.get(key);
						if (col.getType() instanceof OEManyToOne) {
							if (value instanceof JSONArray) {
								JSONArray m2oRec = new JSONArray(
										value.toString());
								value = m2oRec.get(0);
								OEManyToOne m2o = (OEManyToOne) col.getType();
								OEDatabase db = (OEDatabase) m2o.getDBHelper();
								mRelRecord.add(db, value);
							}
						}
						if (col.getType() instanceof OEManyToMany) {
							if (value instanceof JSONArray) {
								JSONArray m2mRec = new JSONArray(
										value.toString());
								List<Integer> ids = getIdsList(m2mRec);
								OEM2MIds mIds = new OEM2MIds(Operation.REPLACE,
										ids);
								value = mIds;
								OEManyToMany m2m = (OEManyToMany) col.getType();
								OEDatabase db = (OEDatabase) m2m.getDBHelper();
								mRelRecord.add(db, ids);
							}
						}
						cValue.put(key, value);
					}
				}
				mValues.add(cValue);
			}
		} catch (Exception e) {
		}
	}

	private List<Integer> getIdsList(JSONArray array) {
		List<Integer> ids = new ArrayList<Integer>();
		try {
			for (int i = 0; i < array.length(); i++) {
				ids.add(array.getInt(i));
			}
		} catch (Exception e) {
		}
		return ids;
	}

	public void addAll(String[] fields) {
		try {
			for (int i = 0; i < fields.length; i++) {
				mFields.accumulate("fields", fields[i]);
			}
			if (fields.length == 1) {
				mFields.accumulate("fields", fields[0]);
			}
		} catch (Exception e) {
		}
	}

	public void addAll(List<OEColumn> cols) {
		try {
			for (OEColumn col : cols) {
				if (col.canSync())
					mFields.accumulate("fields", col.getName());
			}
			if (cols.size() == 1) {
				mFields.accumulate("fields", cols.get(0));
			}
		} catch (Exception e) {
		}
	}

	public void addManyToOneId(Object model, int id) {

	}

	public JSONObject get() {
		return mFields;
	}

	public List<OEValues> getValues() {
		return mValues;
	}

	public List<OERelationData> getRelationData() {
		return mRelRecord.getAll();
	}

	class OERelRecord {
		private HashMap<String, Object> _models = new HashMap<String, Object>();
		private HashMap<String, List<Object>> _model_ids = new HashMap<String, List<Object>>();

		@SuppressWarnings("unchecked")
		public void add(OEDatabase db, Object ids) {
			if (!_models.containsKey(db.getModelName())) {
				_models.put(db.getModelName(), db);
			}
			List<Object> _ids = new ArrayList<Object>();
			if (ids instanceof List) {
				_ids = (List<Object>) ids;
			}
			if (ids instanceof Integer) {
				_ids.add(ids);
			}
			if (_model_ids.containsKey(db.getModelName())) {
				if (!_model_ids.containsValue(_ids))
					_model_ids.get(db.getModelName()).addAll(_ids);
			} else {
				_model_ids.put(db.getModelName(), _ids);
			}
		}

		public List<OERelationData> getAll() {
			List<OERelationData> datas = new ArrayList<OEFieldsHelper.OERelationData>();
			Set<String> keys = _models.keySet();
			for (String key : keys) {
				OEDatabase db = (OEDatabase) _models.get(key);
				datas.add(new OERelationData(db, _model_ids.get(key)));
			}
			return datas;
		}
	}

	public class OERelationData {
		OEDatabase db;
		List<Object> ids;

		public OERelationData(OEDatabase db, List<Object> ids) {
			super();
			this.db = db;
			this.ids = ids;
		}

		public OEDatabase getDb() {
			return db;
		}

		public void setDb(OEDatabase db) {
			this.db = db;
		}

		public List<Object> getIds() {
			return ids;
		}

		public void setIds(List<Object> ids) {
			this.ids = ids;
		}

	}

}
