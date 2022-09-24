/*
 *  Copyright 2011 Justin Stoecker
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jsgl.jogl.util.shader;

import java.util.ArrayList;
import javax.swing.*;
import jsgl.jogl.Shader;
import jsgl.jogl.Uniform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Reads a shader file for custom UI widget comments for interacting with the
 * shader in an application.
 *
 * @author justin
 */
public class ShaderWidgetParser
{
	public static class SWCheckbox extends ShaderWidget
	{
		public SWCheckbox(String name, String uName)
		{
			this.add(new JCheckBox(name));
			this.uniformName = uName;
		}
	}

	public static class SWSlider extends ShaderWidget
	{
		public void setVariable(Uniform.Float var)
		{
			this.uVariable = var;
		}

		public SWSlider(String name, String uName, float min, float max, float defaultValue)
		{
			this.add(new JLabel(name));
			this.uniformName = uName;
			final JSlider slider = new JSlider((int) (min * 10.0f), (int) (max * 10.0f));
			slider.setValue((int) (defaultValue * 10.0f));
			slider.addChangeListener(e -> {
				if (uVariable != null) {
					valueChanged = true;
					((Uniform.Float) uVariable).setValue(slider.getValue() / 10.0f);
				}
			});
			this.add(slider);
		}
	}

	private static final Logger LOGGER = LogManager.getLogger();

	private static final String WIDGET_SEQ = "ui_widget:";

	// ui_widget: slider, "float ambient", "Ambient", 0.0, 1.0
	// ui_widget(vec3_slider, "vec3 lightDir", "Light Direction")
	// ui_widget(vec3_slider, "vec3 lightColor", "Light Color")
	// ui_widget(checkbox, "bool useLighting")
	// ui_widget(slider, "float specExponent", "Specular Exponent", 0.0, 128.0);

	private enum WidgetType
	{
		SLIDER,
		VEC3_SLIDER,
		CHECKBOX,
	}

	private enum UniformType
	{
		BOOL,
		INT,
		FLOAT,
		VEC2,
		VEC3,
		VEC4
	}

	public static ArrayList<ShaderWidget> parse(Shader s)
	{
		return parse(s.getSourceLines());
	}

	public static ArrayList<ShaderWidget> parse(String[] src)
	{
		// go through each line in shader source code and check for ui widget
		// declarations that follow correct format:
		// ui_widget: <type>, <uniform declaration>, <widget name>, <param1>, ...
		ArrayList<ShaderWidget> widgets = new ArrayList<>();
		for (String line : src) {
			int paramsIndex;
			if ((paramsIndex = line.indexOf(WIDGET_SEQ)) != -1) {
				paramsIndex += WIDGET_SEQ.length();
				String[] params = line.substring(paramsIndex).trim().split(",");
				for (int i = 0; i < params.length; i++) {
					params[i] = params[i].trim();
					if (params[i].startsWith("\"") && params[i].endsWith("\""))
						params[i] = params[i].substring(1, params[i].length() - 1);
				}

				ShaderWidget widget = parseWidget(params);
				if (widget != null) {
					widgets.add(widget);
				}
			}
		}
		return widgets;
	}

	private static ShaderWidget parseWidget(String[] params)
	{
		// declaration should have at least a type, declaration, and name
		if (params.length < 3)
			return null;

		// try to parse the widget type and ignore line on failure
		WidgetType widgetType = null;
		try {
			widgetType = WidgetType.valueOf(params[0].toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}

		// try to parse uniform declaration and ignore line on failure. proper
		// declaration should have only 2 parts: the uniform type and its name
		UniformType uniformType = null;
		String[] uDecParts = params[1].split("\\s+");
		if (uDecParts.length != 2)
			return null;
		try {
			uniformType = UniformType.valueOf(uDecParts[0].toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
		String uniformName = uDecParts[1];

		// third parameter is the name applied to the widget
		String widgetName = params[2];

		// create desired widget using rest of parameters if necessary
		String[] restOfParams = null;
		if (params.length > 3) {
			restOfParams = new String[params.length - 3];
			System.arraycopy(params, 3, restOfParams, 0, restOfParams.length);
		}
		switch (widgetType) {
		case CHECKBOX:
			return createCheckbox(uniformType, uniformName, widgetName);
		case SLIDER:
			return createSlider(uniformType, uniformName, widgetName, restOfParams);
		case VEC3_SLIDER:
			break;
		}

		// if type wasn't found, ignore this and return null
		return null;
	}

	private static SWCheckbox createCheckbox(UniformType uType, String uName, String widgetName)
	{
		LOGGER.debug("new sw checkbox");
		return new SWCheckbox(widgetName, uName);
	}

	private static SWSlider createSlider(UniformType uType, String uName, String widgetName, String[] params)
	{
		float defaultValue = Float.parseFloat(params[0]);
		float min = Float.parseFloat(params[1]);
		float max = Float.parseFloat(params[2]);
		return new SWSlider(widgetName, uName, min, max, defaultValue);
	}
}
