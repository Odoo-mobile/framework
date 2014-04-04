.. OpenERP Mobile documentation master file, created by
   sphinx-quickstart on Tue Mar 25 14:15:37 2014.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Methods
=======

Methods are based on each ModelObject which inherits OEDatabase class.

Local Database Methods
----------------------

create
~~~~~~

Used to create new record in Local database.

Syntax:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	
	OEValues values = new OEValues();
	
	values.put("name", "ABC");
	values.put("age", 10);
	
	long new_id = model.create(values); // Create new record and return new created id.

createORReplace
~~~~~~~~~~~~~~~

Used to create record if not exits otherwise, update record. It takes OEValues list as parameter.

Syntax:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	
	List<OEValues> records = new OEValues();
	
	OEValues values1 = new OEValues(); // will going to create because there is no record in db.
	values1.put("name", "XYZ");
	values1.put("age", 15);
	
	records.add(values1);
	
	OEValues values2 = new OEValues(); // will going to update record because local id exists
	values2.put("name", "ABC");
	values2.put("age", 12);
	
	records.add(values2);
	
	List<Long> mAffectedIds = model.createORReplace(records); // will return affected ids.


select
~~~~~~
Used to select records from Local SQLite database.

Syntax:

.. code-block:: java
	
	ModelObject model = new ModelObject(context);
	
	List<OEDataRow> records = model.select();		// Select all record from SQLite
	OEDataRow record = model.select(id);			// Select specific record.
	
	List<OEDataRow> records = model.select("age > ?", new String[] { "10" }); // select records with custom where clause
	
	
update
~~~~~~

Used to update local record.

Syntax:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	
	OEValues values = new OEValues();
	values.put("name", "ABC");
	values.put("age", 15);
	
	int affected_rows = model.update(values, 10); // will return affected rows.
	
	values = new OEValues();
	values.put("age", 15);
	
	affected_rows = model.update(values, "age = ?", new String[] {"10"});
	

updateManyToManyRecords
~~~~~~~~~~~~~~~~~~~~~~~

Used to update many to many records of related column.

Possible operation to update Many to many records.

**Operation.ADD**

Add given ids to related table

**Operation.APPEND**

Append given ids with existing rows

**Operation.REMOVE**

Remove given ids from many to many relation table

**Operation.REPLACE** 

It first remove all related rows and than replace with new one

Syntax:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	
	List<Integer> ids = new ArrayList<Integer>();
	
	ids.put(10);
	ids.put(12);
	ids.put(13);
	
	// updateManyToManyRecords("relation_column_name", Operation, "record_id", "relation_record_ids OR id")
	
	model.updateManyToManyRecords("subject_ids", Operation.REPLACE, 10, ids);	

delete
~~~~~~

Used to delete record from local database

Syntax:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	
	int count = model.delete(10); // returns number of record deleted.
	
	count = model.delete("age > ? ", new String[] {"20"}); // returns number of record deleted.

truncateTable
~~~~~~~~~~~~~

Used to clean table records.

Syntax:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	
	boolean cleaned = model.truncateTable(); // returns boolean flag 

count
~~~~~

Used to count number of records available in local SQLite database

Syntax:

.. code-block:: java
	
	ModelObject model = new ModelObject(context);
	
	int count = model.count();  // returns total number of records
	
	int mCount = model.count("age > ?", new String[] {"5"}); // returns total number of records based on condition

lastId
~~~~~~

Used to get table records last id.

Syntax:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	
	int last_id = model.lastId();

isEmptyTable
~~~~~~~~~~~~

Used to check whether table is empty or not

Syntax:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	
	if(model.isEmptyTable()){
		//TODO: next stuff
	}


Server related methods
----------------------

getOEInstance
~~~~~~~~~~~~~

Used to get OpenERP helper instance. Which allows you to interact with OpenERP server.

Syntax:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	
	OEHelper openerp = model.getOEInstance();

login
~~~~~

Used to create session between OpenERP server and mobile app.

Syntax:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	OEHelper openerp = model.getOEInstance();
	
	// Create session between application and server. Returns User object
	OEUser user = openerp.login("username", "password", "database", "OpenERP server URL");

isModelInstalled
~~~~~~~~~~~~~~~~

Used to check whether model installed on server or not.

Syntax:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	OEHelper openerp = model.getOEInstance();
	
	if(openerp.isModelInstalled("note.note")){
		//TODO: next stuff
	}	


syncWithServer
~~~~~~~~~~~~~~

