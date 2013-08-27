package com.openerp.support;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.content.Context;

import com.openerp.MainActivity;
import com.openerp.base.ir.AttachmentFragment;
import com.openerp.base.res.ResFragment;
import com.openerp.config.ModulesConfig;
import com.openerp.orm.BaseDBHelper;
import com.openerp.orm.SQLStatement;

public class Boot {
    private final java.util.logging.Logger logger = java.util.logging.Logger
	    .getLogger(this.getClass().toString());
    private Context context;
    private ArrayList<Module> modules = null;
    private ArrayList<SQLStatement> statements = null;

    public Boot(Context context) {

	((MainActivity) context).getActionBar()
		.setDisplayShowTitleEnabled(true);

	this.context = context;
	this.modules = new ModulesConfig().applicationModules();
	this.statements = new ArrayList<SQLStatement>();
	loadBaseModules();
	this.initDatabase();

    }

    private void loadBaseModules() {
	this.modules.add(new Module("base_res", "Res_Partner",
		new ResFragment(), 0));
	this.modules.add(new Module("ir_attachment", "Ir_Attachment",
		new AttachmentFragment(), 0));
    }

    private boolean initDatabase() {
	for (Module module : this.modules) {
	    try {
		Class newClass = Class.forName(module.getModuleInstance()
			.getClass().getName());
		if (newClass.isInstance(module.getModuleInstance())) {
		    Object receiver = newClass.newInstance();
		    Class params[] = new Class[1];
		    params[0] = Context.class;

		    Method method = newClass.getDeclaredMethod(
			    "databaseHelper", params);

		    Object obj = method.invoke(receiver, this.context);
		    BaseDBHelper dbInfo = (BaseDBHelper) obj;
		    SQLStatement statement = dbInfo.createStatement(dbInfo);
		    dbInfo.createTable(statement);
		}

	    } catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (NoSuchMethodException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (IllegalArgumentException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (IllegalAccessException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (InvocationTargetException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    } catch (InstantiationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

	return false;
    }

    public ArrayList<SQLStatement> getAllStatements() {
	return this.statements;
    }

    public ArrayList<Module> getModules() {
	// TODO Auto-generated method stub
	return modules;
    }

}
