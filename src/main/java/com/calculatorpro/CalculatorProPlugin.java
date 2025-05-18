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

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.MessageNode;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatCommandManager;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
//import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.calculatorpro.uiSpedwards.CalculatorPluginPanel;

import java.awt.image.BufferedImage;

import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
//import java.io.InputStream;
//import javax.imageio.ImageIO;
import net.runelite.client.util.ImageUtil;
import net.runelite.api.Skill;    //to get current xp in a skill
////auto equate
//import net.runelite.client.chat.ChatMessageManager;
//import net.runelite.api.ChatMessageType;

@PluginDescriptor(
        name = "Calculator Pro",
        description = "Perform calculations using the side panel or in-game chat commands",
        tags = {"calculator", "math", "chat", "command"}
)
public class CalculatorProPlugin extends Plugin {
    //calc panel from Spedwards
    @Inject
    private ClientToolbar clientToolbar;
    private NavigationButton navButton;
    //end calc panel from Spedwards
    @Inject
    private CalculatorProConfig config;
    @Inject
    private ChatCommandManager chatCommandManager;
    @Inject
    private Client client;

//	//auto equate
//	@Inject
//	private ChatMessageManager chatMessageManager;

    private static final String CALCULATE_STRING = "!calc";

    //store xp values of each lvl (lvl 1-126)
    final HashMap<String, String> lvlTags = new HashMap<>();
    //store custom tags from config panel
    final HashMap<String, String> configTags = new HashMap<>();
    //store tags made by user during runtime (last, etc)
    final HashMap<String, String> runTimeTags = new HashMap<>();
    //store skill current xp values
    final HashMap<String, String> SkillTags = new HashMap<>();

    //store whether the panel is currently active or not
    boolean panelActive = false;

    //track autoTag values
    boolean needAutoTag;
    int autoTag = 0;

    //ignore checking in runTimeTags if new tag is coming from Config textbox
    boolean isConfigTag = false;

    //store the newTagName (after tagName is valid, wait for value to pass check)
    String newTagName;
    //Message to output (Error/result)
    String output;
    //Store equation from Config textbox
    String customEquation = "";

    @Override
    public void startUp() {
        //load lvl xp tags (lvl 1-126)
        loadLvlTags();
        //load skill xp values
        loadSkillTags();
        //load runTime tags (default with a0-a9 with value 0)
        loadRunTimeTags();
        //load tags & equation from Config text boxes
        reloadTags();

        //watch for !calc keyword in chat, perform calculate function if found
        chatCommandManager.registerCommandAsync(CALCULATE_STRING, this::calculate);

        CalculatorPluginPanel panel = new CalculatorPluginPanel();

        BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/icon.png");

        navButton = NavigationButton.builder()
                .tooltip("Calculator Pro")
                .priority(7)
                .icon(icon)
                .panel(panel)
                .build();

        if (config.panelActive()) {
            clientToolbar.addNavigation(navButton);
            panelActive = true;
        }
    }

    @Override
    public void shutDown() {

        chatCommandManager.unregisterCommand(CALCULATE_STRING);

        //calc panel from Spedwards
        if (panelActive) {
            clientToolbar.removeNavigation(navButton);
            panelActive = false;
        }
        //end calc panel from Spedwards
    }

