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

import java.awt.BorderLayout;
import lombok.Getter;
import net.runelite.client.ui.PluginPanel;

public class CalculatorPluginPanel extends PluginPanel
{
    private final DisplayPanel displayPanel = new DisplayPanel();

    @Getter
    private final HistoryPanel historyPanel = new HistoryPanel();

    public CalculatorPluginPanel()
    {
        super();

        CalculatorPanel calculatorPanel = new CalculatorPanel(this);

        setLayout(new BorderLayout(5, 5));

        add(displayPanel, BorderLayout.NORTH);
        add(calculatorPanel, BorderLayout.CENTER);
        add(historyPanel, BorderLayout.SOUTH);
    }

    protected DisplayField getDisplayField()
    {
        return displayPanel.getDisplayField();
    }
}