package com.openerp.orm;

import com.openerp.orm.OEFieldsHelper.ValueWatcher;

/**
 * The Class OEColumn. Handle each database column with name, title, type
 * (OEFields) and help.
 */
public class OEColumn {

	/** The name. */
	private String mName;

	/** The title. */
	private String mTitle;

	/** The type. */
	private Object mType;

	/** The help. */
	private String mHelp = "";

	/** The can sync. */
	private boolean mCanSync = true;

	/** The column domain */
	OEColumnDomain mColumnDomain = null;

	ValueWatcher mValueWatcher = null;

	/**
	 * Instantiates a new database column.
	 * 
	 * @param mName
	 *            the m name
	 * @param mTitle
	 *            the m title
	 * @param mType
	 *            the m type
	 */
	public OEColumn(String mName, String mTitle, Object mType) {
		this.mName = mName;
		this.mTitle = mTitle;
		this.mType = mType;
	}

	public OEColumn(String mName, String mTitle, Object mType,
			ValueWatcher valueWatcher) {
		this.mName = mName;
		this.mTitle = mTitle;
		this.mType = mType;
		this.mValueWatcher = valueWatcher;
	}

	/**
	 * Instantiates a new database column.
	 * 
	 * @param mName
	 *            the m name
	 * @param mTitle
	 *            the m title
	 * @param mType
	 *            the m type
	 * @param mCanSync
	 *            the m can sync
	 */
	public OEColumn(String mName, String mTitle, Object mType, boolean mCanSync) {
		this.mName = mName;
		this.mTitle = mTitle;
		this.mType = mType;
		this.mCanSync = mCanSync;
	}

	public OEColumn(String name, String title, Object type,
			OEColumnDomain columnDomain) {
		this.mName = name;
		this.mTitle = title;
		this.mType = type;
		this.mColumnDomain = columnDomain;
	}

	/**
	 * Instantiates a new database column.
	 * 
	 * @param mName
	 *            the m name
	 * @param mTitle
	 *            the m title
	 * @param mType
	 *            the m type
	 * @param mHelp
	 *            the m help
	 * @param mCanSync
	 *            the m can sync
	 */
	public OEColumn(String mName, String mTitle, Object mType, String mHelp,
			boolean mCanSync) {
		this.mName = mName;
		this.mTitle = mTitle;
		this.mType = mType;
		this.mHelp = mHelp;
		this.mCanSync = mCanSync;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return mName;
	}

	/**
	 * Sets the name.
	 * 
	 * @param mName
	 *            the new name
	 */
	public void setName(String mName) {
		this.mName = mName;
	}

	/**
	 * Gets the title.
	 * 
	 * @return the title
	 */
	public String getTitle() {
		return mTitle;
	}

	/**
	 * Sets the title.
	 * 
	 * @param mTitle
	 *            the new title
	 */
	public void setTitle(String mTitle) {
		this.mTitle = mTitle;
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public Object getType() {
		return mType;
	}

	/**
	 * Sets the type.
	 * 
	 * @param mType
	 *            the new type
	 */
	public void setType(Object mType) {
		this.mType = mType;
	}

	/**
	 * Gets the help.
	 * 
	 * @return the help
	 */
	public String getHelp() {
		return mHelp;
	}

	/**
	 * Sets the help.
	 * 
	 * @param mHelp
	 *            the new help
	 */
	public void setHelp(String mHelp) {
		this.mHelp = mHelp;
	}

	/**
	 * Can sync.
	 * 
	 * @return true, if successful
	 */
	public boolean canSync() {
		return mCanSync;
	}

	/**
	 * Sets the can sync.
	 * 
	 * @param mCanSync
	 *            the new can sync
	 */
	public void setCanSync(boolean mCanSync) {
		this.mCanSync = mCanSync;
	}

	public OEColumnDomain getColumnDomain() {
		return mColumnDomain;
	}

	public void setColumnDomain(OEColumnDomain columnDomain) {
		mColumnDomain = columnDomain;
	}

	public ValueWatcher getmValueWatcher() {
		return mValueWatcher;
	}

	public void setmValueWatcher(ValueWatcher mValueWatcher) {
		this.mValueWatcher = mValueWatcher;
	}

}
