package ui;

/**
 * Created by JuanSebastian on 27/05/2016.
 */
public class ConsoleUI implements UI
{
    @Override
    public void displayText(String txt)
    {
        System.out.println(txt);
    }
}
