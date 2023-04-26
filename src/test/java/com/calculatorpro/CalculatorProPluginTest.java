package com.calculatorpro;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CalculatorProPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(CalculatorProPlugin.class);
		RuneLite.main(args);
	}
}