package ICG;

import a.f.S;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by abhishek on 11/11/16.
 */
public class VariableMap
{
    HashMap<String,Integer> local_var_map = null;
    HashMap<String,Integer> global_var_map = null;
    int local_index;
    int global_index;

    public VariableMap()
    {
        local_var_map = new HashMap<>();
        global_var_map = new HashMap<>();
        local_index = 0;
        global_index = 0;
    }

    public int getMapSize(boolean isGlobal)
    {
        if(isGlobal)
            return global_var_map.size();
        else
            return local_var_map.size();
    }

    public void addToMap(boolean isGlobal,String key)
    {
        if (isGlobal)
            addToGlobal(key,global_index++);
        else
            addToLocal(key,local_index++);
    }

    public void removeFromMap(boolean isGlobal,String key)
    {
        if(isGlobal)
            removeGlobalKey(key);
        else
            removeLocalKey(key);
    }

    public void clearMap(boolean isGlobal)
    {
        if(isGlobal)
            clearGlobal();
        else
            clearLocal();
    }

    public void addToGlobal(String key,int value)
    {
        global_var_map.put(key,value);
    }

    public void addToLocal(String key,int value)
    {
        local_var_map.put(key,value);
    }

    public void removeGlobalKey(String key)
    {
        global_var_map.remove(key);
    }

    public void removeLocalKey(String key)
    {
        local_var_map.remove(key);
    }

    public void clearLocal()
    {
        local_var_map.clear();
        local_index = 0;
    }

    public void clearGlobal()
    {
        global_var_map.clear();
        global_index = 0;
    }

    public boolean isPresent(boolean isGlobal,String key)
    {
        if(isGlobal)
            return global_var_map.containsKey(key);
        else
            return local_var_map.containsKey(key);
    }

    public int getValue(boolean isGlobal,String key)
    {
        if(isGlobal)
            return global_var_map.get(key);
        else
            return local_var_map.get(key);
    }

    public void printMap(boolean isGlobal)
    {
        System.out.println("Printing Map");
        if(isGlobal)
        {
            for (Map.Entry<String,Integer> ent : global_var_map.entrySet())
            {
                System.out.println(ent.getKey() + "=>" + ent.getValue());
            }
        }
        else
        {
            for (Map.Entry<String,Integer> ent : local_var_map.entrySet())
            {
                System.out.println(ent.getKey() + "=>" + ent.getValue());
            }
        }
    }
}