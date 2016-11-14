package com.odoo.core.rpc.helper;

import java.util.Arrays;

public class OdooUserCurrency {
    public Integer id;
    public Integer[] digits = new Integer[2];
    public String position;
    public String symbol;

    @Override
    public String toString() {
        return "OdooUserCurrency{" +
                "id=" + id +
                ", digits=" + Arrays.toString(digits) +
                ", position='" + position + '\'' +
                ", symbol='" + symbol + '\'' +
                '}';
    }
}
