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
 * Created on 4/6/15 11:42 AM
 */
package com.odoo.core.account;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;

import com.odoo.R;
import com.odoo.core.auth.OdooAccountManager;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.core.utils.OControls;

import com.odoo.core.rpc.Odoo;
import com.odoo.core.rpc.handler.OdooVersionException;

public class OdooUserObjectUpdater extends AlertDialog {
    public static final String TAG = OdooUserObjectUpdater.class.getSimpleName();
    private OnUpdateFinish mOnUpdateFinish;
    private Context mContext;
    private UpdateData updateData;

    protected OdooUserObjectUpdater(Context context,
                                    OnUpdateFinish onUpdateFinish) {
        super(context);
        mContext = context;
        mOnUpdateFinish = onUpdateFinish;
    }

    public interface OnUpdateFinish {
        void userObjectUpdateFinished();

        void userObjectUpdateFail();
    }

    public void showDialog() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.base_user_object_updater,
                null, false);
        bindView(view);
        setView(view);
        setCancelable(false);
        show();
        updateData = new UpdateData(this);
        updateData.execute(OUser.current(mContext));
    }

    private void bindView(View view) {
        OUser user = OUser.current(mContext);
        String avatar = user.getAvatar();
        if (!avatar.equals("false")) {
            Bitmap img = BitmapUtils.getBitmapImage(mContext, avatar);
            OControls.setImage(view, R.id.userAvatar, img);
        }
        OControls.setText(view, R.id.userName, "Hello " + user.getName());
    }


    private class UpdateData extends AsyncTask<OUser, Void, Boolean> {

        private OdooUserObjectUpdater mObj;

        public UpdateData(OdooUserObjectUpdater obj) {
            mObj = obj;
        }

        @Override
        protected Boolean doInBackground(OUser... params) {
            try {
                OUser user = params[0];
                Odoo odoo = Odoo.createInstance(mContext, user.getHost());
                OUser mUser = odoo.authenticate(user.getUsername(), user.getPassword(),
                        user.getDatabase());
                if (mUser != null) {
                    OUser updatedUser = new OUser();
                    updatedUser.setFromBundle(mUser.getAsBundle());
                    OdooAccountManager.updateUserData(mContext, user, updatedUser);

                    Thread.sleep(1500);
                    return true;
                }
            } catch (OdooVersionException | InterruptedException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mObj.dismiss();
            if (!result) {
                mOnUpdateFinish.userObjectUpdateFail();
            } else {
                mOnUpdateFinish.userObjectUpdateFinished();
            }
        }
    }

    public static void showUpdater(Context context, OnUpdateFinish callback) {
        OdooUserObjectUpdater updater = new OdooUserObjectUpdater(context, callback);
        updater.showDialog();
    }

}
