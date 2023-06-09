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

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JButton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CalculatorButton extends JButton
{
    private static final Dimension PREFERRED_SIZE = new Dimension(55, 55);

    //Cyborger1 edit
    private static final Dimension MINIMUM_SIZE = new Dimension(54, 55);
    //end Cyborger1 edit

    public CalculatorButton(String text)
    {
        super(text);

        setPreferredSize(PREFERRED_SIZE);
        //Cyborger1 edit
        setMinimumSize(MINIMUM_SIZE);
        //end Cyborger1 edit

        // Use Arial. Zero and Asterisk look funny in "Runescape Standard"
        setFont(new Font("Arial", Font.BOLD, 20));
    }

//    public CalculatorButton(Icon icon)
//    {
//        super(icon);
//
//        setPreferredSize(PREFERRED_SIZE);
//        //Cyborger1 edit
//        setMinimumSize(MINIMUM_SIZE);
//        //end Cyborger1 edit
//    }
}