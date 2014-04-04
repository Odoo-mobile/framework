.. OpenERP Mobile documentation master file, created by
   sphinx-quickstart on Tue Mar 25 14:15:37 2014.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Fields
======

varchar
~~~~~~~

Creates varchar (character string) type column in SQLite.

.. code-block:: java

	OEColumn column = new OEColumn("field_name", "Label", OEFields.varchar(30));

integer
~~~~~~~

Creates integer type column in SQLite.

.. code-block:: java
	
	OEColumn column = new OEColumn("field_name", "Label", OEFields.integer(5));

text
~~~~

Creates text type column in SQLite.

.. code-block:: java
	
	OEColumn column = new OEColumn("field_name", "Label", OEFields.text());


oneToMany
~~~~~~~~~

Creates oneToMany relation with column. Used with OpenERP Android framework ORM

.. code-block:: java
	
	OEColumn column = new OEColumn("field_name", "Label", OEFields.oneToMany(new ModelObject(context));


manyToOne
~~~~~~~~~

Creates manyToOne relation with column and also create integer type column in SQLite. Used with OpenERP Android framework ORM.

.. code-block:: java
	
	OEColumn column = new OEColumn("field_name", "Label", OEFields.manyToOne(new ModelObject(context));

manyToMany
~~~~~~~~~~

Creates manyToMany relation with column and create many2many related third table in SQLite based on primary column id. Used with framework ORM.

.. code-block:: java
	
	OEColumn column = new OEColumn("field_name", "Label", OEFields.manyToMany(new ModelObject(context));

blob
~~~~

Creates blob column in SQLite used to store Base64 String.

.. code-block:: java
	
	OEColumn column = new OEColumn("field_name", "Label", OEFields.blob());
	
