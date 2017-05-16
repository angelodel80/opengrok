/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * See LICENSE.txt included in this distribution for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright (c) 2005, 2017, Oracle and/or its affiliates. All rights reserved.
 */
package org.opensolaris.opengrok.analysis.document;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.opensolaris.opengrok.analysis.Definitions;
import org.opensolaris.opengrok.analysis.FileAnalyzer;
import org.opensolaris.opengrok.analysis.FileAnalyzerFactory;
import org.opensolaris.opengrok.analysis.StreamSource;
import org.opensolaris.opengrok.analysis.TextAnalyzer;
import org.opensolaris.opengrok.configuration.Project;
import org.opensolaris.opengrok.history.Annotation;
import org.opensolaris.opengrok.search.QueryBuilder;

/**
 * Analyzes [tn]roff files Created on September 30, 2005
 *
 * @author Chandan
 */
public class TroffAnalyzer extends TextAnalyzer {

    private TroffXref xref;

    /**
     * Creates a new instance of TroffAnalyzer
     * @param factory name
     */
    protected TroffAnalyzer(FileAnalyzerFactory factory) {
        super(factory);
        SymbolTokenizer = new TroffFullTokenizer(FileAnalyzer.dummyReader);
    }    
    
    @Override
    public void analyze(Document doc, StreamSource src, Writer xrefOut) throws IOException {        
        //this is to explicitly use appropriate analyzers tokenstream to workaround #1376 symbols search works like full text search 
        this.SymbolTokenizer.setReader(getReader(src.getStream()));
        TextField full=new TextField(QueryBuilder.FULL,SymbolTokenizer);        
        doc.add(full);

        if (xrefOut != null) {
            try (Reader in = getReader(src.getStream())) {
                writeXref(in, xrefOut);
            }
        }
    }

    /**
     * Write a cross referenced HTML file.
     *
     * @param in Input source
     * @param out Writer to write HTML cross-reference
     */
    private void writeXref(Reader in, Writer out) throws IOException {
        if (xref == null) {
            xref = new TroffXref(in);
        } else {
            xref.reInit(in);
        }
        xref.project = project;
        out.write("</pre><div id=\"man\">");
        xref.write(out);
        out.write("</div><pre>");
    }

    /**
     * Write a cross referenced HTML file reads the source from in
     *
     * @param in Input source
     * @param out Output xref writer
     * @param defs definitions for the file (could be null)
     * @param annotation annotation for the file (could be null)
     */
    static void writeXref(Reader in, Writer out, Definitions defs, Annotation annotation, Project project) throws IOException {
        TroffXref xref = new TroffXref(in);
        xref.project = project;
        xref.setDefs(defs);
        out.write("</pre><div id=\"man\">");
        xref.write(out);
        out.write("</div><pre>");
    }
}
