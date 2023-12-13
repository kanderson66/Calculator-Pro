/*
 * Copyright (c) 2023,  <https://github.com/kanderson66/Calculator-Pro>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.calculatorpro;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("calculatorpro")
public interface CalculatorProConfig extends Config {
	@ConfigItem(
			position = 1,
			keyName = "panel",
			name = "Panel Active",
			description = "Configures whether to turn the Calculator panel on or off"
	)
	default boolean panelActive()
	{
		return true;
	}

	@ConfigItem(
			position = 2,
			keyName = "calculate",
			name = "Calculator Command",
			description = "Configures whether the Calculator command is enabled  !calc 3*4"
	)
	default boolean calcCommand()
	{
		return true;
	}

	@ConfigItem(
			position = 3,
			keyName = "numDecimals",
			name = "Decimals to Display",
			description = "Configures how many decimal places to display on the Calculator chat result (max 15)"
	)
	default int numDecimals() {
		return 3;
	}

	//Make section for instructions, so they can be collapsible
	@ConfigSection(
			name = "Chatbox Instructions",
			description = "How to use the calculator in the chatbox",
			position = 4,
			closedByDefault = false
	)
	String instructionSection = "instructions";

	//STRING VALUE (TEXTBOX) collapsible instructions
	@ConfigItem(
			position = 5,
			keyName = "instructions",
			name = "Instructions",
			description = "How to use the calculator in the chatbox",
			section = instructionSection
	)
	default String instructions()
	{
		return "Call the calculator through the chatbox using:" +
				"\n!calc 2+3\n\n" +
				"You can create a tag for the result: \n!calc [tagName] 2+3" +
				"\n\nIf a [tagName] is not provided for the result, an auto-generated name will be supplied\n\n" +
				"You can reference tags in your equation:\n" +
				"!calc 5*tagName+3\n\n"+
				"You can clear all tags created through the chatbox, or only remove a certain tag:\n" +
				"!calc !clear\n!calc !remove tagName\n\n" +
				"There are some built-in tags:\n" +
				"lvl1  --experience at lvl 1, up to\nlvl99\n" +
				"last  --result of last calculation\n\n" +
				"Math functions such as ^2, sqrt, sin, cos, tan are also supported:\n" +
				"!calc sqrt(9^3)\n\n" +
				"Handles complex calculations:\n!calc 5*((tagName+2)-cos(2+6)/3)";
	}

	@ConfigSection(
			name = "Custom Tags",
			description = "Text-based custom tags",
			position = 6)
	String customTagsSection = "Custom Tags";

	@ConfigItem(
			keyName = "customTags",
			name = "Custom Tags",
			description = "Tags to use in the calculator",
			section = customTagsSection,
			position = 7
	)
	default String customTags() {
		return "tag1=123\noak=37.5\nexample=oak+tag1\n\n###\nInsert tags above this line, notes below this line"
				+"\n\nTags should look like this:\n"
				+"tagName=###\n\nWhere \"tagName\" is at least 1 letter, and can contain letters and numbers, but no symbols"
				+"\n\nAnd \"###\" is a real number or equation (negative numbers, scientific notation (33k), fractions and decimals are ok)"
				+"\n\nYou can then reference these tags in your equation\n!calc 4*tagName";
	}

	@ConfigSection(
			name = "Custom Equation",
			description = "Text-based custom equation",
			position = 8)
	String customEquationSection = "Custom Equation";

	@ConfigItem(
			keyName = "customEquation",
			name = "Custom Equation",
			description = "Equation to execute by calling \"!calc\" with nothing after it",
			section = customEquationSection,
			position = 9
	)
	default String customEquation() {
		return "[tag] (lvl99-lvl60)/oak\n\n" +
				"###\nWrite the equation you want to execute on the top line of this textbox. " +
				"Long equations that wrap onto a new line are ok\n\n" +
				"Execute this equation in-game by calling: \n" +
				"!calc \n" +
				"in-game with nothing after it";
	}
}