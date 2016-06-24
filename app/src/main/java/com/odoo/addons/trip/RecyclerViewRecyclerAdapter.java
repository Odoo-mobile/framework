package com.odoo.addons.trip;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.aakira.expandablelayout.ExpandableLayout;
import com.github.aakira.expandablelayout.ExpandableLayoutListenerAdapter;
import com.github.aakira.expandablelayout.ExpandableLinearLayout;
import com.github.aakira.expandablelayout.Utils;
import com.odoo.R;
import com.odoo.addons.Equipment.EquipmentDetails;
import com.odoo.addons.Equipment.providers.CmmsEquipment;
import com.odoo.addons.tripdestination.providers.CmmsTripDestination;
import com.odoo.base.addons.res.ResCompany;
import com.odoo.base.addons.res.ResPartner;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.utils.IntentUtils;

import java.util.List;


/**
 * Created by Sylwek on 23/06/2016.
 */

public class RecyclerViewRecyclerAdapter extends RecyclerView.Adapter<RecyclerViewRecyclerAdapter.ViewHolder> {

    private final List<ItemModel> data;
    private Context context;
    private SparseBooleanArray expandState = new SparseBooleanArray();


    public RecyclerViewRecyclerAdapter(final List<ItemModel> data) {
        this.data = data;
        for (int i = 0; i < data.size(); i++) {
            expandState.append(i, false);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        this.context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.trip_recycler_view_list_row, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ItemModel item = data.get(position);
        holder.item_txtName.setText(item.getCustomer());
        holder.item_type.setText(item.getType());
        holder.l.setImageBitmap(item.getL());
        holder.i.setImageBitmap(item.getI());
        holder.t.setImageBitmap(item.getT());
        holder.p.setImageBitmap(item.getP());
        holder.r.setImageBitmap(item.getR());
        holder.a.setImageBitmap(item.getA());
        holder.podConNumber.setText("Controller- "+item.getController_number());
        //TODO - add revision number
       holder.itemView.setBackgroundColor(ContextCompat.getColor(context, item.getColorId1()));
       holder.expandableLayout.setBackgroundColor(ContextCompat.getColor(context, item.getColorId2()));
        holder.expandableLayout.setInterpolator(Utils.createInterpolator(Utils.DECELERATE_INTERPOLATOR));
        holder.expandableLayout.setExpanded(expandState.get(position));
        holder.expandableLayout.setListener(new ExpandableLayoutListenerAdapter() {
            @Override
            public void onPreOpen() {
                createRotateAnimator(holder.buttonLayout, 0f, 180f).start();
                expandState.put(position, true);
            }

            @Override
            public void onPreClose() {
                createRotateAnimator(holder.buttonLayout, 180f, 0f).start();
                expandState.put(position, false);
            }
        });

        holder.buttonLayout.setRotation(expandState.get(position) ? 180f : 0f);
        holder.buttonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onClickButton(holder.expandableLayout);
            }
        });
        holder.podDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                podMoreDetails(position,item.getEquipment_id());
            }
        });
        holder.startSatNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.i("onClick","start Sat Nav");
                startSatNav(item.getCustomerID());
            }
        });

        holder.satNavDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.i("onClick","Sat Nav details");
                navigationDetails();
            }
        });

    }
    private void navigationDetails()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Sat Nav Det");
        builder.setMessage("Please..e.e.e");

// Set up the input
        final EditText input = new EditText(context);
        final TextView coords = new TextView(context);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT );
        builder.setView(input);
        builder.setView(coords);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("OnClick","InputDialog OK");
                String city = input.getText().toString();

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("OnClick","InputDialog Cancel");
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void startSatNav(int customerID)
    {
        ResPartner resPartner = new ResPartner(context,null);
        ODataRow record = resPartner.browse(customerID);
        String coords =  resPartner.getCoords(record);
        if (coords.equals("0.0, 0.0"))
            IntentUtils.redirectToMap(context, record.getString("full_address"));
        else
            IntentUtils.redirectToMap(context, coords);
    }
    private void loadEquipmentActivity(ODataRow row) {
        Bundle data = null;
        if (row != null) {
            data = row.getPrimaryBundleData();
        }
        IntentUtils.startActivity(context, EquipmentDetails.class, data);
    }
    private void onClickButton(final ExpandableLayout expandableLayout) {
        expandableLayout.toggle();
    }
    private void podMoreDetails(int position,int equipmentID)
    {
        CmmsEquipment cmmsEquipment = new CmmsEquipment(context,null);
        ODataRow oDataRowEquipment = cmmsEquipment.browse(equipmentID);
        loadEquipmentActivity(oDataRowEquipment);

    }
    @Override
    public int getItemCount() {
        return data.size();
    }

    public ObjectAnimator createRotateAnimator(final View target, final float from, final float to) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "rotation", from, to);
        animator.setDuration(300);
        animator.setInterpolator(Utils.createInterpolator(Utils.LINEAR_INTERPOLATOR));
        return animator;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView item_txtName;
        public TextView item_type;
        public ImageView t;
        public ImageView i ;
        public ImageView a ;
        public  ImageView l ;
        public  ImageView p ;
        public ImageView r ;
        public RelativeLayout buttonLayout;
        //pod details
        public Button podDetails;
        public TextView podRevision;
        public TextView podConNumber;
        //satnav
        public Button startSatNav;
        public Button satNavDetails;

        /**
         * You must use the ExpandableLinearLayout in the recycler view.
         * The ExpandableRelativeLayout doesn't work.
         */
        public ExpandableLinearLayout expandableLayout;

        public ViewHolder(View v) {
            super(v);
            item_txtName = (TextView) v.findViewById(R.id.item_txtName);
            item_type = (TextView) v.findViewById(R.id.item_type);
            t = (ImageView) v.findViewById(R.id.image_small_training);
            i = (ImageView) v.findViewById(R.id.image_small_install);
            a = (ImageView) v.findViewById(R.id.image_small_action);
            l = (ImageView) v.findViewById(R.id.image_small_loler);
            p = (ImageView) v.findViewById(R.id.image_small_pick_up);
            r = (ImageView) v.findViewById(R.id.image_small_replacement);
            buttonLayout = (RelativeLayout) v.findViewById(R.id.button);
            podDetails = (Button)v.findViewById(R.id.poddetails);
            podConNumber = (TextView) v.findViewById(R.id.podConNumber);
            podRevision = (TextView) v.findViewById(R.id.podRevision);
            //
            startSatNav = (Button)v.findViewById(R.id.navigate);
            satNavDetails = (Button)v.findViewById(R.id.satnavdetails);

            expandableLayout = (ExpandableLinearLayout) v.findViewById(R.id.expandableLayout);
        }
    }
}