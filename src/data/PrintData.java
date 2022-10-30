package data;

import com.fs.starfarer.api.Global;
import data.intel.DevUtilsOverlay;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrintData
{
    public Map<String, String> DisplayScriptValues(Object script) throws Throwable {
        Class methodClass = Class.forName("java.lang.reflect.Field", false, Class.class.getClassLoader());
        MethodHandle valueField = MethodHandles.lookup().findVirtual(methodClass, "get", MethodType.methodType(Object.class, Object.class));
        MethodHandle nameField = MethodHandles.lookup().findVirtual(methodClass, "getName", MethodType.methodType(String.class));
        MethodHandle access = MethodHandles.lookup().findVirtual(methodClass, "setAccessible", MethodType.methodType(void.class, boolean.class));

        int size = script.getClass().getDeclaredFields().length;

        Map<String, String> map = new HashMap<>();

        Object[] test = script.getClass().getDeclaredFields();
        for (Object obj : test) {

            access.invoke(obj, true);
            Object value = valueField.invoke(obj, script);
            Object name = nameField.invoke(obj);

            String valueText = "";

            if (value instanceof Boolean || value instanceof String || value instanceof Float || value instanceof Integer || value instanceof Long )
            {
                valueText += value.toString();
            }
            else if (value != null)
            {
                valueText += value.getClass().getSimpleName() + "@" + value.hashCode();
            }
            else
            {
                valueText += "null";
            }

            map.put(name.toString(), valueText);
        }
/*
        for (int i = 0; i < size; i++) {

            access.invoke(script.getClass().getDeclaredFields()[i], true);
            Object value = valueField.invoke(script.getClass().getDeclaredFields()[i], null);
            Object name = nameField.invoke(script.getClass().getDeclaredFields()[i]);

            text += name.toString() + ":   " + value.toString() + "\n";
            map.put(name.toString(), value.toString());
        }*/

        return map;
    }
}
