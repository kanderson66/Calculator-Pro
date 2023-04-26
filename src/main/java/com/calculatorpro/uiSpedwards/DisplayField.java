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

import javax.swing.JTextField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.calculatorpro.CalculatorProPlugin;
import java.math.RoundingMode;
import java.text.DecimalFormat;

@EqualsAndHashCode(callSuper = true)
@Data
public class DisplayField extends JTextField
{
    Double result;
    public String previousResult = "0";
    public String input = "";
    public String output = "0";
    boolean calcDone = false;

    //called after hitting =
    protected void calculateResult()
    {
        //kanderson66 edit
        //check end of equation validity
        //split equation into individual components
        String[] components = input.split("(?<=[-+*/^()\\[\\]])|(?=[-+*/^()\\[\\]])");

        //restrict math if equation is only (up to 3) "mathless" operations
        switch (components.length)
        {
            //dont do math on single component
            case 1:
                return;
            //dont do math if only max 1 number
            case 2:
                //check for - or (
                if (components[0].equals("-") || components[0].equals("("))
                {
                    return;
                }
            case 3:
                //check for -( or -- or (- or ((
                if ((components[0].equals("-") || components[0].equals("(")) && (components[1].equals("-") || components[1].equals("(")))
                {
                    return;
                }
        }

        //cant end in math symbol (remove it)
        switch (components[components.length-1])
        {
            case "+":
            case "-":
            case "*":
            case "/":
            case "^":
            case "(":
                components[components.length-1] = "";

                //reform input, re-enter Calculate result
                input = "";
                for (String component : components)
                {
                    input += component;
                }
                calculateResult();
                return;
            //last input is a ### - check if ended in a . (remove it)
            default:
                //# is a single digit, cant be a .
                if (components[components.length-1].length() != 1 ) {
                    //# ended in . (remove it)
                    if (components[components.length-1].charAt(components[components.length-1].length()-1) == '.')
                    {
                        components[components.length-1] = components[components.length-1].substring(0,components[components.length-1].length()-1);
                        break;
                    }
                }
                break;
        }

        //count num Open & closed Parenthesis '(' and ')'
        int countOpenParen = 0;
        int countClosedParen = 0;
        input="";

        for(String component : components){
            switch (component){
                case "(":
                    countOpenParen++;
                    break;
                case ")":
                    countClosedParen++;
                    break;
                default:
                    break;
            }
            //put components back into 1 string if anything changed
            input += component;
        }
        //if more '(' than ')', add as many ')' to end as needed
        if (countOpenParen-countClosedParen != 0)
        {
            for(int n=0; n<(countOpenParen-countClosedParen); n++)
            {
                input += ")";
            }
        }

        //evaluate the math equation string
        result = CalculatorProPlugin.eval(input.replaceAll(",",""));

        //check for divide by 0 error message ("Infinity")
        if(result.toString().charAt(0)=='I'){
            output = "Cant divide by 0";
            calcDone = true;
            return;
        }
        //todo add ability to custom set how many decimals to display from CalculatorProConfig.numDecimals()
//        CalculatorProConfig config = new CalculatorProConfig() {
//            @Override
//            public int numDecimals() {
//                return CalculatorProConfig.super.numDecimals();
//            }
//        };
//        System.out.println(config.numDecimals());
        //reduce answer to 3 decimal places or less & include commas for easier reading
        DecimalFormat df = new DecimalFormat("#,###.###");
        df.setRoundingMode(RoundingMode.HALF_UP);

        output = df.format(result);
        calcDone = true;
        previousResult = output;
    } //end kanderson66 edit

    protected void reset()
    {
        result = 0.0;
        input = "";
        update();
    }

    protected void clear()
    {
        reset();
    }

    protected void update()
    {
        if(calcDone)
        {
            super.setText(this.input + " = " + this.output);
            input = "";
            calcDone = false;
        }
        else {
            super.setText(this.input);
        }
        repaint();
    }
}