/* GROOVE: GRaphs for Object Oriented VErification
 * Copyright 2003--2011 University of Twente
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific 
 * language governing permissions and limitations under the License.
 *
 * $Id: GraphToPDF.java 5479 2014-07-19 12:20:13Z rensink $
 */
package groove.io.external.util;

import groove.gui.jgraph.JGraph;
import groove.io.external.PortException;
import groove.util.Version;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

/** Class offering the functionality to save a JGraph to PDF format. */
public class GraphToPDF extends GraphToVector {
    @Override
    public void renderGraph(JGraph<?> graph, File file)
        throws PortException {
        // Get graph bounds. If not available, do nothing (probably empty graph)
        Rectangle2D bounds = graph.getGraphBounds();
        if (bounds == null) {
            return;
        }
        Rectangle bound =
            new Rectangle((float) bounds.getWidth(), (float) bounds.getHeight());

        FileOutputStream fos = null;
        Document document = new Document(bound);
        PdfWriter writer = null;
        try {
            // Open file, create PDF document
            fos = new FileOutputStream(file);
            writer = PdfWriter.getInstance(document, fos);
        } catch (DocumentException e) {
            throw new PortException(e);
        } catch (FileNotFoundException e) {
            throw new PortException(e);
        }

        // Set some metadata
        document.addCreator(Version.getAbout());

        // Open document, get graphics
        document.open();
        PdfContentByte cb = writer.getDirectContent();
        boolean onlyShapes = true;
        //The embedded fonts most likely do not contain all necessary glyphs, so using outlines instead
        // onlyShapes makes PDF considerably bigger, but no alternative at the moment
        PdfGraphics2D pdf2d =
            new PdfGraphics2D(cb, (float) bounds.getWidth(),
                (float) bounds.getHeight(), new DefaultFontMapper(),
                onlyShapes, false, (float) 100.0);

        // Render
        toGraphics(graph, pdf2d);

        // Cleanup
        pdf2d.dispose();
        document.close();
        try {
            fos.close();
        } catch (IOException e) {
            throw new PortException(e);
        }
    }
}
