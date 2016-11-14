/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p>
 * Created on 22/4/15 3:57 PM
 */
package com.odoo.core.rpc.helper.utils.gson;

public class OdooResponse {
    public String jsonrpc;
    public Integer id;
    public OdooResult result;
    public OdooResult error;

    @Override
    public String toString() {
        return "OdooResponse{" +
                "jsonrpc='" + jsonrpc + '\'' +
                ", error='" + error + '\'' +
                ", id=" + id +
                ", result=" + result +
                '}';
    }
}
