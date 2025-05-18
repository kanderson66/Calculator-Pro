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
			name = "Instructions/Features",
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
		return "Call the calculator through the chatbox using the !calc command:" +
				"\n!calc 2+3\n\n" +
				"You can create a tag for the result: \n!calc [tagName] 2+3\n\n" +
				"You can reference tags in your equation:\n" +
				"!calc 5*tagName+3\n\n" +
				"Handles complex calculations:\n!calc 5*((tagName+2)-lvl90+1.3m)\n\n" +
				"For a complete set of included commands and tags, see the “Pre-set Tags and Commands” infobox below"
				;
	}

	@ConfigSection(
			name = "Custom Tags",
			description = "Text-based custom tags",
			position = 6,
			closedByDefault = true
	)
	String customTagsSection = "Custom Tags";
	@ConfigItem(
			keyName = "customTags",
			name = "Custom Tags",
			description = "Tags to use in the calculator",
			section = customTagsSection,
			position = 7
	)
	default String customTags() {
		return "tag1=123\n" +
				"oak=37.5\n" +
				"xp=oak+tag1\n" +
				"func(x,y)=x+y*2+x+oak\n\n" +
				"###\n" +
				"Insert tags above this line, notes below this line"
				+"\n\nTags should look like this:\n"
				+"tagName=###\n\n" +
				"Where \"tagName\" is at least 1 letter, and can contain letters and numbers, but no symbols"
				+"\n\nYou can then reference these tags in your equation\n" +
				"!calc 4*tagName\n\n"
				+ "Also supports custom functions. Define a function and its varables:\n"
				+ "func(x,y)=x+y+oak*3\n\n"
				+ "Then call it in-game, with the desired values:\n" +
				"!calc func(2,3)";
	}

	@ConfigSection(
			name = "Custom Equation",
			description = "Text-based custom equation",
			position = 8,
			closedByDefault = true
	)
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
				"###\nWrite the equation you want to execute on the top line of this textbox.\n\n" +
				"Execute this equation in-game by calling: \n" +
				"!calc \n" +
				"in-game with nothing after it";
	}

	@ConfigSection(
			name = "Pre-set Tags and Commands",
			description = "Ready-to-use Tags and Commands",
			position = 10,
			closedByDefault = true
	)
	String presetTagsSection = "Pre-set Tags and Commands";
	@ConfigItem(
			keyName = "presetTags",
			name = "Preset Tags",
			description = "Ready-to-use Tags and Commands",
			section = presetTagsSection,
			position = 11
	)
	default String presetTags() {
		return ""
				+ "Math functions such as ^2, sqrt, sin, cos, tan are supported:\n"
				+ "!calc sqrt(9^3)\n\n"
				+ "Tags:\n"
				+ "Include this tag to enter the result of the last calculation:\n"
				+ "last\n"
				+ "!calc last+200k\n\n"
				+ "Level Tags:\n"
				+ "Include these tags to enter the xp for the desired lvl (1-126)\n"
				+ "lvl1\n"
				+ "lvl2\n"
				+ "...\n"
				+ "lvl126\n"
				+ "!calc lvl90-lvl87\n\n"
				+ "Skill Current xp Tags:\n"
				+ "Enter your current xp in the desired skill:\n"
				+ "myatt OR myattack\n"
				+ "!calc lvl90-myfm\n\n"
				+ "Commands:\n"
				+ "Remove all user-created tags:\n"
				+ "!calc !clear\n\n"
				+ "Remove a user-created tag:\n"
				+ "!calc !remove tagName\n\n"
				+ "List all stored CustomTags or RunTimeTags:\n"
				+ "!calc !list RunTimeTags\n"
				+ "!calc !list CustomTags"
				;
	}

//	@ConfigSection(
//			name = "Update Log",
//			description = "Update Log",
//			position = 12,
//			closedByDefault = true
//	)
//	String updateLogSection = "Update Log";
//	@ConfigItem(
//			keyName = "updateLog",
//			name = "Update Log",
//			description = "Update Log",
//			section = updateLogSection,
//			position = 13
//	)
//	default String updateLog() {
//		return "Apr 2025\n" +
//				"     -Current xp Lookup\n" +
//				"     -Config Panel Updated\n\n" +
//				"Dec 2023\n" +
//				"     -Scientific Notation\n" +
//				"     -lvl100 to lvl126 pre-sets added\n\n" +
//				"Apr 2023\n" +
//				"     -Plugin Created\n\n\n" +
//				"Click 'Reset' button below to get all updated notes and info from developer\n\n" +
//				"NOTE:\n" +
//				"This will delete ALL custom tags, equations and notes you may have stored in any infoboxes in this config panel"
//				;
//	}

	@ConfigSection(
			name = "Bugs/Improvements",
			description = "Bugs/Improvements",
			position = 12,
			closedByDefault = true
	)
	String bugsSection = "Bugs/Improvements";
	@ConfigItem(
			keyName = "bugs/Improvements",
			name = "Bugs/Improvements",
			description = "Bugs/Improvements",
			section = bugsSection,
			position = 13
	)
	default String bugsImprovements() {
		return "Please submit any bugs or improvement requests to:\n\n" +
				"https://github.com/kanderson66/Calculator-Pro/issues"
				;
	}
}