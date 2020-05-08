package config;

import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.text.NumberFormatter;

public class IntegerTextField extends JFormattedTextField
{
	public IntegerTextField(int value, int minValue, int maxValue)
	{
		super(value);
		NumberFormat format = NumberFormat.getInstance();
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setValueClass(Integer.class);
		formatter.setMinimum(minValue);
		formatter.setMaximum(maxValue);
		formatter.setAllowsInvalid(false);
		setFormatter(formatter);
	}

	public int getInt() throws ParseException
	{
		return NumberFormat.getInstance().parse(getText()).intValue();
	}
}
