package com.odoo.addons.Equipment.providers;

import android.content.Context;
import android.net.Uri;

import com.odoo.base.addons.res.ResPartner;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OBoolean;
import com.odoo.core.orm.fields.types.OSelection;
import com.odoo.core.orm.fields.types.OVarchar;
import com.odoo.core.support.OUser;

/**
 * Created by Sylwek on 02/12/2015.
 */
public class CmmsEquipment extends OModel {
    public static final String AUTHORITY = "com.corepd.addons.Equipment.Equipment";
    public static final String TAG = CmmsEquipment.class.getSimpleName();
    OColumn name = new OColumn("name", OVarchar.class);
    OColumn type = new OColumn("Serial Number", OVarchar.class);
    OColumn active = new OColumn("Active?", OBoolean.class);
    ///////////////////////////////////
//    OColumn address = new OColumn("address", OVarchar.class);
//    OColumn county = new OColumn("county", OVarchar.class);
//    OColumn postcode = new OColumn("postcode", OVarchar.class);
//    OColumn location = new OColumn("location", OVarchar.class);
//    OColumn operator = new OColumn("operator", OVarchar.class);
//    OColumn main_phone_number = new OColumn("main_phone_number", OVarchar.class);
//    OColumn addit_phone_number = new OColumn("addit_phone_number", OVarchar.class);
    //////////////////////////////////////////////////////////
    OColumn pp_controller_number = new OColumn("Poolpod Controller Number", OVarchar.class);
    OColumn batch_number = new OColumn("Batch Number", OVarchar.class);
    OColumn pp_serial_number = new OColumn("Poolpod Serial Number", OVarchar.class);
   // OColumn uk_supp_agr = new OColumn("uk_supp_agr", OBoolean.class);
    OColumn remote_serial_number = new OColumn("Remote Serial Number", OVarchar.class);
    OColumn lanyard_bat_upg = new OColumn("Lanyard battery Upgraded?", OBoolean.class);
    OColumn lift_bat_serial1 = new OColumn("Lift Battery Serial 1", OVarchar.class);
    OColumn lift_bat_serial2 = new OColumn("Lift Battery Serial 2", OVarchar.class);
    OColumn pside_cont_software = new OColumn("Poolside Controller Software", OVarchar.class);
    OColumn rfid_soft = new OColumn("RFID Software", OVarchar.class);
    OColumn remote_pull_upgraded = new OColumn("Remote Pull Upgraded",OBoolean.class);
    OColumn pp_remote_software = new OColumn("Remote Software", OVarchar.class);
    OColumn pp_remote_charger_type = new OColumn("Remote Charger Type", OVarchar.class);
    OColumn anchor_short = new OColumn("Anchor Short", OBoolean.class);
    OColumn anchor_medium = new OColumn("Anchor Medium", OBoolean.class);
    OColumn other_anchor = new OColumn("Other Anchor", OBoolean.class);
    OColumn low = new OColumn("Feet Low", OBoolean.class);
    OColumn high = new OColumn("Feet High", OBoolean.class);
    OColumn other_feet = new OColumn("Other Feet", OBoolean.class);
    OColumn pod_bat_type = new OColumn("Battery Type", OSelection.class).addSelection("1","Pouch").addSelection("2", "3P Stab in").addSelection("3","2P Plug").setLabel("Poolpod Battery Type");
    OColumn pump_mod_type = new OColumn("Pump Module Type", OSelection.class).addSelection("1","SMC Valves (4)").addSelection("2", "SMC Valves (3)").addSelection("3","Core PD Valves");
    OColumn pside_con_bat = new OColumn("Poolside Battery", OSelection.class).addSelection("1","1P Plug").addSelection("2", "1P Stab in").addSelection("3","Pouch");
    OColumn on_switch = new OColumn("On Switch", OSelection.class).addSelection("1", "Piezo Switch").addSelection("2", "Reed Switch");
    OColumn manual_recovery = new OColumn("Manual Recovery", OSelection.class).addSelection("1", "Sick").addSelection("2", "Asmtec");
    OColumn limit_plate_type = new OColumn("Limit Plate Type", OSelection.class).addSelection("1", "Inductive").addSelection("2", "Mechanical");
    OColumn door_lock_sensor = new OColumn("Door Lock Sensor ", OSelection.class).addSelection("1","Omrom (Braket)").addSelection("2", "Omrom (Plate)").addSelection("3","Orange cap").addSelection("4","Reed").addSelection("4","Contrinex");
    OColumn motor_con = new OColumn("Motor Controller", OSelection.class).addSelection("1","V3").addSelection("2", "V2 Plug").addSelection("3","V2.0 Stab in").addSelection("4","V1.0 Stab in");
    OColumn pp_remote_control = new OColumn("Poolpod Remote", OSelection.class).addSelection("1", "Version 7(With graphic)").addSelection("2", "Version 6(Without graphic)");
    OColumn ramp = new OColumn("Ramp", OSelection.class).addSelection("1","Gray PVC Rubber (no front edge)").addSelection("2", "Gray PVC Rubber (with front edge)").addSelection("3","Black Rubber (Old)");
    OColumn platform = new OColumn("Platform", OSelection.class).addSelection("1", "Black").addSelection("2", "Gray");
    OColumn trolleys = new OColumn("Trolleys", OSelection.class).addSelection("1","Unknown").addSelection("2", "Trolleys Version 2").addSelection("3","Trolleys Version 1").addSelection("4","None");
    OColumn wheelchair = new OColumn("Wheelchair", OSelection.class).addSelection("1", "Black").addSelection("2", "Gray");
    OColumn customer = new OColumn("Customer", ResPartner.class, OColumn.RelationType.ManyToOne);

