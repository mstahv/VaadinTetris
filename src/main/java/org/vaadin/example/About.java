package org.vaadin.example;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;


/**
 *
 * @author Matti Tahvonen
 */
public class About extends Div {

    public About() {
        add(new H1("A server side Tetris game, WTF!? "));
        final Anchor gitHubPage = new Anchor("https://github.com/mstahv/VaadinTetris", "See GitHub page for more details");
        gitHubPage.setTarget("_blank");
        add(gitHubPage);
    }

}