    //adds Calculator Pro to config panel
    @Provides
    CalculatorProConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CalculatorProConfig.class);
    }

    //load tags for xp at each lvl (lvl1-lvl126)
    public void loadLvlTags() {
        //Formula from Wiki 'Experience' page (see 'Formula')
        for (Integer lvl = 1; lvl < 127; lvl++) {
            lvlTags.put("lvl" + lvl, Integer.toString(Integer.valueOf((int) eval("(1/8)*(" + lvl + "^2-" + lvl + "+600*((2^(" + lvl + "/7)-2^(1/7)))/(2^(1/7)-1))-(" + lvl + "/10)"))));
        }
    }

    //load Skill tags, all starting at 0xp
    public void loadSkillTags() {
        SkillTags.put("myattack", "0");
        SkillTags.put("myatt", "0");
        SkillTags.put("myhitpoints", "0");
        SkillTags.put("myhp", "0");
        SkillTags.put("mymining", "0");
        SkillTags.put("mymine", "0");
        SkillTags.put("mystrength", "0");
        SkillTags.put("mystr", "0");
        SkillTags.put("myagility", "0");
        SkillTags.put("myagil", "0");
        SkillTags.put("mysmithing", "0");
        SkillTags.put("mysmith", "0");
        SkillTags.put("mydefence", "0");
        SkillTags.put("mydef", "0");
        SkillTags.put("myherblore", "0");
        SkillTags.put("myherb", "0");
        SkillTags.put("myfishing", "0");
        SkillTags.put("myfish", "0");
        SkillTags.put("myranged", "0");
        SkillTags.put("myrange", "0");
        SkillTags.put("myranging", "0");
        SkillTags.put("mythieving", "0");
        SkillTags.put("mythieve", "0");
        SkillTags.put("mycooking", "0");
        SkillTags.put("mycook", "0");
        SkillTags.put("myprayer", "0");
        SkillTags.put("mypray", "0");
        SkillTags.put("mycrafting", "0");
        SkillTags.put("mycraft", "0");
        SkillTags.put("myfiremaking", "0");
        SkillTags.put("myfm", "0");
        SkillTags.put("mymagic", "0");
        SkillTags.put("mymage", "0");
        SkillTags.put("myfletching", "0");
        SkillTags.put("myfletch", "0");
        SkillTags.put("mywoodcutting", "0");
        SkillTags.put("mywc", "0");
        SkillTags.put("myrunecrafting", "0");
        SkillTags.put("myrc", "0");
        SkillTags.put("myslayer", "0");
        SkillTags.put("myslay", "0");
        SkillTags.put("myfarming", "0");
        SkillTags.put("myfarm", "0");
        SkillTags.put("myconstruction", "0");
        SkillTags.put("mycon", "0");
        SkillTags.put("myhunter", "0");
        SkillTags.put("myhunt", "0");
    }

    //reserve last and a0-a9 tags for autoTag (not a hard reserve)
    private void loadRunTimeTags() {
        runTimeTags.put("last", "0");

        for (; autoTag < 10; autoTag++) {
            runTimeTags.put("a" + autoTag, "0");
        }
        autoTag = 0;
    }

    //detect change in Config textbox, update tags & equation
    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged) {
        if (configChanged.getGroup().equals("calculatorpro")) {
            reloadTags();

            //auto equate check if custom equation changed (and != last custom equation or blank), output to chat (no !calc command needed)
            //client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says ", null);
//			if (configChanged.getKey().equals("customEquation")){
//				chatMessageManager.queue(
//						ChatMessageType.GAMEMESSAGE,
//						"",
//						"Custom equation updated to: "
//				);
//			}

            if (!config.panelActive() && panelActive) {
                clientToolbar.removeNavigation(navButton);
                panelActive = false;
            }
            if (config.panelActive() && !panelActive) {
                clientToolbar.addNavigation(navButton);
                panelActive = true;
            }
        }
    }

    //clear current config panel Tags & equation & update with new tags from Config Text boxes
    private void reloadTags() {
        configTags.clear();
        customEquation = "";

        loadTags(config.customTags());
        loadEquation(config.customEquation());
    }

    //load tags from Config text box
    private void loadTags(String textbox) {
        //HashMap<String, String> swaps = new HashMap<String, String>();
        for (String line : textbox.split("\n")) {
            //get next line if this line is empty
            if (line.trim().equals("")) {
                continue;
            }
            //end of tags, now entering notes
            if (line.trim().contains("###")) {
                break;
            }
            line = line.toLowerCase();
            line = line.replaceAll("\\s", "");
            //Split string between tag & value
            String[] lineSplit = line.split("=");

            //check if tag has name (possibly empty) and value
            if (lineSplit.length != 2) {
                //	System.out.println("no value");
                continue;
            }

            //CF Step 3: check if is custom function (will contain at least 1 ( and ) )
            //check if name of custom function is valid func(2)
            String name = lineSplit[0];
            String value = lineSplit[1];
            String funcValue = "";

            //if, it is a function declaration func(x) = x
            if (name.contains("(") || name.contains(")")) {

                //check # ) never above # (, and add ) if needed
                List<String> compName = tokenize(name);
                if (compName.contains("(") || compName.contains(")")) {
                    //check # ) never above # (, and add ) if needed
                    compName=checkParenth(compName);
                    if (compName.get(0).contains("Error")) {
                        continue;
                    }
                    //merge list of strings back into single string
                    name = "";
                    for (String s : compName) {
                        name += s;
                    }
                }

                //check Custom function variables names are valid
                //extract variables from between the ( )    func(x,y) > x,y
                int start = name.indexOf('(');
                int end = name.lastIndexOf(')');

                //find # of variables in function
                String variables = name.substring(start + 1, end);
                String[] varSplit = variables.split(",");

                //extract name of function before the (, then add number of variables in brackets func(2)
                name = name.substring(0, start).trim() + "(" + varSplit.length + ")";

                //validate the tag/function name
                // config tag skips looking in runTime tags (replace it if exists, after value validated)
                isConfigTag = true;
                if (!checkTagName(name)) {
                    isConfigTag = false;
                    continue;
                }
                isConfigTag = false;

                boolean isError = false;
                for (String s : varSplit) {
                    //ensure variable has a name
                    if (s.isEmpty()) {
                        output = "Error- no tag name";
                        //System.out.println(output);
                        isError = true;
                        break;
                    }
                    //variable contains at least 1 letter
                    if (!s.matches(".*[a-z]+.*")) {
                        output = "Error- tag name/function (no letters): \"" + s + "\"";
                        //System.out.println(output);
                        isError = true;
                        break;
                    }

                    //variable contains only letters and/or #s, and/or ()
                    int len = s.length();
                    for (int i = 0; i < len; i++) {
                        if (!(Character.isLetterOrDigit(s.charAt(i)) || s.charAt(i) == '(' || s.charAt(i) == ')')) {
                            output = "Error- tag name (contains symbols): \"" + s + "\"";
                            //System.out.println(output);
                            isError = true;
                            break;
                        }
                    }
                }

                //error with func variable, end processing config line
                if (isError) {
                    continue;
                }

                //Prepare custom function equation to pass check (replace variables with 1)
                //prepare custom function for storage (replace custom variable names with var0, etc
                //split equation into individual components

                List<String> components = tokenize(value);
                List<String> funcComp = tokenize(value);

                for (int n = 0; n < varSplit.length; n++) {
                    for (int m = 0; m < components.size(); m++) {
                        if (varSplit[n].equals(components.get(m))) {
                            components.set(m, "1");
                            funcComp.set(m, "var" + n);
                        }
                    }
                }

                //func(x,y) = splinters(x)*200
                //splinters(var0)*200

                //if a component has a letter and immediately followed by (, check if is a tag( or func( and replace as needed
                for (int i = 1; i < components.size(); i++) {
                    if (components.get(i).equals("(") && components.get(i - 1).matches(".*[a-zA-Z]+.*")) {
                        //System.out.println("Before entering Nested Functions: " + components);
                        components = evaluateNestedFunctions(components);
                        //System.out.println("After Nested Functions: " + components);

                        //System.out.println("Before entering Nested Functions: " + funcComp);
                        funcComp = evaluateNestedFunctions(funcComp);
                        //System.out.println("After Nested Functions: " + funcComp);

                        if (components.get(0).contains("Error") || funcComp.get(0).contains("Error")) {
                            break;
                        }
                        //re-string all components, then properly re-tokenize (evalNestedFunc will string multiple components together)
                        String tempComp = "";
                        String tempFuncComp = "";
                        for (int n=0; n < components.size(); n++) {
                                tempComp += components.get(n);
                                tempFuncComp += funcComp.get(n);
                        }
                        components = tokenize(tempComp);
                        funcComp = tokenize(tempFuncComp);
                        break;
                    }
                }
                //get next line
                if (components.get(0).contains("Error")  || funcComp.get(0).contains("Error")) {
                    continue;
                }
                //recreate strings with variables replaced with 1's (value) and val0 val1 (funcValue)
                for (int n = 0; n < components.size(); n++) {
                    //this good to replace tags, but not functions in function definition
                    //if components contains a letter, its a tag, evaluate value of tag and put in funcValue
                    if (components.get(n).matches(".*[a-z]+.*")) {
                        if(!mathFunction(components.get(n))) {
                            //funcValue += checkEquation(components[n]);
                            funcValue += evaluateTag(components.get(n));

                            if (funcValue.contains("Error")) {
                                break;
                            }

                        }
                        else {
                            funcValue += components.get(n);
                        }

                        continue;
                    }
                    funcValue += funcComp.get(n);
                }
                value = funcValue;                
            } // not a function declaration, is a tag
            else {

                //validate the tag/function name
                // config tag skips looking in runTime tags (replace it if exists, after value validated)
                isConfigTag = true;
                if (!checkTagName(name)) {
                    isConfigTag = false;
                    continue;
                }
                isConfigTag = false;
                //validate the value of the tag/function
                //String correctedEquation = checkEquation(value);
                value = checkEquation(value);

                //Error with tag value- dont add
                if (value.contains("Error")) {
                    //System.out.println(correctedEquation);
                    continue;
                }
            }

            if (value.contains("Error")) {
                continue;
            }

            //name, with function name being func(2), value being var1+var2*2
            //add tag
            configTags.put(name, value);

            System.out.println("Tag/Func added: " + name + " , " + value);

            //remove tag from runTimeTags if it exists
            if (runTimeTags.get(name) != null) {
                runTimeTags.remove(name);
            }
        }
    }


    //load equation from Config text box
    private void loadEquation(String textbox) {
        //add a new line incase user has equation on 1 line w/o any new lines
        textbox = textbox + "\n";
        String[] equation = textbox.split("\n", 2);

        customEquation = equation[0];
        if (customEquation.isEmpty()) {
            customEquation = "Error- First line in Custom Equation box is empty";
            return;
        }

        //remove any capitals and whitespaces in equation
        customEquation = customEquation.toLowerCase();
        customEquation = customEquation.replaceAll("\\s", "");
    }

    //check that the config or runTime [tag] is valid
    private boolean checkTagName(String newTagString) {
        /*
            ERRORS TO CHECK
            -value not # DONE
            -value has spaces DONE
            -tag name needs at least 1 letter DONE
            -tag name contain only numbers and/or letters DONE
            -tag name doesnt already exist DONE
         */

        //ensure tag has a name
        if (newTagString.isEmpty()) {
            output = "Error- no tag name";
            //System.out.println(output);
            return false;
        }
        //tag name contains at least 1 letter
        if (!newTagString.matches(".*[a-z]+.*")) {
            output = "Error- tag name/function (no letters): \"" + newTagString + "\"";
            //System.out.println(output);
            return false;
        }

        //tag name contains only letters and/or #s, and/or ()
        int len = newTagString.length();
        for (int i = 0; i < len; i++) {
            if (!(Character.isLetterOrDigit(newTagString.charAt(i)) || newTagString.charAt(i) == '(' || newTagString.charAt(i) == ')')) {
                output = "Error- tag name (contains symbols): \"" + newTagString + "\"";
                //System.out.println(output);
                return false;
            }
        }
        //check if tag already exists
        if (configTags.get(newTagString) != null || lvlTags.get(newTagString) != null || SkillTags.get(newTagString) != null) {
            output = "Error- tag \"" + newTagString + "\" already exists";
            //System.out.println(output);
            return false;
        }
        //config tag overrides tags in runTimeTags
        if (!isConfigTag && runTimeTags.get(newTagString) != null) {
            output = "Error- tag \"" + newTagString + "\" already exists";
            //System.out.println(output);
            return false;
        }
        //check if tag is reserved
        switch (newTagString) {
            case "sqrt":
            case "cos":
            case "sin":
            case "tan":
                output = "Error- tag \"" + newTagString + "\" is reserved for math operations";
                //System.out.println(output);
                return false;
            case "last":
            case "a0":
            case "a1":
            case "a2":
            case "a3":
            case "a4":
            case "a5":
            case "a6":
            case "a7":
            case "a8":
            case "a9":
                output = "Error- tag \"" + newTagString + "\" is reserved for default tags";
                //System.out.println(output);
                return false;
            default:
                break;
        }
        return true;
    }

    //perform all checks, commands, calculations and tagging
    private void calculate(ChatMessage chatMessage, String equation) {
        //command turned off
        if (!config.calcCommand()) {
            return;
        }
        output = "";
        newTagName = "";
        needAutoTag = false;
        Double answer = 0.0;

        //reload custom tags & equation incase values in tags changed (gained xp, etc)
        reloadTags();

        equation = equation.toLowerCase();
        //remove any whitespaces in equation	//DOESNT remove ,
        equation = equation.replaceAll("\\s", "");

        //remove !calc beginning
        String[] split = equation.split("!calc");

        //check if nothing entered after !calc
        switch (split.length) {
            case 0:
                //check if Custom Equation was valid
                if (customEquation.charAt(0) == 'E') {
                    build(equation, customEquation, answer.toString(), chatMessage);
                    return;
                }
                //get equation from Custom Equation textbox in Config Panel
                equation = customEquation;
                break;
            case 2:
                //success
                //get equation from chatbox w/o !calc command
                equation = split[1];
                break;
            default:
                //should never reach, but just for safety
                build(equation, "Error- invalid entry", answer.toString(), chatMessage);
                return;
        }

        //check for header (!command or [tagName])
        if (equation.charAt(0) == '!' || equation.charAt(0) == '[') {
            equation = checkHeaders(equation);
            if (output.charAt(0) == 'E' || output.equals("Command Complete") || output.contains("Tags")) {
                //print output
                build(equation, output, answer.toString(), chatMessage);
                return;
            }
        } //equation is removed of !calc and any !commands or [tagName]

        //no [tagName] entered, add new autoTag
        else {
            needAutoTag = true;
        }

        //check equation for errors, replace any tags needed
        String correctedEquation = checkEquation(equation);

        //Check for error message
        if (correctedEquation.contains("Error")) {
            correctedEquation = errorStrip(correctedEquation);
            build(equation, correctedEquation, answer.toString(), chatMessage);
            return;
        }

        //evaluate expression, remove any , present (NEEDED)
        answer = eval(correctedEquation.replaceAll(",", ""));
        //answer = eval(correctedEquation);

        //check for divide by 0 error message ("Infinity")
        if (answer.toString().charAt(0) == 'I') {
            build(equation, correctedEquation, answer.toString(), chatMessage);
            return;
        }

        //reduce answer to desired # decimal places or less & include commas for easier reading
        String format = "#,###";

        if (config.numDecimals() > 0) {
            format += ".";

            //limit decimal points to 15 places
            if (config.numDecimals() > 15) {
                for (int n = 0; n < 15; n++) {
                    format += "#";
                }
            } else {
                for (int n = 0; n < config.numDecimals(); n++) {
                    format += "#";
                }
            }
        }
        DecimalFormat df = new DecimalFormat(format);
        df.setRoundingMode(RoundingMode.HALF_UP);

        //update runTimeTags to include last & custom tag or default tag (a1-a9)
        addNewTag(df.format(answer));

        //output results
        build(equation, correctedEquation, "[" + newTagName + "] " + df.format(answer), chatMessage);
    }

    //check !command or [newTag] are valid
    //returns just equation, no !command or [tagName]
    public String checkHeaders(String input) {
        //input is either [tagName] equation or !command ????
        String[] split;

        switch (input.charAt(0)) {
            //check if new tag name & separate tag name from equation
            case '[':
                split = input.split("\\[");
                split = split[1].split("]");
                //check if tag name was followed by ]
                if (split.length < 2) {
                    output = "Error- missing ']' or missing equation after ']'";
                    return input;
                }
                //remove whitespace from tag name
                split[0] = split[0].replaceAll("\\s", "");
                if (!checkTagName(split[0])) {
                    return input;
                } //split[0] has tagName split[1] has equation

                //valid tag. Save name to assign if value also passes
                newTagName = split[0];
                output = split[1];
                break;
            case '!':
                //!clear command. Clear all saved values, but keep last if currently set
                if (input.equals("!clear")) {
                    String temp = "0";
                    if (runTimeTags.get("last") != null) {
                        temp = runTimeTags.get("last");
                    }
                    runTimeTags.clear();
                    loadRunTimeTags();
                    runTimeTags.replace("last", temp);
                    autoTag = 0;
                    output = "Command Complete";
                    return input;
                }
                //!remove command
                else if (input.charAt(1) == 'r') {
                    split = input.split("!remove");
                    //nothing after !remove
                    if (split.length != 2) {
                        output = "Error- invalid command. Usage: !remove tagName";
                        return input;
                    }

                    //tagName not found in runTimeTags (cant remove tags from other HashMaps)
                    if (runTimeTags.get(split[1]) == null) {
                        output = "Error- tagName not found. Usage: !remove tagName";
                        return input;
                    }
                    //remove the desired tag from runTimeTags
                    runTimeTags.remove(split[1]);
                    output = "Command Complete";
                    return "!remove " + split[1];
                } //end !remove command
                else if (input.charAt(1) == 'l') {
                    split = input.split("!list");
                    //nothing after !list
                    if (split.length != 2) {
                        output = "Error- invalid command.\nUsage: !list RunTimeTags or !list CustomTags";
                        return input;
                    }
                    if (split[1].equals("runtimetags")) {
                        output = "RunTimeTags: \n" + runTimeTags.toString();
                        return input;
                    }
                    else if (split[1].equals("customtags")) {
                        output = "CustomTags: \n" + configTags.toString();
                        return input;
                    }
                    output = "Error- invalid command.\nUsage: !list RunTimeTags or !list CustomTags";
                    return input;
                }
                else {
                    output = "Error- unknown command";
                    return input;
                }
            default:
                //should never reach
                output = "Error- headers";
                return input;
        }//end switch 1st character
        //returns the equation w/o headers
        return output;
    }

    //check equation will evaluate properly (no illegal characters or tags, etc)
    //input just equation (no !command or [tagName])
    public String checkEquation(String input) {

        //check no illegal characters or spaces
        //remove any math symbols
        Pattern p = Pattern.compile("(.{1})");
        Matcher m = p.matcher("(),.+-/*^");
        String temp = input.replaceAll(m.replaceAll("\\\\$1\\|"), "");


        //check if remaining are just letters and #s
        int len = temp.length();
        for (int i = 0; i < len; i++) {
            if (!Character.isLetterOrDigit(temp.charAt(i))) {
                output = "Error- unsupported symbols";
                return output;
            }
        }
        //split equation into individual components
        //String[] components = input.split("(?<=[-+*/^()\\[\\]])|(?=[-+*/^()\\[\\]])");
        List<String> components = tokenize(input);

        if (components.contains("(") || components.contains(")")) {
            //check # ) never above # (, and add ) if needed
            components=checkParenth(components);
            if (components.get(0).contains("Error")) {
                return components.get(0);
            }
        }
        //if a component has a letter and immediately followed by (, check if is a tag( or func( and replace as needed
        for (int i = 1; i < components.size(); i++) {
            if (components.get(i).equals("(") && components.get(i - 1).matches(".*[a-zA-Z]+.*")) {
                //System.out.println("Before entering Nested Functions: " + components);
                components = evaluateNestedFunctions(components);
                //System.out.println("After Nested Functions: " + components);

                if (components.get(0).contains("Error")) {
                    components.set(0, errorStrip(components.get(0)));
                    return components.get(0);
                }
                //re-string all components, then properly re-tokenize (evalNestedFunc will string multiple components together)
                String tempComp = "";
                for (int n=0; n < components.size(); n++) {
                    tempComp += components.get(n);
                }
                components = tokenize(tempComp);

                //check # ) never above # (, and add ) if needed
                components = checkParenth(components);
                if (components.get(0).contains("Error")) {
                    return components.get(0);
                }
                break;
            }
        }



        //check all math operators legal
        for (int n = 0; n < components.size(); n++) {
            // -+*/^( cant be followed by +*/^)
            switch (components.get(n)) {
                case "+":
                case "-":
                case "*":
                case "/":
                case "^":
                case "(":
                    if (n + 1 != components.size()) {
                        switch (components.get(n + 1)) {
                            case "+":
                            case "*":
                            case "/":
                            case "^":
                            case ")":
                                output = "Error- 2 math operators together";
                                return output;
                            default:
                                break;
                        }
                    } else {
                        output = "Error- cant end equation in math operator";
                        return output;
                    }
                default:
                    break;
            }

            //add * after ) if not end of equation, or followed by math operator
            if (components.get(n).equals(")") && n + 1 != components.size()) {
                switch (components.get(n + 1)) {
                    case "+":
                    case "-":
                    case "*":
                    case "/":
                    case ")":
                    case "^":
                    case ",":
                        break;
                    default:
                        components.set(n, ")*");
                        break;
                }
            }
            //dont allow math tag at end of equation
            switch (components.get(n)) {
                case "sqrt":
                case "cos":
                case "sin":
                case "tan":
                    if (n + 1 == components.size()) {
                        output = "Error- math tag not followed by (###)";
                        return output;
                    } else if (!components.get(n + 1).equals("(")) {
                        output = "Error- math tag not followed by (###)";
                        return output;
                    }
                    break;
                default:
                    break;
            }
            //check . is before at least 1 # & only 1 in component
            if (components.get(n).matches(".*[.]+.*")) {
                String[] split = components.get(n).split("\\.");
                if (split.length == 1) {
                    //add 0 after . if needed  2.  >  2.0
                    components.set(n, components.get(n) + "0");
                }
                //is just "." OR has 2 or more . in same # 5..3
                else if(split.length == 0 || split.length > 2) {
                    output = "Error- incorrect decimal usage";
                    return output;
                }
            }
        } //end for

        //if any , remain, is an extra and unnecessary
        if (components.contains(",")) {
            return "Error- extra \",\" in equation or function definition";
        }
        //check remaining tags and ( correctly placed
        for (int n = 0; n < components.size(); n++) {
            //add * before ( if needed  ("56(4-2)" changes to "56*(4-2)")
            //here handles # before (, evaluateFunction handles tag/func before (
            if (components.get(n).equals("(") && n!=0) {
                switch (components.get(n - 1)) {
                    case "+":
                    case "-":
                    case "*":
                    case "/":
                    case "^":
                    case "(":
                    case "*(":
                    case "sqrt":
                    case "cos":
                    case "sin":
                    case "tan":
                        break;
                    default:
                        components.set(n, "*(");
                        break;
                }
            }
            //if component contains a letter, its a tag or scientific notation (ignore math tags (sqrt, sin, cos, tan))
            if (components.get(n).matches(".*[a-zA-Z]+.*")) {
                components.set(n, evaluateTag(components.get(n)));
            } //end if
        }

        //convert components back into 1 string
        output = "";
        for (String component : components) {
            output += component;
        }
        //System.out.println("Tags Replaced in checkEquation: " + output);
        return output;
    }

    //evaluate string math equation
    public static double eval(final String str) {
        System.out.println("Enter eval: " + str);
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)` | number
            //        | functionName `(` expression `)` | functionName factor
            //        | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (; ; ) {
                    if (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (; ; ) {
                    if (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return +parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    if (!eat(')')) throw new RuntimeException("Missing ')'");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    if (eat('(')) {
                        x = parseExpression();
                        if (!eat(')')) throw new RuntimeException("Missing ')' after argument to " + func);
                    } else {
                        x = parseFactor();
                    }
                    switch (func) {
                        case "sqrt":
                            x = Math.sqrt(x);
                            break;
                        case "sin":
                            x = Math.sin(Math.toRadians(x));
                            break;
                        case "cos":
                            x = Math.cos(Math.toRadians(x));
                            break;
                        case "tan":
                            x = Math.tan(Math.toRadians(x));
                            break;
                        default:
                            throw new RuntimeException("Unknown function: " + func);
                    }
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

    //Lookup current xp for desired skill
    public String xpLookup(String skill) {
        switch (skill) {
            case "myattack":
            case "myatt":
                return Integer.toString(client.getSkillExperience(Skill.ATTACK));
            case "myhitpoints":
            case "myhp":
                return Integer.toString(client.getSkillExperience(Skill.HITPOINTS));
            case "mymining":
            case "mymine":
                return Integer.toString(client.getSkillExperience(Skill.MINING));
            case "mystrength":
            case "mystr":
                return Integer.toString(client.getSkillExperience(Skill.STRENGTH));
            case "myagility":
            case "myagil":
                return Integer.toString(client.getSkillExperience(Skill.AGILITY));
            case "mysmithing":
            case "mysmith":
                return Integer.toString(client.getSkillExperience(Skill.SMITHING));
            case "mydefence":
            case "mydef":
                return Integer.toString(client.getSkillExperience(Skill.DEFENCE));
            case "myherblore":
            case "myherb":
                return Integer.toString(client.getSkillExperience(Skill.HERBLORE));
            case "myfishing":
            case "myfish":
                return Integer.toString(client.getSkillExperience(Skill.FISHING));
            case "myranged":
            case "myranging":
            case "myrange":
                return Integer.toString(client.getSkillExperience(Skill.RANGED));
            case "mythieving":
            case "mythieve":
                return Integer.toString(client.getSkillExperience(Skill.THIEVING));
            case "mycooking":
            case "mycook":
                return Integer.toString(client.getSkillExperience(Skill.COOKING));
            case "myprayer":
            case "mypray":
                return Integer.toString(client.getSkillExperience(Skill.PRAYER));
            case "mycrafting":
            case "mycraft":
                return Integer.toString(client.getSkillExperience(Skill.CRAFTING));
            case "myfiremaking":
            case "myfm":
                return Integer.toString(client.getSkillExperience(Skill.FIREMAKING));
            case "mymagic":
            case "mymage":
                return Integer.toString(client.getSkillExperience(Skill.MAGIC));
            case "myfletching":
            case "myfletch":
                return Integer.toString(client.getSkillExperience(Skill.FLETCHING));
            case "mywoodcutting":
            case "mywc":
                return Integer.toString(client.getSkillExperience(Skill.WOODCUTTING));
            case "myrunecrafting":
            case "myrc":
                return Integer.toString(client.getSkillExperience(Skill.RUNECRAFT));
            case "myslayer":
            case "myslay":
                return Integer.toString(client.getSkillExperience(Skill.SLAYER));
            case "myfarming":
            case "myfarm":
                return Integer.toString(client.getSkillExperience(Skill.FARMING));
            case "myconstruction":
            case "mycon":
                return Integer.toString(client.getSkillExperience(Skill.CONSTRUCTION));
            case "myhunter":
            case "myhunt":
                return Integer.toString(client.getSkillExperience(Skill.HUNTER));
            default:
                return "Error in xpLookup";
        }
    }

    //add new tag & value to runTimeTags
    public void addNewTag(String value) {
        //add auto tag (no tag name provided)
        if (needAutoTag) {
            //only hold 10 auto-tags (a0-a9), then write-over old auto tags
            if (autoTag == 10) {
                autoTag = 0;
            }

            newTagName = "a" + autoTag++;

            if (runTimeTags.get(newTagName) != null) {
                runTimeTags.remove(newTagName);
            }
            runTimeTags.put(newTagName, value);
        }

        //add new tag & value
        else {
            runTimeTags.put(newTagName, value);
        }

        //update "last" tag to last result value
        if (runTimeTags.get("last") != null) {
            runTimeTags.remove("last");
        }
        runTimeTags.put("last", value);
    }

    //build chat message (error or result)
    public void build(String equ, String corEqu, String ans, ChatMessage chatMessage) {
        String response;
        //If input equation is more than 45 characters, just write "Your long equation equals "
        if (equ.length() > 45) {
            equ = " Your long equation";
        }
        //error message received, or command (!clear, !remove, !list) completed (C & R)
        if (corEqu.charAt(0) == 'E' || corEqu.charAt(0) == 'C' || corEqu.charAt(0) == 'R') {
            //trim error message between "Error" and possibly "@&"
            if (corEqu.charAt(0) == 'E') {
                corEqu = errorStrip(corEqu);
            }
            //System.out.println(equ + " " + corEqu);
            response = new ChatMessageBuilder()
                    .append(ChatColorType.NORMAL)
                    .append(equ)
                    .append(ChatColorType.HIGHLIGHT)
                    .append(" " + corEqu)
                    .build();
        } else if (ans.charAt(0) == 'I') {
            //System.out.println(equ + " Error- cannot divide by 0");
            //build new message
            response = new ChatMessageBuilder()
                    .append(ChatColorType.NORMAL)
                    .append(equ)
                    .append(ChatColorType.HIGHLIGHT)
                    .append(" Error- number too large, or cannot divide by 0")
                    .build();
        } else {
            //build new message
            response = new ChatMessageBuilder()
                    .append(ChatColorType.NORMAL)
                    .append(equ)
                    .append(ChatColorType.HIGHLIGHT)
                    .append(" equals ")
                    .append(ChatColorType.NORMAL)
                    .append(ans) //removed .toString()
                    .build();
        }

        //print new message to chatbox
        final MessageNode messageNode = chatMessage.getMessageNode();
        messageNode.setRuneLiteFormatMessage(response);
        client.refreshChat();
    }

    // Tokenizer that includes words, numbers, operators, parentheses, and commas
    public static List<String> tokenize(String input) {
        //this pattern properly supports tokens starting with a #  50k
        //Pattern pattern = Pattern.compile("\\d+(?:\\.\\d+)?[a-zA-Z0-9]+|[a-zA-Z][a-zA-Z0-9]*|\\d+(?:\\.\\d+)?|[+\\-*/(),]");
        //this pattern meant to be the same, but also allow decimal numbers w/o leading # like .4
//        Pattern pattern = Pattern.compile(
//                "(?:\\d+\\.\\d*|\\.\\d+|\\d+)(?:[a-zA-Z0-9]+)?|" +   // numbers with optional trailing letters (e.g., 2.5kg, .4kg)
//                        "[a-zA-Z][a-zA-Z0-9]*|" +                            // variable names (e.g., x1, var2)
//                        "[+\\-*/(),^]"
//        );

//        Pattern pattern = Pattern.compile(
//                "(?:\\d+\\.\\d+|\\.\\d+|\\d+)[a-zA-Z][a-zA-Z0-9]*|" +  // number with unit, e.g., 45.5fss, .4kg
//                        "\\d+\\.\\d+|\\.\\d+|\\d+|" +                          // plain numbers
//                        "[a-zA-Z][a-zA-Z0-9]*|" +                              // variables
//                        "[+\\-*/()^,]"                                         // operators
//        );
//        Matcher matcher = pattern.matcher(input);
//
//        List<String> tokens = new ArrayList<>();
//        while (matcher.find()) {
//            tokens.add(matcher.group());
//        }
//        return tokens;

        // Split while keeping the delimiters (math operators)
        String[] rawTokens = input.split("(?=[+\\-*/()^,])|(?<=[+\\-*/()^,])");

        // Remove empty strings and trim whitespace
        List<String> tokens = new ArrayList<>();
        for (String token : rawTokens) {
            String trimmed = token.trim();
            if (!trimmed.isEmpty()) {
                tokens.add(trimmed);
            }
        }
        return tokens;
    }

    // Recursively evaluate all nested functions
    private List<String> evaluateNestedFunctions(List<String> tokens) {
        List<String> result = new ArrayList<>(tokens);

        while (true) {
            boolean changed = false;

            for (int i = 1; i < result.size(); i++) {
                if (result.get(i).equals("(") && result.get(i - 1).matches(".*[a-zA-Z]+.*")) {
                    String functionName = result.get(i - 1);
                    int closingIndex = findMatchingClosingParen(result, i);
                    if (closingIndex != -1) {
                        List<String> innerTokens = new ArrayList<>(result.subList(i + 1, closingIndex));
                        List<String> argStrings = splitFunctionArguments(innerTokens);

                        // Evaluate each argument recursively
                        List<String> evaluatedArgs = new ArrayList<>();
                        for (String arg : argStrings) {
                            List<String> argTokens = tokenize(arg);
                            List<String> evaluated = evaluateNestedFunctions(argTokens);
                            evaluatedArgs.add(String.join("", evaluated));
                        }

                        // Simulate function evaluation
                        String value = evaluateFunction(functionName, evaluatedArgs);

                        // Replace function call with its result
                        result.set(i - 1, value);
                        result.subList(i, closingIndex + 1).clear();

                        changed = true;
                        break; // Start over after modification
                    }
                }
            }

            if (!changed) break;
        }
    //evaluateNestedFunctions return
        return result;
    }

    // Finds the index of the closing parenthesis that matches the one at startIndex
    private static int findMatchingClosingParen(List<String> tokens, int startIndex) {
        int depth = 1;
        for (int i = startIndex + 1; i < tokens.size(); i++) {
            if (tokens.get(i).equals("(")) depth++;
            else if (tokens.get(i).equals(")")) depth--;
            if (depth == 0) return i;
        }
        return -1;
    }

    // Splits tokens by commas, respecting nested parentheses
    private static List<String> splitFunctionArguments(List<String> tokens) {
        List<String> args = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;

        for (String token : tokens) {
            if (token.equals("(")) depth++;
            if (token.equals(")")) depth--;

            if (token.equals(",") && depth == 0) {
                args.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(token);
            }
        }

        if (current.length() > 0) {
            args.add(current.toString().trim());
        }

        return args;
    }

    // Stub for evaluating a function with arguments
    //func(2,func(3,4))
    //receive func(3,4)
    //then receive func(2,func(3,4))
    private String evaluateFunction(String name, List<String> args) {
        //System.out.println("EvaluateFunction begin: " + name + " with args: " + args);
        //receive func(3,4)
        //var1+var2*var1+4
        for (int n=0; n< args.size(); n++) {
            if (args.get(n).isEmpty()) {
                return "Error- variable is empty";
            }
        }

        //oak(5)  >  oak , 5   >   oak(1)
        //Check if name and size is a stored function
        String checkForFunc = name + "(" + args.size() + ")" ;
        if(configTags.get(checkForFunc) != null) {
            //get function definition (var1+var2*var1+4)
            List<String> funcDef = tokenize(configTags.get(checkForFunc));

            //replace var placeholders with passed args
            List<String> indexesChanged = new ArrayList<>();
            for (int n=0; n<args.size(); n++) {
                for (int m=0; m<funcDef.size(); m++) {
                    //dont replace variable twice (can change to var1 on first loop, then match again on 2nd loop and change)
                    if (funcDef.get(m).equals("var"+n) && !indexesChanged.contains(Integer.toString(m))) {
                        indexesChanged.add(Integer.toString(m));
                        funcDef.set(m,args.get(n));
                    }
                }
            }
            //component of function can contain multiple components splinters(oak+500,15)  >  oak+500 , 15    > need check if multiple items, send back into check equation
            //check if any component still has letters (tags), replace them now if possible
            //dont replace if component is still var# (used when function is within another functions definition) func(x,y) = splinters(x)*200
            for (int n=0; n<funcDef.size(); n++) {
                if (funcDef.get(n).matches(".*[a-z]+.*") && !funcDef.get(n).matches("var\\d+")) {

                    // New block loop
                    List<String> temp = tokenize(funcDef.get(n));
                    if (temp.size() > 1) {
                        funcDef.set(n, "(" + funcDef.get(n) + ")");
                        funcDef.set(n,checkEquation(funcDef.get(n)));
                        if (funcDef.get(n).contains("Error")) {
                            funcDef.set(n, errorStrip(funcDef.get(n)));
                            return funcDef.get(n);
                        }
                    }
                    else {
                        funcDef.set(n,evaluateTag(funcDef.get(n)));

                        if (funcDef.get(n).contains("Error")) {
                            funcDef.set(n, errorStrip(funcDef.get(n)));
                            //System.out.println("Error, no \"" + name + "\" tag or function with matching parameters found@&");
                            return funcDef.get(n);
                        }
                    }
                }
            }
            name = "(";
            for (String s:funcDef) {
                name += s;
            }
            name += ")";
        }
        //not a function, is tag(contents). Check if tag exists, and contents are valid (no , and valid math)
        else {
            //oak , 5
            name = evaluateTag(name);
            if (name.charAt(0) == 'E') {
                //System.out.println("Error, no \"" + name + "\" tag or function with matching parameters found@&");
                return name;
            }
            String temp ="";
            for (String component : args) {
                temp += component;
            }
            //checkEquation checks validity of formattings, and replaces tags/handles functions
            temp = checkEquation(temp);
            if (temp.contains("Error")) {
                temp = errorStrip(temp);
                return temp;
            }

            //close math function cos(45)
            if (mathFunction(name)){
                name += "(";
                temp += ")";
            }

            else {
                name += "*";
            }

            name += temp;

        }
        //System.out.println("Evaluate Function end: " + name);
        return name;
    }
    //Receives a single component, return tag value if found
    //Leave math tags (sqrt, cos, etc), Evaluate scientific notation, Replace tag with tag value
    private String evaluateTag(String name) {
        //for (int n = 0; n < name.length(); n++) {

        //if component contains a letter, its a tag or scientific notation (ignore math tags (sqrt, sin, cos, tan))
        if (name.matches(".*[a-zA-Z]+.*") && !mathFunction(name)) {
            //check for scientific notation
            //component would be # ended in k m or b (10.6k)
            //String[] split = name.split("(?=[k|m|b])");
            String[] split = name.split("(?<=\\d)(?=[kmb]$)");

            //check for possible scientific notation (### k)
            if (split[0].matches("\\d+.+|\\d+") && split.length == 2) {

                boolean isScientific = false;

                //check if 2nd index is scientific notation
                switch (split[1]) {
                    case "k":
                        name = "(" + split[0] + "*1000)";
                        isScientific = true;
                        break;
                    case "m":
                        name = "(" + split[0] + "*1000000)";
                        isScientific = true;
                        break;
                    case "b":
                        name = "(" + split[0] + "*1000000000)";
                        isScientific = true;
                        break;
                    default:
                        break;
                }
                //dont check if is a tag if is scientific notation
                if (isScientific) {
                    return name;
                }
            }

            //check if is a tag
            if (configTags.get(name) != null) {
                name = configTags.get(name);
            } else if (lvlTags.get(name) != null) {
                name = lvlTags.get(name);
            } else if (runTimeTags.get(name) != null) {
                name = runTimeTags.get(name);
            } else if (SkillTags.get(name) != null) {
                name = xpLookup(name);
            } else {
                if (name.contains(".")) {
                    output = "Error- tag or function name can't contain \".\" or symbols";
                    return output;
                }
                output = "Error- no \"" + name + "\" tag or function with matching parameters found@&";
                System.out.println(output);
                return output;
            }
        } //end if
        return name;
    }
    private boolean mathFunction(String item) {
        switch (item) {
            case "sqrt":
            case "cos":
            case "sin":
            case "tan":
                return true;
            default:
                return false;
        }
    }
    private String errorStrip(String error) {
        int start = error.indexOf("Error");
        if (start == -1) return ""; // Or handle "Error" not found

        int end = error.indexOf("@&", start);
        if (end == -1) {
            return error.substring(start).trim(); // No "@&" found; return from "Error" to end
        } else {
            return error.substring(start, end).trim(); // Return from "Error" to before "@&"
        }
    }

    List<String> checkParenth(List<String> components) {
        int countOpenParen = 0;
        int countClosedParen = 0;
        //for(int n=0;n<length;n++){
        for (String component : components) {
            switch (component) {
                case "(":
                    countOpenParen++;
                    break;
                case ")":
                    countClosedParen++;
                    if (countClosedParen > countOpenParen) {
                        components.set(0,"Error- found ) before (");
                        return components;
                    }
                    break;
                default:
                    break;
            }
        }
        //add missing ) if needed
        for (int n=0; n<(countOpenParen-countClosedParen); n++){
            components.add(")");
        }
        return components;
    }

}

