package odoo.controls;

import com.odoo.orm.OColumn.ColumnDomain;

public interface OnDomainFilterCallbacks {
	public void onFieldValueChanged(ColumnDomain domain);
}
