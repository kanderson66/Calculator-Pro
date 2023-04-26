/*
 * Copyright (c) 2020, Spedwards <https://github.com/Spedwards>
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
package com.calculatorpro.uiSpedwards;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.RoundingMode;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.text.DecimalFormat;

public class CalculatorPanel extends JPanel
{
    private static final Insets INSETS_LEFT_BORDER = new Insets(1, 0, 1, 1);
    private static final Insets INSETS_RIGHT_BORDER = new Insets(1, 1, 1, 0);
    private static final Insets INSETS = new Insets(1, 1, 1, 1);

    private final CalculatorPluginPanel panel;
    private final DisplayField displayField;
    private final GridBagConstraints c;

    protected CalculatorPanel(CalculatorPluginPanel panel)
    {
        super();
        this.panel = panel;
        this.displayField = panel.getDisplayField();

        setLayout(new GridBagLayout());
        //Cyborger1 edit
        setBorder(new EmptyBorder(0, 1, 0, 1));
        //end Cyborger1 edit

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        //Cyborger1 edit
        c.weightx = 1;
        c.weighty = 1;
        //end Cyborger1 edit

//        CalculatorButton plusMinus = new CalculatorButton(PLUS_MINUS_ICON);

        addButton("+");
        addButton("7");
        addButton("8");
        addButton("9");

        addButton("-");
        addButton("4");
        addButton("5");
        addButton("6");

        addButton("*");
        addButton("1");
        addButton("2");
        addButton("3");

        addButton("/");
        addButton(".");
        addButton("0");
        addButton("=");


        addButton("(");
        addButton(")");
        addButton("^");
        addButton("<");
//        addComp(plusMinus);

        addButton("C");
        c.gridwidth = 3;
        addButton("Clear History");
    }

    private void addButton(String key)
    {
        //todo: add keyboard inputs (ctrl+v, nums, backspace, etc)
        CalculatorButton btn = new CalculatorButton(key);
        btn.addActionListener(e ->
        {
            //create string from button inputs, validate key presses on the go, dont allow invalid key presses
            String text = btn.getText();
            switch (text) {
                case "=":
                    if (displayField.input.equals("")) {
                        return;
                    }

                    //add extra ) if needed, check end of equation valid
                    displayField.calculateResult();

                    // Add new calculation to history
                    if(displayField.calcDone){
                        panel.getHistoryPanel().addHistoryItem(displayField.input + " =", displayField.output);
                    }
                    break;

                //clear display
                case "C":
                    displayField.clear();
                    return;

                //clear history
                case "Clear History":
                    panel.getHistoryPanel().clearHistory();
                    displayField.previousResult = "0";
                    return;

                //kanderson66 edit
                //backspace function for display panel
                case "<":
                    displayField.input = deleteLastCharacter(displayField.input);
                    break;
                //input is a #
                default:
                    displayField.input += text;
                    //input not valid, remove last input
                    if (!inputValid(displayField.input.replaceAll(",", ""))) {
                        displayField.input = deleteLastCharacter(displayField.input);
                        return;
                    }
                    break;
                    //end kanderson66 edit
            }
            displayField.update();
        });
        addComp(btn);
    }

    private void addComp(Component component)
    {
        switch (c.gridx)
        {
            case 0:
                c.insets = INSETS_LEFT_BORDER;
                break;
            case 3:
                c.insets = INSETS_RIGHT_BORDER;
                break;
            default:
                c.insets = INSETS;
        }
        if (c.gridwidth == 3)
        {
            c.insets = INSETS_RIGHT_BORDER;
        }
        add(component, c);
        c.gridx = ++c.gridx % 4;
        c.gridy = c.gridx == 0 ? ++c.gridy : c.gridy;
    }

    //kanderson66 edit
    private String deleteLastCharacter(String string)
    {
        if (string.equals(""))
        {
            return string;
        }
        return string.substring(0, string.length() - 1);
    }

    private boolean inputValid(String input)
    {
        //split equation into individual components
        String[] components = input.split("(?<=[-+*/^()\\[\\]])|(?=[-+*/^()\\[\\]])");

        //check # ) never above # (
        int length=components.length;
        int countOpenParen = 0;
        int countClosedParen = 0;

        for(String component : components){
            switch (component){
                case "(":
                    countOpenParen++;
                    break;
                case ")":
                    countClosedParen++;
                    if(countClosedParen>countOpenParen){
                        return false;
                    }
                    break;
                default:
                    break;
            }
        }

        //check all math operators legal
        for(int n=0;n<length;n++){
            //grab last result if first digit pressed is math symbol & display has not been cleared
            if (n==0  && !displayField.previousResult.equals("0") && length == 1){
                switch(components[n]){
                    case "+":
                    case "-":
                    case "*":
                    case "/":
                    case "^":
                        //ensure last result enter properly w comma
                        String temp = components[n];
                        components[n] = displayField.previousResult.replaceAll(",","") + temp;
                        //re-submit value to properly separate previousResult and math operator
                        if (inputValid(components[n].replaceAll(",","")))
                        {
                            return true;
                        }
                        return false;
                    default:
                        break;
                }
            }
            //  +*/^) cant come after +-*/^( or start of equation
            switch(components[n]){
                case "+":
                case "*":
                case "/":
                case "^":
                case ")":
                    if(n != 0){
                        //check if previous component was ###. with no number after decimal (remove last decimal)
                        //# of length 1 does not have a .
                        if (components[n-1].length() != 1 ) {
                            if (components[n - 1].substring(components[n - 1].length() - 1, components[n - 1].length()).equals("."))
                            {
                                components[n - 1] = components[n - 1].substring(0, components[n - 1].length() - 1);
                                break;
                            }
                        }
                        switch (components[n-1]){
                            case "+":
                            case "-":
                            case "*":
                            case "/":
                            case "^":
                            case "(":
                                return false;
                            default:
                                break;
                        }
                    }
                    //cant have math symbol at start of equation (except ( or -)
                    else
                    {
                        return false;
                    }
            }
            //check proper input before (
            if(components[n].equals("(") && n!=0){
                //check if last digit is ., if so, delete . and add *  (4.*3 change to 4*3)
                if (components[n - 1].substring(components[n-1].length()-1, components[n-1].length()).equals("."))
                {
                    components[n-1] = components[n-1].substring(0,components[n-1].length()-1);
                    components[n] = "*(";
                    break;
                }
                //add * before ( if needed  ("56(4-2)" changes to "56*(4-2)")
                switch (components[n-1]){
                    case "+":
                    case "-":
                    case "*":
                    case "/":
                    case "^":
                    case "(":
                        break;
                    default:
                        components[n] = "*(";
                        break;
                }
            }

            //check . is before at least 1 # & only 1 in component
            if(components[n].matches(".*[.]+.*")){
                String[] split = components[n].split("\\.");
                switch (split.length)
                {
                    // "."
                    case 0:
                        components[n] = "0.";
                        break;
                    // "4."
                    case 1:
                        break;
                    // "4.4" or "4.4."
                    case 2:
                        //check if last character is .
                        if (components[n].charAt(components[n].length()-1) == '.')
                        {
                            return false;
                        }
                        break;
                    // "4.4.4"
                    //should not reach- should be corrected when 2nd . entered
                    default:
                        return false;
                }
            }
        } //end for

        //include commas for easier reading, display max 15 decimal points in input field
        //(answer field decimal limit is set separately in DisplayField.java)
        DecimalFormat df = new DecimalFormat("#,###.###############");
        df.setRoundingMode(RoundingMode.HALF_UP);

        //convert components back into 1 string, update displayField input incase changed
        displayField.input="";
        for(int n=0;n<length;n++){
            if(components[n].length() > 3)
            {
                //todo: check if this change is fine
//                String change = df.format(Double.parseDouble(components[n]));
//                displayField.input += change;
                displayField.input += df.format(Double.parseDouble(components[n]));
            }
            else
            {
                displayField.input+=components[n];
            }
        }
        return true;
    }
    //end kanderson66 edit
}


