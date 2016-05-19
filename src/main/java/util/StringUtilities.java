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

    public static String getPrintInfo(int line, int col, String msg)
    {
        col++;
        return "(" + line + ", " + col + "): " + msg;
    }
}
