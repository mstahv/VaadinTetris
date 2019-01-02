package org.vaadin.example;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;


/**
 *
 * @author Matti Tahvonen
 */
public class About extends Div {

    public About() {
        setText("A server side Tetris game, WTF!? ");

        final Anchor gitHubPage = new Anchor("https://github.com/mstahv/VaadinTetris", "GitHub page");
        gitHubPage.setTarget("_blank");
        add(gitHubPage);
    }

}
