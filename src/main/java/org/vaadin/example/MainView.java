package org.vaadin.example;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.shared.ui.Transport;
import org.vaadin.firitin.layouts.BorderLayout;
import org.vaadin.marcus.shortcut.Shortcut;
import org.vaadin.pekkam.Canvas;

import java.util.Arrays;

// The most efficient transport system
@Push(transport = Transport.WEBSOCKET)
@Route
@PWA(name = "Vaadin Tetris", shortName = "Tetris", themeColor = "lightblue")
@Viewport("width=device-width, minimum-scale=1, initial-scale=1, user-scalable=no, minimal-ui")
@HtmlImport("frontend://custom-style.html")
public class MainView extends VerticalLayout {

    private static final int PAUSE_TIME_MS = 500;

    private static final long serialVersionUID = -152735180021558969L;


    // Playfield width in tiles
    private static final int PLAYFIELD_W = 10;

    // Playfield height in tiles
    private static final int PLAYFIELD_H = 20;

    // Playfield background color
    private static final String PLAYFIELD_COLOR = "#000";

    // Tile size in pixels
    private int tileSize = 30;

    private Canvas canvas;
    private boolean running;
    private Game game;

    private Span scoreLabel;

    public MainView() {
        add(new About());

        // Button for moving left
        final Button leftBtn = new Button(VaadinIcon.ARROW_LEFT.create());
        leftBtn.addClickListener(e -> {
            game.moveLeft();
            drawGameState();
        });
        Shortcut.add(this, Key.ARROW_LEFT, leftBtn::click);

        // Button for moving right
        final Button rightBtn = new Button(VaadinIcon.ARROW_RIGHT.create());
        rightBtn.addClickListener(e -> {
            game.moveRight();
            drawGameState();
        });
        Shortcut.add(this, Key.ARROW_RIGHT, rightBtn::click);

        // Button for rotating clockwise
        final Button rotateCWBtn = new Button(VaadinIcon.ROTATE_RIGHT.create());
        rotateCWBtn.addClickListener(e -> {
            game.rotateCW();
            drawGameState();
        });
        Shortcut.add(this, Key.ARROW_DOWN, rotateCWBtn::click);

        // Button for rotating counter clockwise
        final Button rotateCCWBtn = new Button(VaadinIcon.ARROW_UP.create());
        rotateCCWBtn.addClickListener(e -> {
            game.rotateCCW();
            drawGameState();
        });
        Shortcut.add(this, Key.ARROW_UP, rotateCCWBtn::click);

        // Button for dropping the piece
        final Button dropBtn = new Button(VaadinIcon.ARROW_DOWN.create());

        dropBtn.addClickListener(e -> {
            game.drop();
            drawGameState();
        });
        Shortcut.add(this, Key.of("d"), dropBtn::click);

        // Button for restarting the game
        final Button restartBtn = new Button(VaadinIcon.PLAY.create());
        restartBtn.addClickListener(e -> {
            running = !running;
            if (running) {
                game = new Game(10, 20);
                startGameThread();
                restartBtn.setIcon(VaadinIcon.STOP.create());
                dropBtn.focus();
            } else {
                restartBtn.setIcon(VaadinIcon.PLAY.create());
                gameOver();
            }
        });

        // Canvas for the game
        canvas = new Canvas(tileSize * PLAYFIELD_W, tileSize * PLAYFIELD_H);
        add(canvas);

        // Label for score
        scoreLabel = new Span("Score: 0");
        add(scoreLabel);

        styleControlButtons(restartBtn, leftBtn, rightBtn, rotateCCWBtn, rotateCWBtn, dropBtn);
        rotateCWBtn.addClassName("dominant-control");
        restartBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        final BorderLayout cursorsPanel = new BorderLayout();
        cursorsPanel.setChildAt(BorderLayout.Region.NORTH, rotateCCWBtn);
        cursorsPanel.setChildAt(BorderLayout.Region.SOUTH, dropBtn);
        cursorsPanel.setChildAt(BorderLayout.Region.EAST, rightBtn);
        cursorsPanel.setChildAt(BorderLayout.Region.WEST, leftBtn);

        final HorizontalLayout controlsPanel = new HorizontalLayout(cursorsPanel, restartBtn, rotateCWBtn);
        controlsPanel.setAlignItems(Alignment.CENTER);
        controlsPanel.setSpacing(true);
        controlsPanel.setJustifyContentMode(JustifyContentMode.BETWEEN);
        controlsPanel.setWidth(canvas.getWidth());

        add(controlsPanel);

        setAlignItems(Alignment.CENTER);
        getStyle().set("overflow", "hidden");
    }

