package util;

/**
 * Created by JuanSebastian on 18/05/2016.
 */
public class StringUtilities
{
    public static String getStyledID(String oldID)
    {
        char[] chars = new char[oldID.length()];
        oldID.getChars(0, oldID.length(), chars, 0);

        String styledID = "";

        for (char c : chars)
        {
            if (Character.isUpperCase(c))
            {
                if (!styledID.endsWith("_")) styledID += "_";
                c = Character.toLowerCase(c);
            }

            styledID += c;
        }

        return styledID.startsWith("_") ? styledID.substring(1) : styledID;
    }

    public static String getStyledStructID(String oldID)
    {
        String styledID = "";
        boolean toUpper = true;

        for (int i = 0; i < oldID.length(); i++)
        {
            String current = oldID.substring(i, i + 1);

            if (current.equals("_"))
            {
                toUpper = true;
                current = "";
            }
            else
            {
                if (toUpper)
                {
                    current = current.toUpperCase();
                    toUpper = false;
                }
            }

            styledID += current;
        }

        return styledID;
    }

    public static String getPrintInfo(int line, int col, String msg)
    {
        col += 1;
        return "(" + line + ", " + col + "): " + msg;
    }
}