Used to sync data with server. It will automatically takes all columns from ModelObject and request to server. After getting response it will store each record and its relation records to its specific tables.

Syntax:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	OEHelper openerp = model.getOEInstance();
	
	// Returns True, if sync finished.
	if(openerp.syncWithServer()){
		//TODO: sync completed. next stuff
	}

Possible parameters:

.. code-block:: java

	// all default parameters
	syncWithServer()
	
	// take boolean. If true, will remove local record if it not exists on server.
	syncWithServer(boolean removeLocalRecordIfNotExists)
	
	// take OEDomain, If passed, will filter requesting records from server.
	syncWithServer(OEDomain domain)
	
	// take two argumetns as said above.
	syncWitServer(OEDomain domain, boolean removeLocalRecordIfNotExists)
	
Example:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	OEHelper openerp = model.getOEInstance();
	
	OEdomain domain = new OEDomain();
	domain.put("age", ">", 10);

	boolean done = openerp.syncWithServer(domain);
	
syncWithMethod
~~~~~~~~~~~~~~

This method will used when you have created your model based on server's custom method result colums.

Syntax:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	OEHelper openerp = model.getOEInstance();
	
	OEArguments args = new OEArgumets();
	
	List<Integer> ids = new ArrayList<Integer>();
	ids.put(10);
	ids.put(20);
	
	args.add(ids);
	args.add(true);
	args.addNull();
	
	// OEArguments will create : [[10,20], true, null]
	
	boolean done = openerp.syncWithMethod("get_users", args);


create
~~~~~~

Used to create record on server. After creating record on server ORM will create that record in local database by itself.

Syntax:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	OEHelper openerp = model.getOEInstance();
	
	OEValues values = new OEValues();
	values.put("name", "PQR");
	values.put("age", 14);
	
	int new_id = openerp.create(values); // returns new created id

update
~~~~~~

Used to udpate server record. After updating on server it will update record in local database.

Syntax:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	OEHelper openerp = model.getOEInstance();
	
	OEValues values = new OEValues();
	values.put("age", 15);
	
	if(openerp.update(values, 20)){
		// updated
	}


delete
~~~~~~

Used to remove record from server after removing on server will remove from local.

Syntax:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	OEHelper openerp = model.getOEInstance();

	openrp.delete(20);

search_read
~~~~~~~~~~~

Used to select record from server. Will return List of OEDataRow.

Syntax:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	OEHelper openerp = model.getOEInstance();

	List<OEDataRow> records = openerp.search_read();

search_read_remain
~~~~~~~~~~~~~~~~~~

Used to select record that are not exists on local database. > last id.

Syntax:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	OEHelper openerp = model.getOEInstance();

	List<OEDataRow> records = openerp.search_read_remain();

call_kw
~~~~~~~

Used to call server custom method. Returns JSONObject.

Syntax:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	OEHelper openerp = model.getOEInstance();
	
	OEArguments args = new OEArguments();
	
	List<Integer> ids = new ArrayList<Integer>();
	ids.put(10);
	ids.put(20);
	
	args.put(ids);
	args.put(true);
	args.putNull();
	
	JSONObject result = openerp.call_kw("get_users", args);

Possible parameters:

.. code-block:: java

	// method and arguments
	call_kw(String method, OEArguments arguments)
	
	// method, arguments and user context (if any)
	call_kw(String method, OEArguments arguments, JSONObject context)
	
	// method, arguments, user context and kwargs if any
	call_kw(String method, OEArguments arguments, JSONObject context, JSONObject kwargs)
	
	// model, method, arguments, user context and kwargs if any
	call_kw(String model, String method, OEArguments arguments, JSONObject context, JSONObject kwargs)

Getting user context:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	OEHelper openerp = model.getOEInstance();
	
	JSONObject newContextValues = new JSONObject();
	JSONObject context = openerp.openERP().updateContext(newContextValues);

getRemovedRecords
~~~~~~~~~~~~~~~~~

Used after sync finish. If any record removed from local than it will return list of removed records.

Syntax:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	OEHelper openerp = model.getOEInstance();
	
	if(openerp.syncWithServer()){
		List<OEDataRow> removedRecords = openerp.getRemovedRecords();
	}

getAffectedIds
~~~~~~~~~~~~~~

Used after sync finish. If any record affected after sync it will return list of ids.

Syntax:

.. code-block:: java

	ModelObject model = new ModelObject(context);
	OEHelper openerp = model.getOEInstance();

	if(openerp.syncWithServer()){
		List<Integer> ids = openerp.getAffectedIds();
	}
