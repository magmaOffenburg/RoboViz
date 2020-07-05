package config;

import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.JFormattedTextField;
import javax.swing.text.NumberFormatter;

public class IntegerTextField extends JFormattedTextField
{
	public IntegerTextField(int value, int minValue, int maxValue)
	{
		super(new NumberFormatter(NumberFormat.getNumberInstance()));
		setValue(value);
		NumberFormatter formatter = (NumberFormatter) getFormatter();
		formatter.setValueClass(Integer.class);
		formatter.setMinimum(minValue);
		formatter.setMaximum(maxValue);
		formatter.setAllowsInvalid(false);
	}

	public int getInt() throws ParseException
	{
		return NumberFormat.getInstance().parse(getText()).intValue();
	}
}