    @Override
    public void onAttach(AttachEvent event) {
        UI.getCurrent().getPage().executeJavaScript(
                        "const viewPortHeight = window.innerHeight;" +
                        "const aboutTextHeight = document.querySelectorAll('div')[1].offsetHeight;" +
                        "const scoreLabelHeight = document.querySelector('span').offsetHeight;" +
                        "const controlsPanelHeight = document.querySelector('vaadin-horizontal-layout').offsetHeight;" +
                        "const befittingCanvasHeight = viewPortHeight - aboutTextHeight - scoreLabelHeight - controlsPanelHeight - 60;" +
                        "$0.$server.updateCanvasHeight(befittingCanvasHeight);", this);
    }

    @ClientCallable
    public void updateCanvasHeight(int befittingCanvasHeight) {
        int currentCanvasHeight = tileSize * PLAYFIELD_H;
        double scalingFactor = (double) (befittingCanvasHeight) / currentCanvasHeight;
        tileSize *= scalingFactor;
        remove(canvas);
        canvas = new Canvas(tileSize * PLAYFIELD_W, tileSize * PLAYFIELD_H);
        addComponentAtIndex(1, canvas);
    }

    private void styleControlButtons(Button... buttons) {
        Arrays.stream(buttons).forEach(button -> button.addClassName("control-button"));
    }

    /**
     * Start the game thread that updates the game periodically.
     *
     */
    protected synchronized void startGameThread() {
        UI ui = UI.getCurrent();
        Thread t = new Thread(() -> {

            // Continue until stopped or game is over
            while (running && !game.isOver()) {

                // Draw the state
                ui.access(this::drawGameState);

                // Pause for a while
                try {
                    Thread.sleep(PAUSE_TIME_MS);
                } catch (InterruptedException ignored) {
                }

                // Step the game forward and update score
                game.step();
                ui.access(this::updateScore);

            }

            // Notify user that game is over
            ui.access(this::notifyGameOver);
        });
        t.start();
    }

    /**
     * Update the score display.
     *
     */
    protected synchronized void updateScore() {
        scoreLabel.setText("Score: " + game.getScore());
    }

    protected synchronized void notifyGameOver() {
        Notification.show("Game Over! Your score: " + game.getScore());
    }

    /**
     * Quit the game.
     *
     */
    protected synchronized void gameOver() {
        running = false;
    }

    /**
     * Draw the current game state.
     *
     */
    protected synchronized void drawGameState() {

        // Reset and clear canvas
        canvas.getContext().clearRect(0, 0, tileSize * PLAYFIELD_W, tileSize * PLAYFIELD_H);
        canvas.getContext().setFillStyle(PLAYFIELD_COLOR);

        canvas.getContext().fillRect(0, 0, game.getWidth() * tileSize + 2, game.getHeight()
                * tileSize + 2);

        // Draw the tetrominoes
        Grid state = game.getCurrentState();
        for (int x = 0; x < state.getWidth(); x++) {
            for (int y = 0; y < state.getHeight(); y++) {

                int tile = state.get(x, y);
                if (tile > 0) {

                    String color = Tetromino.get(tile).getColor();
                    canvas.getContext().setFillStyle(color);
                    canvas.getContext().fillRect(x * tileSize + 1, y * tileSize + 1,
                            tileSize - 2, tileSize - 2);
                }

            }
        }
    }
}
