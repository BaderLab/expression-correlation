package org.baderlab.expressioncorrelation.internal.view;

import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.text.html.HTMLEditorKit;

/**
 * * Copyright (c) 2005 Memorial Sloan-Kettering Cancer Center
 * *
 * * Code written by: Gary Bader
 * *
 * * This library is free software; you can redistribute it and/or modify it
 * * under the terms of the GNU Lesser General Public License as published
 * * by the Free Software Foundation; either version 2.1 of the License, or
 * * any later version.
 * *
 * * This library is distributed in the hope that it will be useful, but
 * * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * * documentation provided hereunder is on an "as is" basis, and
 * * Memorial Sloan-Kettering Cancer Center
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no type shall the
 * * Memorial Sloan-Kettering Cancer Center
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * Memorial Sloan-Kettering Cancer Center
 * * has been advised of the possibility of such damage.  See
 * * the GNU Lesser General Public License for more details.
 * *
 * * You should have received a copy of the GNU Lesser General Public License
 * * along with this library; if not, write to the Free Software Foundation,
 * * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 * *
 * * User: Gary Bader
 * * Date: Jan 3, 2005
 * * Time: 5:47:31 PM
 * * Description: An about dialog box for Expression Correlation Network plugin
 */

/**
 * An about dialog box for Expression Correlation Network plugin
 */
public class AboutDialog extends JDialog {

	private static final long serialVersionUID = 641571959376323128L;

	public AboutDialog(final Frame parentFrame) {
        super(parentFrame, "About", false);
        setResizable(false);

        //main panel for dialog box
        final JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setEditorKit(new HTMLEditorKit());
        editorPane.setText("<html><body><P align=center>Expression Correlation Network Plugin v1.1 (May 2007)<BR>" +
                "written by Elena Potylitsine and Weston Whitaker<BR>" +
                "during the Cornell/MSKCC summer student program 2004<BR>" +
                "Chris Sander Group<BR>" +
                "Computational Biology Center<BR>" +
                "Memorial Sloan-Kettering Cancer Center<BR>" +
                "New York City</p></body></html>");
        
        setContentPane(editorPane);
    }
}
