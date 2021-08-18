package org.vaadin.example;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;


/**
 *
 * @author Matti Tahvonen
 */
public class About extends Div {

    public About() {
        add(new H1("A server side Tetris game, but why!? "));
        add(new Paragraph("This application is a fun way to prove that \"server-side architecture\" doesn't necessarily cause you huge latency issues."));
        final Anchor gitHubPage = new Anchor("https://vaadin.com/blog/server-side-tetris-with-vaadin-flow", "Read more from the related blog post...");
        gitHubPage.setTarget("_blank");
        add(gitHubPage);
    }

}
