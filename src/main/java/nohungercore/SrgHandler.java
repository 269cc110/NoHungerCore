package nohungercore;

import java.util.HashMap;

public class SrgHandler
{
    private static boolean isObf;
    private static HashMap<String, String> map;

    static
    {
        map = new HashMap<String, String>();

        try
        {
            Class.forName("yz", false, ClassLoader.getSystemClassLoader());
            isObf = true;
        }
        catch (Exception e)
        {
            isObf = false;
        }

        if (isObf)
        {
            map.put("renderGameOverlay", "func_73830_a");
            map.put("foodStats", "field_71100_bB");
        }
    }

    public static String getString(String s)
    {
        if (!isObf)
        {
            return s;
        }

        String result = map.get(s);

        return result == null ? s : result;
    }
}