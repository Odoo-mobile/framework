/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General License for more details
 * <p>
 * You should have received a copy of the GNU Affero General License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p>
 * Created on 7/1/15 5:15 PM
 */
package odoo.controls;

import android.view.View;

import com.odoo.core.orm.fields.OColumn;

interface IOControlData {
    String TAG = IOControlData.class.getSimpleName();

    void setValue(Object value);

    Object getValue();

    void setEditable(Boolean editable);

    Boolean isEditable();

    void setLabelText(String label);

    void setColumn(OColumn column);

    void initControl();

    String getLabel();

    void setValueUpdateListener(ValueUpdateListener listener);

    interface ValueUpdateListener {
        void onValueUpdate(Object value);

        void visibleControl(boolean isVisible);
    }

    Boolean isControlReady();

    void resetData();

    View getFieldView();

    void setError(String error);
}
