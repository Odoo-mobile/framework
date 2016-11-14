/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p/>
 * Created on 21/4/15 4:09 PM
 */
package com.odoo.core.rpc.helper;

import android.os.Bundle;

import com.odoo.core.rpc.handler.OdooVersionException;
import com.odoo.core.rpc.helper.utils.OBundleUtils;
import com.odoo.core.rpc.helper.utils.gson.OdooResult;

import java.util.List;

/**
 * Stores odoo version information
 * <p/>
 * 8.saas~6     : [8,'saas~6',0,'final',0],
 * 9.0alpha1    : [9,0,0,'alpha',1],
 * 8.0          : [8,0,0,'final',0]
 */
public class OdooVersion {
    public static final String TAG = OdooVersion.class.getSimpleName();
    private String serverSerie, serverVersion, versionType, versionRelease;
    private int versionNumber, versionTypeNumber;
    private Boolean isEnterprise = false;

    public Bundle getAsBundle() {
        Bundle version = new Bundle();
        version.putString("server_serie", getServerSerie());
        version.putString("server_version", getServerVersion());
        version.putString("version_type", getVersionType());
        version.putString("version_release", getVersionRelease());
        version.putInt("version_number", getVersionNumber());
        version.putInt("version_type_number", getVersionTypeNumber());
        return version;
    }

    public void fillFromBundle(Bundle data) {
        if (OBundleUtils.hasKey(data, "server_serie"))
            setServerSerie(data.getString("server_serie"));
        if (OBundleUtils.hasKey(data, "server_version"))
            setServerVersion(data.getString("server_version"));
        if (OBundleUtils.hasKey(data, "version_type"))
            setVersionType(data.getString("version_type"));
        if (OBundleUtils.hasKey(data, "version_release"))
            setVersionRelease(data.getString("version_release"));
        if (OBundleUtils.hasKey(data, "version_number"))
            setVersionNumber(data.getInt("version_number"));
        if (OBundleUtils.hasKey(data, "version_type_number"))
            setVersionTypeNumber(data.getInt("version_type_number"));
    }

    public static OdooVersion parseVersion(OdooResult result) throws OdooVersionException {
        OdooVersion version = new OdooVersion();
        version.setServerSerie(result.getString("server_serie"));
        version.setServerVersion(result.getString("server_version"));
        List<Object> version_info = result.getArray("server_version_info");
        version.setVersionNumber(((Double) version_info.get(0)).intValue());
        if (version.getVersionNumber() < 7) {
            throw new OdooVersionException("Server version is different from " +
                    "the application supported version. (Only Odoo 7.0+ supported)");
        }
        String versionType = version_info.get(1) + "";
        int versionTypeNumber = 0;
        if (versionType.contains("saas")) {
            versionTypeNumber = Integer.parseInt(versionType.split("~")[1]);
            versionType = "saas";
        }
        version.setVersionTypeNumber(versionTypeNumber);
        version.setVersionType(versionType);
        version.setVersionRelease((String) version_info.get(3));

        if (version.getVersionNumber() > 9) {
            String ent = version_info.get(5) + "";
            version.setEnterprise(ent.equals("e"));
        }

        return version;
    }


    public String getVersionRelease() {
        return versionRelease;
    }

    public void setVersionRelease(String versionRelease) {
        this.versionRelease = versionRelease;
    }

    public String getServerSerie() {
        return serverSerie;
    }

    public void setServerSerie(String serverSerie) {
        this.serverSerie = serverSerie;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public String getVersionType() {
        return versionType;
    }

    public void setVersionType(String versionType) {
        this.versionType = versionType;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public int getVersionTypeNumber() {
        return versionTypeNumber;
    }

    public void setVersionTypeNumber(int versionTypeNumber) {
        this.versionTypeNumber = versionTypeNumber;
    }


    public Boolean isEnterprise() {
        return isEnterprise;
    }

    public void setEnterprise(Boolean enterprise) {
        isEnterprise = enterprise;
    }

    @Override
    public String toString() {
        return "OdooVersion{" +
                "serverSerie='" + serverSerie + '\'' +
                ", serverVersion='" + serverVersion + '\'' +
                ", versionType='" + versionType + '\'' +
                ", versionRelease='" + versionRelease + '\'' +
                ", versionNumber=" + versionNumber +
                ", versionTypeNumber=" + versionTypeNumber +
                ", isEnterprise=" + isEnterprise +
                '}';
    }
}
