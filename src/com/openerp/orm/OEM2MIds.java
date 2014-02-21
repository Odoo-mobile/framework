package com.openerp.orm;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

/**
 * The Class OEM2MIds. handling many2many ids operations.
 */
public class OEM2MIds {

	/**
	 * The Enum Many2Many ids Operation.
	 */
	public enum Operation {

		/** Adds given ids to related many2many table. */
		ADD,
		/** Appends given ids to related many2many table. */
		APPEND,
		/** Removes given ids from related many2many table. */
		REMOVE,
		/**
		 * Replace old ids with new one. (i.e, remove old one and insert new
		 * one).
		 */
		REPLACE
	}

	/** The operation. */
	Operation mOperation = null;

	/** The List<Integer> ids. */
	private List<Integer> mIds = new ArrayList<Integer>();

	/**
	 * Instantiates a new Many2Many operation and ids.
	 * 
	 * @param operation
	 *            the operation
	 * @param ids
	 *            the ids list
	 */
	public OEM2MIds(Operation operation, List<Integer> ids) {
		mIds.clear();
		mOperation = operation;
		mIds.addAll(ids);
	}

	/**
	 * Gets the operation.
	 * 
	 * @return the operation
	 */
	public Operation getOperation() {
		return mOperation;
	}

	/**
	 * Sets the operation.
	 * 
	 * @param mOperation
	 *            the new operation
	 */
	public void setOperation(Operation mOperation) {
		this.mOperation = mOperation;
	}

	/**
	 * Gets the ids.
	 * 
	 * @return the ids
	 */
	public List<Integer> getIds() {
		return mIds;
	}

	public JSONArray getJSONIds() {
		JSONArray ids = new JSONArray();
		try {
			for (int id : mIds) {
				ids.put(id);
			}
		} catch (Exception e) {
		}
		return ids;
	}

	/**
	 * Sets the ids.
	 * 
	 * @param mIds
	 *            the new ids
	 */
	public void setIds(List<Integer> ids) {
		mIds.clear();
		mIds.addAll(ids);
	}

}
