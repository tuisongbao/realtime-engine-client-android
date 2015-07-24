package com.tuisongbao.engine.util;
import java.lang.reflect.Field;

import android.util.Log;

public class Resources
{

    /**
     * Retrieves an Android resource id for the given package, class and asset name.
     * 
     * @param packageName   This should be the package of your application from something like Context.getPackageName()
     * @param className     The class of the resource, eg "drawable"
     * @param name          The name of the resource, eg "icon"
     * @return              The Android resource id of the resource or -1 if it wasn't found
     */
    public static int getResourseIdByName(String packageName, String className, String name) 
    {
        int id = 0;
        try 
        {
            for (int i = 0; i < Class.forName(packageName + ".R").getClasses().length; i++) 
            {
                if(Class.forName(packageName + ".R").getClasses()[i].getName().split("\\$")[1].equals(className)) 
                {
                    if(Class.forName(packageName + ".R").getClasses()[i] != null)
                        id = Class.forName(packageName + ".R").getClasses()[i].getField(name).getInt(Class.forName(packageName + ".R").getClasses()[i]);
                    break;
                }
            }
        } 
        catch (Exception e) 
        {
            return -1;
        }
        return id;
    }
    

    
    public static void listResources( String packageName ) 
    {
        try 
        {
            for (int i = 0; i < Class.forName(packageName + ".R").getClasses().length; i++) 
            {
                Field [] fields = Class.forName(packageName + ".R").getClasses()[i].getFields();
                for (int j = 0; j < fields.length; j++)
                {
                    Log.d("Resources", Class.forName(packageName + ".R").getClasses()[i].getName()+"::"+fields[j].getName() );
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
}