    //OColumn pod_bat_type = new OColumn("pod_bat_type", OSelection.class).addSelection("1","1P Plug").addSelection("2", "1P Stab in").addSelection("3","Pouch");


  //  OColumn equipment = new OColumn("equipment", CmmsEquipment.class, OColumn.RelationType.ManyToOne);

    public CmmsEquipment(Context context, OUser user) {
        super(context, "cmms.equipment", user);
        setDefaultNameColumn("name");
    }
    @Override
    public Uri uri() {
        return buildURI(AUTHORITY);
    }
    public String getAddress(ODataRow row) {
        String add = "";
        if (!row.getString("address").equals("false"))
            add += row.getString("address") + ", ";
        if (!row.getString("postcode").equals("false"))
            add += " - " + row.getString("postcode") + " ";
        return add;
    }
}

/**
 *  'type': fields.char('Serial Number', size=64,required=True),

 'pp_controller_number': fields.char('Poolpod Controller Number'),+
 'batch_number': fields.char('Production Batch Number'),+
 'pp_serial_number': fields.char('Poolpod Serial Number'),+
 'uk_supp_agr': fields.boolean('UK Support Agreement'),#+
 'remote_serial_number': fields.char('Remote Control Serial Number'),+
 'lanyard_bat_upg': fields.boolean('Is the Lanyard Battery Upgraded'),    #+
 'pod_bat_type': fields.selection(AVAILABLE_POD_BAT_TYPE, 'Pod Battery Type'),+
 'pump_mod_type': fields.selection(AVAILABLE_POD_PUMP_TYPE, 'Pump Module Type'),+
 'pside_con_bat': fields.selection(AVAILABLE_POOLSIDE_BATTERY, 'Poolside Control Battery'),+
 'lift_bat_serial1': fields.char('Lift Battery Serial Number 1'),+
 'lift_bat_serial2': fields.char('Lift Battery Serial Number 2'),+
 'pside_con_bat_serial1': fields.char('Poolside Control Battery Serial Number 1'),
 'pside_con_bat_serial2': fields.char('Poolside Control Battery Serial Number 2'),
 'pside_cont_software': fields.char('Poolside Control Software'),
 'on_switch': fields.selection(AVAILABLE_ON_SWITCH, 'On Switch'),
 'manual_recovery': fields.selection(AVAILABLE_MANUAL_RECOVERY, 'Manual Recovery'),
 'limit_plate_type': fields.selection(AVAILABLE_LIMIT_PLATE, 'Limit Plate Type'),
 'door_lock_sensor': fields.selection(AVAILABLE_DOOR_LOCK, 'Door Lock Sensor'),
 'motor_con': fields.selection(AVAILABLE_MOTOR_CONTROLLER, 'Motor Controller'),
 'rfid_soft': fields.char('RFID Software'),
 'pp_remote_control': fields.selection(AVAILABLE_PP_REMOTE_CONTROL, 'Poolpod Remote Control'),
 'remote_pull_upgraded': fields.boolean('Remote Pull upgraded'),
 'pp_remote_software': fields.char('Remote Software'),
 'pp_remote_charger_type': fields.char('Remote Charger Type'),
 'ramp': fields.selection(AVAILABLE_RAMP, 'Ramp'),
 'platform': fields.selection(AVAILABLE_COLOUR, 'Platform'),
 'trolleys': fields.selection(AVAILABLE_TROLLEYS, 'Trolleys'),
 'wheelchair': fields.selection(AVAILABLE_COLOUR, 'Wheelchair'),
 'short': fields.boolean('Short'),
 'medium': fields.boolean('Medium'),
 'other_anchor': fields.boolean('Other'),
 'low': fields.boolean('Low'),
 'high': fields.boolean('High'),
 'other_feet': fields.boolean('Other'),
 'anchor_extenders': fields.char('Anchor Details'),
 'front_feet': fields.char('Feet Details'),
 ###history###



 'address': fields.char('Address'),a
 'town': fields.char('Town'),a
 'county': fields.char('County'),a
 'postcode': fields.char('Postcode'),a
 'location': fields.char('Location'),a
 'operator': fields.char('Operator'),a
 'main_phone_number': fields.char('Main Telephone'),
 'addit_phone_number': fields.char('Additional Telephone'),

 */