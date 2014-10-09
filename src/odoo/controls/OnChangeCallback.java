package odoo.controls;

import com.odoo.orm.ODataRow;

public interface OnChangeCallback {
	public void onValueChange(ODataRow row);
}
