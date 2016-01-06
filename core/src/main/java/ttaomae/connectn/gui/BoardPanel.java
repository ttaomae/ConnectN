package ttaomae.connectn.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import ttaomae.connectn.Board;
import ttaomae.connectn.BoardListener;

/**
 * A panel for displaying a Connect-N board. The Board being displayed can be
 * changed and the spacing of the pieces will be adjusted to fit evenly in the
 * panel.
 *
 * @author Todd Taomae
 */
public class BoardPanel extends Canvas implements BoardListener
{
    private Board board;
    private double pieceRadius;
    private double horizontalGap;
    private double verticalGap;

    /**
     * Sets the gaps between spaces and padding on all edges of the board based
     * on the size of this panel and the size of the board.
     */
    private void setGapsAndPadding()
    {
        double panelWidth = this.getWidth(); //this.getPrefWidth();
        double panelHeight = this.getHeight(); //this.getPrefHeight();
        // the width and height measured in radii
        // 2*n radius for pieces + n+1 radius for gaps and padding
        int radiusWidths = 3 * this.board.getWidth() + 1;
        int radiusHeights = 3 * this.board.getHeight() + 1;

        // compute radius needed to fit pieces both horizontally and vertically
        // choose the smaller of the two to guarantee that the pieces will fit
        this.pieceRadius = Math.min(panelWidth / radiusWidths, panelHeight / radiusHeights);

        double totalPieceWidth = 2 * this.board.getWidth() * this.pieceRadius;
        double totalPieceHeight = 2 * this.board.getHeight() * this.pieceRadius;

        horizontalGap = (panelWidth - totalPieceWidth) / (this.board.getWidth() + 1);
        verticalGap = (panelHeight - totalPieceHeight) / (this.board.getHeight() + 1);
    }

    /**
     * Updates this BoardPanel each time the underlying board is changed.
     */
    @Override
    public void boardChanged()
    {
        javafx.application.Platform.runLater(() -> this.update());
    }

    /**
     * Updates this panel to reflect the current state of the underlying Board.
     */
    public void update()
    {
        GraphicsContext graphics = this.getGraphicsContext2D();
        graphics.setFill(Color.web("0x336699"));
        graphics.fillRect(0, 0, this.getWidth(), this.getHeight());

        for (int row = 0; row < this.board.getHeight(); row++) {
            for (int col = 0; col < this.board.getWidth(); col++) {
                int invertedRow = this.board.getHeight() - row - 1;
                // set color
                switch (BoardPanel.this.board.getPieceAt(col, row)) {
                    case BLACK:
                        graphics.setFill(Color.BLACK);
                        break;
                    case RED:
                        graphics.setFill(Color.RED);
                        break;
                    default:
                        graphics.setFill(Color.WHITE);
                        break;
                }

                double diameter = this.pieceRadius * 2;
                double x = horizontalGap + (col * diameter) + (col * horizontalGap);
                double y = verticalGap + (invertedRow * diameter) + (invertedRow * verticalGap);

                graphics.fillOval(x, y, diameter, diameter);
            }
        }
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
        double halfGap = this.horizontalGap / 2;
        double leftPadding = this.horizontalGap - halfGap;
        double rightPadding = this.horizontalGap - halfGap;

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
