package ttaomae.connectn.gui;

import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import ttaomae.connectn.Board;
import ttaomae.connectn.BoardListener;

/**
 * A panel for displaying a Connect-N board. The Board being displayed can be
 * changed and the spacing of the pieces will be adjusted to fit evenly in the
 * panel.
 *
 * @author Todd Taomae
 */
public class BoardPanel extends GridPane implements BoardListener
{
    private Board board;
    private int pieceRadius;

    /**
     * Sets the gaps between spaces and padding on all edges of the board based
     * on the size of this panel and the size of the board.
     */
    private void setGapsAndPadding()
    {
        int panelWidth = (int)this.getPrefWidth();
        int panelHeight = (int)this.getPrefHeight();
        // the width and height measured in radii
        // 2*n radius for pieces + n+1 radius for gaps and padding
        int radiusWidths = 3 * this.board.getWidth() + 1;
        int radiusHeights = 3 * this.board.getHeight() + 1;

        this.pieceRadius = Math.min(panelWidth / radiusWidths, panelHeight / radiusHeights);

        // (size of the panel - total size of pieces) / (total number of gaps/padding)
        int totalPieceWidth = 2 * this.board.getWidth() * this.pieceRadius;
        int horizontalGap = (panelWidth - totalPieceWidth) / (this.board.getWidth() + 1);

        int totalPieceHeight = 2 * this.board.getHeight() * this.pieceRadius;
        int verticalGap = (panelHeight - totalPieceHeight) / (this.board.getHeight() + 1);

        int totalGapWidth = (this.board.getWidth() - 1) * horizontalGap;
        int horizontalPadding = (panelWidth - totalPieceWidth - totalGapWidth);
        int leftPadding = horizontalPadding / 2;
        // add padding % 2 to account for odd numbered padding
        int rightPadding = leftPadding + (horizontalPadding % 2);

        int totalGapHeight = (this.board.getHeight() - 1) * verticalGap;
        int verticalPadding = (panelHeight - totalPieceHeight - totalGapHeight);
        int bottomPadding = verticalPadding / 2;
        // add padding % 2 to account for odd numbered padding
        int topPadding = bottomPadding + (verticalPadding % 2);

        this.setHgap(horizontalGap);
        this.setVgap(verticalGap);
        this.setPadding(new Insets(topPadding, rightPadding, bottomPadding, leftPadding));
    }

    /**
     * Updates this BoardPanel each time the underlying board is changed.
     */
    @Override
    public void boardChanged()
    {
        this.update();
    }

    /**
     * Updates this panel to reflect the current state of the underlying Board.
     */
    public void update()
    {
        javafx.application.Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                BoardPanel.this.getChildren().clear();
                for (int y = 0; y < BoardPanel.this.board.getHeight(); y++) {
                    for (int x = 0; x < BoardPanel.this.board.getWidth(); x++) {
                        Circle circle;
                        int row = BoardPanel.this.board.getHeight() - y - 1;
                        switch (BoardPanel.this.board.getPieceAt(x, row)) {
                            case BLACK:
                                circle = new Circle(BoardPanel.this.pieceRadius, Color.BLACK);
                                break;
                            case RED:
                                circle = new Circle(BoardPanel.this.pieceRadius, Color.RED);
                                break;
                            default:
                                circle = new Circle(BoardPanel.this.pieceRadius, Color.WHITE);
                        }
                        BoardPanel.this.add(circle, x, y);
                    }
                }
            }
        });
    }

    /**
     * Returns the column of the underlying Board corresponding to a horizontal
     * position on this panel.
     *
     * @param x horizontal position on this panel
     * @return the column of the underlying Board corresponding to the
     *         horizontal position
     *
     */
    public int getBoardColumn(double x)
    {
        double halfGap = this.getHgap() / 2;
        double leftPadding = this.getPadding().getLeft() - halfGap;
        double rightPadding = this.getPadding().getRight() - halfGap;

        // outside of board
        if (x < (leftPadding) || x > (this.getWidth() - rightPadding)) {
            return -1;
        }

        double boardWidth = this.getWidth() - leftPadding - rightPadding;
        double pieceWidth = boardWidth / this.board.getWidth();

        double boardX = x - leftPadding;
        int column = (int) (boardX / pieceWidth);

        return column;
    }

    /**
     * Sets the underlying Board to a new Board.
     *
     * @param board the new Board
     */
    public void setBoard(Board board)
    {
        this.board = board;
        this.board.addBoardListener(this);
        this.setGapsAndPadding();
        this.update();
    }
}
