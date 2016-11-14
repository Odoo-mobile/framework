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
 * Created on 21/4/15 5:51 PM
 */
package com.odoo.core.rpc.listeners;

import java.util.ArrayList;
import java.util.List;

import com.odoo.core.rpc.helper.utils.gson.OdooResult;

public class OdooError {
    private String message, serverTrace, exceptionType;
    private List<String> errors = new ArrayList<>();
    private Throwable throwable;
    private int responseCode = -1;


    public static OdooError parse(OdooResult error) {
        OdooError odooError = new OdooError(error.getString("message"), null);
        odooError.setServerTrace(error.getMap("data").getString("debug"));
        if (error.getMap("data").containsKey("exception_type"))
            odooError.setExceptionType(error.getMap("data").getString("exception_type"));
        odooError.setMessage(error.getMap("data").getString("message"));
        odooError.setErrors(error.getMap("data").<String>getArray("arguments"));
        return odooError;
    }

    public OdooError(String message, Throwable throwable) {
        this.message = message;
        this.throwable = throwable;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }

    public String getServerTrace() {
        return serverTrace;
    }

    public void setServerTrace(String serverTrace) {
        this.serverTrace = serverTrace;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public String toString() {
        return "OdooError{" +
                "message='" + message + '\'' +
                ", serverTrace='" + serverTrace + '\'' +
                ", exceptionType='" + exceptionType + '\'' +
                ", errors=" + errors +
                ", throwable=" + throwable +
                ", responseCode=" + responseCode +
                '}';
    }
}
