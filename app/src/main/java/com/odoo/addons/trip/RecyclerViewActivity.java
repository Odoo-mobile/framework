package com.odoo.addons.trip;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;

import com.github.aakira.expandablelayout.Utils;
import com.odoo.R;
import com.odoo.addons.Equipment.providers.CmmsEquipment;
import com.odoo.addons.productionline.providers.CmmsProductionLine;
import com.odoo.addons.trip.providers.CmmsTrips;
import com.odoo.addons.tripdestination.providers.CmmsTripDestination;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.utils.BitmapUtils;
import com.odoo.core.utils.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sylwek on 23/06/2016.
 */

public class RecyclerViewActivity extends AppCompatActivity {
    Menu mMenu;

    private ActionBar actionBar;
    private ODataRow oDataRow1;
    private List<ODataRow> tripDestinations;
    private CmmsTripDestination cmmsTripDestination;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, RecyclerViewActivity.class));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.odoo.R.menu.menu_trip_detail, menu);
        mMenu = menu;
        return true;
    }
    private void getTripDestinationList() {
        cmmsTripDestination = new CmmsTripDestination(getApplicationContext(), null);
        List<ODataRow> test;
        OModel oModelDestination = new OModel(getApplicationContext(), cmmsTripDestination.getModelName(), null);
        //String stest = record.getString("_id");
        try {
            tripDestinations = oModelDestination.query("select * from " + cmmsTripDestination.getTableName() + " where trip = " + "1" + " ORDER BY order1 ASC");
            // tripDestinations = oModelDestination.
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }

    }
    public ItemModel getTripDestinationDetails(ODataRow oDataRow)
    {
        ItemModel itemModel = new ItemModel();
        CmmsEquipment cmmsEquipment = new CmmsEquipment(getApplicationContext(),null);
//        ODataRow oDataRowEquipment = cmmsEquipment.select(
//                new String[]{"name","type","pp_controller_number"},
//                "_id = ?",
//                new String[]{oDataRow.getString("equipment_id")}
//        ).get(0); // always first element (should be only one)
        ODataRow oDataRowEquipment = cmmsEquipment.browse(oDataRow.getInt("equipment_id"));
        itemModel.setType(oDataRowEquipment.getString("type"));
        itemModel.setCustomer(oDataRowEquipment.getString("name"));
        itemModel.setCustomerID(oDataRowEquipment.getInt("customer"));
        itemModel.setEquipment_id(oDataRow.getInt("equipment_id"));
        itemModel = setState(oDataRow.getString("state"),itemModel);
        if (oDataRow.getString("action").equals("true")) {
            itemModel.setA(BitmapUtils.getAlphabetImage(getApplicationContext(), "A"));
            itemModel.setAction(true);    }
        if (oDataRow.getString("installation").equals("true")) {
            itemModel.setI(BitmapUtils.getAlphabetImage(getApplicationContext(), "I"));
            itemModel.setInstallation(true);   }
        if (oDataRow.getString("training").equals("true")) {
            itemModel.setT(BitmapUtils.getAlphabetImage(getApplicationContext(), "T"));
            itemModel.setTraining(true);           }
        if (oDataRow.getString("loler").equals("true")) {
            itemModel.setL(BitmapUtils.getAlphabetImage(getApplicationContext(), "L"));
            itemModel.setLoler(true);        }
        if (oDataRow.getString("pick_up").equals("true")) {
            itemModel.setP(BitmapUtils.getAlphabetImage(getApplicationContext(), "P"));
            itemModel.setPick_up(true);        }
        if (oDataRow.getString("replacement").equals("true")) {
            itemModel.setR(BitmapUtils.getAlphabetImage(getApplicationContext(), "R"));
            itemModel.setReplacement(true);        }
        itemModel.setCustomerid(oDataRow.getString("customer"));
        if(oDataRow.getString("description").equals("false"))
        itemModel.setDescription("Info: "+ "N/A");
        else
        itemModel.setDescription("Info: " + oDataRow.getString("description") );

        if(oDataRowEquipment.getString("pp_controller_number").equals("false"))
            itemModel.setController_number("N/A");
        else
            itemModel.setController_number(oDataRowEquipment.getString("pp_controller_number"));

       // CmmsProductionLine cmmsProductionLine = new CmmsProductionLine(getApplicationContext(),null);
        //TODO - Add production line to itemmodel
      //  ODataRow oDataRowLine = cmmsProductionLine.browse(oDataRowEquipment.getInt("line_id"));
      //  itemModel.setEquipment_rev(oDataRowLine.getString("name"));

        return itemModel;
    }
    public ItemModel setState(String state,ItemModel itemModel)
    {
        switch (state) {
            case "1" : //Not started
                itemModel.setColorId1(R.color.material_amber_A400);
                itemModel.setColorId2(R.color.material_amber_200);
                break;
            case "2": // driving
                itemModel.setColorId1(R.color.material_indigo_700);
                itemModel.setColorId2(R.color.material_indigo_200);
                break;
            case "3": // working
                itemModel.setColorId1(R.color.material_teal_700);
                itemModel.setColorId2(R.color.material_teal_200);
                break;
            case "4": // complete
                itemModel.setColorId1(R.color.material_light_green_700);
                itemModel.setColorId2(R.color.material_light_green_400);
                break;
            case "5": //incomplete
                itemModel.setColorId1(R.color.material_red_500);
                itemModel.setColorId2(R.color.material_red_300);
                break;
        }
        return itemModel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);
        getSupportActionBar().setTitle("Trip Details");
        getTripDestinationList();
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        CmmsTrips cmmsTrips = new CmmsTrips(getApplicationContext(),null);

        final List<ItemModel> data = new ArrayList<>();

        for(int i = 0;i < tripDestinations.size();i++)
        {
            data.add(getTripDestinationDetails(tripDestinations.get(i)));
        }

      //  data.add(cmmsTrips.browse(1));
