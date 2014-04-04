.. OpenERP Mobile documentation master file, created by
   sphinx-quickstart on Tue Mar 25 14:15:37 2014.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Drawer Menu
===========

Menus are based on each module of android app. It contains different layout parameter to display different type of menu.

To generate different menu items with icon, tag color and badge we use different constructors.

.. code-block:: java

    @Override
    public List<DrawerItem> drawerMenus(Context context) {
	    List<DrawerItem> menu = new ArrayList<DrawerItem>();

	    menu.add(new DrawerItem(TAG, "Sample Menus", true));
	    menu.add(new DrawerItem(TAG, "Simple Menu", 8, 0, object("all")));
	    menu.add(new DrawerItem(TAG, "Color Menu", 100, "#cc0000", object("all")));
	    menu.add(new DrawerItem(TAG, "Icon Menu", 10, R.drawable.ic_action_about, object("")));
	    return menu;
    }
    
.. image:: images/drawer_menu.png
    :width: 400px
