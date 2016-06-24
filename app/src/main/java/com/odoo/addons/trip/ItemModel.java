package com.odoo.addons.trip;

/**
 * Created by Sylwek on 23/06/2016.
 */

import android.animation.TimeInterpolator;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.odoo.R;

public class ItemModel {
    private  int colorId1;
    private int colorId2;
    private  TimeInterpolator interpolator;
    private String customerid;
    private String type;

    private String startdate;
    private String state ;
    private String trip ;
    private String order1;
    private String description ;
    private String enddate;
    private String driving_time ;
    private String distance;
    private int equipment_id ;
    private String equipment_rev ;
    //v2
    private String scheduled_time ;
    private boolean action ;
    private boolean installation;
    private boolean training ;
    private boolean loler ;
    private String customer ;
    private int customerID ;
    private String intervention ;
    private String interventionobservation ;
    private String interventionmotif ;
    private String controller_number;
    //v3
    private String equipment_id1 ;
    private String equipment_id1type ;
    private String equipment_id1customer ;
    private Bitmap t;
    private Bitmap i ;
    private Bitmap a ;
    private Bitmap l ;
    private Bitmap p ;
    private Bitmap r ;
    private boolean pick_up;
    private boolean replacement ;

    public ItemModel()
    {

    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public String getController_number() {
        return controller_number;
    }

    public void setController_number(String controller_number) {
        this.controller_number = controller_number;
    }

    public String getEquipment_rev() {
        return equipment_rev;
    }

    public void setEquipment_rev(String equipment_rev) {
        this.equipment_rev = equipment_rev;
    }

    public String getInterventionobservation() {
        return interventionobservation;
    }

    public void setInterventionobservation(String interventionobservation) {
        this.interventionobservation = interventionobservation;
    }


    public String getInterventionmotif() {
        return interventionmotif;
    }

    public void setInterventionmotif(String interventionmotif) {
        this.interventionmotif = interventionmotif;
    }

    public Bitmap getT() {
        return t;
    }

    public void setT(Bitmap t) {
        this.t = t;
    }

    public Bitmap getI() {
        return i;
    }

    public void setI(Bitmap i) {
        this.i = i;
    }

    public Bitmap getA() {
        return a;
    }

    public void setA(Bitmap a) {
        this.a = a;
    }

    public Bitmap getL() {
        return l;
    }

    public void setL(Bitmap l) {
        this.l = l;
    }

    public Bitmap getP() {
        return p;
    }

    public void setP(Bitmap p) {
        this.p = p;
    }

    public Bitmap getR() {
        return r;
    }

    public void setR(Bitmap r) {
        this.r = r;
    }

    public int getColorId1() {
        return colorId1;
    }

    public void setColorId1(int colorId1) {
        this.colorId1 = colorId1;
    }

    public int getColorId2() {
        return colorId2;
    }

    public void setColorId2(int colorId2) {
        this.colorId2 = colorId2;
    }

    public TimeInterpolator getInterpolator() {
        return interpolator;
    }

    public void setInterpolator(TimeInterpolator interpolator) {
        this.interpolator = interpolator;
    }

    public String getCustomerid() {
        return customerid;
    }

    public void setCustomerid(String customerid) {
        this.customerid = customerid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTrip() {
        return trip;
    }

    public void setTrip(String trip) {
        this.trip = trip;
    }

    public String getOrder1() {
        return order1;
    }

    public void setOrder1(String order1) {
        this.order1 = order1;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public String getDriving_time() {
        return driving_time;
    }

    public void setDriving_time(String driving_time) {
        this.driving_time = driving_time;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public int getEquipment_id() {
        return equipment_id;
    }

    public void setEquipment_id(int equipment_id) {
        this.equipment_id = equipment_id;
    }

    public String getScheduled_time() {
        return scheduled_time;
    }

    public void setScheduled_time(String scheduled_time) {
        this.scheduled_time = scheduled_time;
    }

    public boolean isAction() {
        return action;
    }

    public void setAction(boolean action) {
        this.action = action;
    }

    public boolean isInstallation() {
        return installation;
    }

    public void setInstallation(boolean installation) {
        this.installation = installation;
    }

    public boolean isTraining() {
        return training;
    }

    public void setTraining(boolean training) {
        this.training = training;
    }

    public boolean isLoler() {
        return loler;
    }

    public void setLoler(boolean loler) {
        this.loler = loler;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getIntervention() {
        return intervention;
    }

    public void setIntervention(String intervention) {
        this.intervention = intervention;
    }

    public String getEquipment_id1() {
        return equipment_id1;
    }

    public void setEquipment_id1(String equipment_id1) {
        this.equipment_id1 = equipment_id1;
    }

    public String getEquipment_id1type() {
        return equipment_id1type;
    }

    public void setEquipment_id1type(String equipment_id1type) {
        this.equipment_id1type = equipment_id1type;
    }

    public String getEquipment_id1customer() {
        return equipment_id1customer;
    }

    public void setEquipment_id1customer(String equipment_id1customer) {
        this.equipment_id1customer = equipment_id1customer;
    }

    public boolean isPick_up() {
        return pick_up;
    }

    public void setPick_up(boolean pick_up) {
        this.pick_up = pick_up;
    }

    public boolean isReplacement() {
        return replacement;
    }

    public void setReplacement(boolean replacement) {
        this.replacement = replacement;
    }




}