//                "0 ACCELERATE_DECELERATE_INTERPOLATOR",
//                R.color.material_red_500,
//                R.color.material_red_300,
//                Utils.createInterpolator(Utils.ACCELERATE_DECELERATE_INTERPOLATOR)));
//        data.add(new ItemModel(
//                "1 ACCELERATE_INTERPOLATOR",
//                R.color.material_pink_500,
//                R.color.material_pink_300,
//                Utils.createInterpolator(Utils.ACCELERATE_INTERPOLATOR)));
//        data.add(new ItemModel(
//                "2 BOUNCE_INTERPOLATOR",
//                R.color.material_purple_500,
//                R.color.material_purple_300,
//                Utils.createInterpolator(Utils.BOUNCE_INTERPOLATOR)));
//        data.add(new ItemModel(
//                "3 DECELERATE_INTERPOLATOR",
//                R.color.material_deep_purple_500,
//                R.color.material_deep_purple_300,
//                Utils.createInterpolator(Utils.DECELERATE_INTERPOLATOR)));
//        data.add(new ItemModel(
//                "4 FAST_OUT_LINEAR_IN_INTERPOLATOR",
//                R.color.material_indigo_500,
//                R.color.material_indigo_300,
//                Utils.createInterpolator(Utils.FAST_OUT_LINEAR_IN_INTERPOLATOR)));
//        data.add(new ItemModel(
//                "5 FAST_OUT_SLOW_IN_INTERPOLATOR",
//                R.color.material_blue_500,
//                R.color.material_blue_300,
//                Utils.createInterpolator(Utils.FAST_OUT_SLOW_IN_INTERPOLATOR)));
//        data.add(new ItemModel(
//                "6 LINEAR_INTERPOLATOR",
//                R.color.material_light_blue_500,
//                R.color.material_light_blue_300,
//                Utils.createInterpolator(Utils.LINEAR_INTERPOLATOR)));
//        data.add(new ItemModel(
//                "7 LINEAR_OUT_SLOW_IN_INTERPOLATOR",
//                R.color.material_cyan_500,
//                R.color.material_cyan_300,
//                Utils.createInterpolator(Utils.LINEAR_OUT_SLOW_IN_INTERPOLATOR)));
        recyclerView.setAdapter(new RecyclerViewRecyclerAdapter(data));
    }
}