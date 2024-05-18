package org.martincorp.Model;

import javafx.scene.paint.Color;

public enum Online {
    ONLINE(Color.GREEN), OFFLINE(Color.RED);

    private final Color COLOR;

    private Online(Color COLOR) {
        this.COLOR = COLOR;
    }

    public Color getColor() {
        return COLOR;
    }
}
