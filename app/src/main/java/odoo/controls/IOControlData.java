/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 *
 * Created on 7/1/15 5:15 PM
 */
package odoo.controls;

import android.view.View;

import com.odoo.core.orm.fields.OColumn;

public interface IOControlData {
    public static final String TAG = IOControlData.class.getSimpleName();

    public void setValue(Object value);

    public Object getValue();

    public void setEditable(Boolean editable);

    public Boolean isEditable();

    public void setLabelText(String label);

    public void setColumn(OColumn column);

    public void initControl();

    public String getLabel();

    public void setValueUpdateListener(ValueUpdateListener listener);

    public static interface ValueUpdateListener {
        public void onValueUpdate(Object value);

        public void visibleControl(boolean isVisible);
    }

    public Boolean isControlReady();

    public void resetData();

    public View getFieldView();

    public void setError(String error);
}
