package com.openerp.orm;

import java.util.List;

public interface OEDBHelper {
	public String getModelName();

	public List<OEColumn> getModelColumns();
}
