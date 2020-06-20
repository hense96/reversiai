package de.rwth.reversiai.visualization;

import de.rwth.reversiai.board.Board;
import de.rwth.reversiai.board.Direction;
import de.rwth.reversiai.board.iterator.BoardIterator;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Sub-view to render the visual representation of a board to the GUI.
 *
 * @author Marvin Pf&ouml;rtner
 */
class BoardVisualizationView extends JPanel
{
    /**
     * The game board that is currently being visualized
     */
    private Board board;

    /**
     * The off-screen buffer used for double-buffering.
     */
    private Image buffer;

    /**
     * The size of the off-screen buffer.
     */
    private Dimension bufferSize;

    /**
     * The graphics object used to draw to the off-screen buffer.
     */
    private Graphics2D bufferGraphics;

    /**
     * Mapping of player IDs to colors.
     */
    private Color[] playerColorMap = {
            Color.red,
            Color.blue,
            Color.green,
            Color.pink,
            Color.lightGray,
            Color.black,
            new Color( 102, 0, 102 ), // purple
            new Color( 102, 51, 0 ) // brown
    };

    /**
     * Mapping of tile outlets to colors.
     */
    private Color[] outletColorMap = {
            Color.red,
            Color.blue,
            Color.green,
            Color.pink,
            Color.cyan,
            Color.black,
            new Color( 102, 0, 102 ), // purple
            new Color( 102, 51, 0 ) // brown
    };

    /**
     * The side length of a tile in pixels.
     */
    private int tileSize;

    /**
     * The width of the map in pixels.
     */
    private int displayWidth;

    /**
     * The height of the map in pixels.
     */
    private int displayHeight;

    /**
     * The thickness of the tile borders in pixels.
     */
    private int borderSize = 1;

    /**
     * X coordinate of the (0,0) tile's top left corner
     */
    private int xMargin;

    /**
     * Y coordinate of the (0,0) tile's top left corner
     */
    private int yMargin;

    /**
     * Position of the tile whose transitions should be highlighted or the unassigned position if transition highlighting
     * is currently disabled.
     */
    private Position transitionHighlightOrigin = Position.unassigned;

    /**
     * Constructs a new {@link BoardVisualizationView} for the given board.
     *
     * @param board The board to visualize.
     */
    public BoardVisualizationView( Board board )
    {
        this.board = board;

        this.setSize( 600, 600 );

        this.setBackground( Color.white );
    }

    /**
     * Visualizes a new board on the screen.
     *
     * @param board The board to be visualized.
     */
    void updateBoard( Board board )
    {
        this.board = board;

        this.repaint();
    }

    /**
     * Calculates a game-internal position form pixel coordinates over the canvas.
     *
     * @param p The point in pixel coordinates over the canvas.
     * @return The corresponding game-internal tile position.
     */
    Position getMousePosition( Point p )
    {
        if ( p.x > this.displayWidth || p.y > this.displayHeight
             || p.x < this.xMargin || p.y < this.yMargin )
        {
            return Position.unassigned;
        }

        int posX = ( p.x - this.xMargin ) / ( this.tileSize + 1 );
        int posY = ( p.y - this.yMargin ) / ( this.tileSize + 1 );

        return new Position( posX, posY );
    }

    /**
     * Calculates the display height of a {@link String}.
     *
     * @param string The {@link String} whose display height should be calculated.
     * @return The display height of the {@link String}.
     */
    private int getStringHeight( String string )
    {
        FontRenderContext frc = this.bufferGraphics.getFontRenderContext();
        GlyphVector gv = this.bufferGraphics.getFont().createGlyphVector( frc, string );
        Rectangle2D bounds = gv.getVisualBounds();

        return (int) Math.round( bounds.getHeight() );
    }

    /**
     * Calculates the display width of a {@link String}.
     *
     * @param string The {@link String} whose display width should be calculated.
     * @return The display width of the {@link String}.
     */
    private int getStringWidth( String string )
    {
        return this.bufferGraphics.getFontMetrics().stringWidth( string );
    }

    /**
     * Returns the coordinates of the top left border corner of the tile at the given position.
     *
     * @param position The position of the tile.
     * @return The coordinates of the top left border corner of the tile at the given position.
     */
    private Point getPointFromMapPosition( Position position )
    {
        return this.getPointFromMapPosition( position, false );
    }

    /**
     * Returns the coordinates of the top left border corner of the tile at the given position if {@code inside} is false
     * and the coordinates of top left corner of the white tile rectangle if {@code inside} is true.
     *
     * @param position The position of the tile.
     * @return The coordinates of the top left border corner of the tile at the given position if {@code inside} is false
     * and the coordinates of top left corner of the white tile rectangle if {@code inside} is true.
     */
    private Point getPointFromMapPosition( Position position, boolean inside )
    {
        int drawX = this.xMargin + ( this.tileSize + this.borderSize ) * position.x;
        int drawY = this.yMargin + ( this.tileSize + this.borderSize ) * position.y;

        if ( inside )
        {
            drawX++;
            drawY++;
        }

        return new Point( drawX, drawY );
    }

    /**
     * Draws a tile at the given position with the given background color.
     *
     * @param position  The position to draw to.
     * @param fillColor The tile's background color.
     */
    private void drawTile( Position position, Color fillColor )
    {
        Point at = this.getPointFromMapPosition( position );

        bufferGraphics.setColor( fillColor );
        bufferGraphics.fillRect( at.x, at.y, this.tileSize + 1, this.tileSize + 1 );

        bufferGraphics.setColor( Color.black );
        bufferGraphics.drawRect( at.x, at.y, this.tileSize + 1, this.tileSize + 1 );
    }

    /**
     * Draws a stone of the given color onto the tile at the given position.
     *
     * @param position  The position to draw to.
     * @param fillColor The stone's background color.
     */
    private void drawStone( Position position, Color fillColor )
    {
        Point at = this.getPointFromMapPosition( position, true );

        bufferGraphics.setColor( fillColor );
        bufferGraphics.fillOval( at.x, at.y, this.tileSize - 1, this.tileSize - 1 );

        bufferGraphics.setColor( Color.black );
        bufferGraphics.drawOval( at.x, at.y, this.tileSize - 1, this.tileSize - 1 );
    }

    /**
     * Draws a label string in the center of the tile or stone at the given position.
     *
     * @param position The position to draw to.
     * @param string   The label to draw onto the tile or stone.
     * @param color    The text color.
     */
    private void drawTileCenteredLabel( Position position, String string, Color color )
    {
        int stringWidth = this.getStringWidth( string );
        int stringHeight = this.getStringHeight( string );

        Point at = this.getPointFromMapPosition( position, true );

        // String will be placed at their baseline
        at.y += stringHeight;

        // Add top and left margin to center the string
        at.x += ( this.tileSize - stringWidth ) / 2;
        at.y += ( this.tileSize - stringHeight ) / 2;

        bufferGraphics.setColor( color );
        bufferGraphics.drawString( string, at.x, at.y );

        bufferGraphics.setColor( color );
    }

    /**
     * Highlights the outgoing transitions of the tile at the given position.
     *
     * @param origin The position of the tile whose transitions will be highlighted.
     */
    void startHighlightingTransitions( Position origin )
    {
        this.transitionHighlightOrigin = origin;

        this.repaint();
    }

    /**
     * Draws the highlighting GUI elements to the canvas.
     */
    private void highlightTransitions()
    {
        if ( !this.board.hasPosition( this.transitionHighlightOrigin.x, this.transitionHighlightOrigin.y ) )
        {
            return;
        }

        BoardIterator iterator1 = this.board.getBoardIterator( this.transitionHighlightOrigin.x,
                                                               this.transitionHighlightOrigin.y );

        BoardIterator iterator2 = this.board.getBoardIterator();

        List< Position > highlightedTiles = new ArrayList<>( 9 );

        int i = 0;

        for ( Direction direction : Direction.values() )
        {
            iterator2.moveTo( this.transitionHighlightOrigin.x, this.transitionHighlightOrigin.y );

            if ( !iterator1.hasNext( direction ) )
            {
                continue;
            }

            iterator2.next( direction );

            Position target = new Position( iterator2.getX(), iterator2.getY() );

            if ( !highlightedTiles.contains( this.transitionHighlightOrigin ) )
            {
                this.highlightTransitionTile( this.transitionHighlightOrigin );

                highlightedTiles.add( this.transitionHighlightOrigin );
            }

            if ( !highlightedTiles.contains( target ) )
            {
                this.highlightTransitionTile( target );

                highlightedTiles.add( target );
            }

            this.highlightTileOutlet( this.transitionHighlightOrigin, direction, this.outletColorMap[ i ] );
            this.highlightTileOutlet( target, iterator1.peekNeighborIncomingDirection( direction ),
                                      this.outletColorMap[ i ] );

            i++;
        }
    }

    /**
     * Stops highlighting transitions.
     */
    void stopHighlightingTransitions()
    {
        this.transitionHighlightOrigin = Position.unassigned;

        this.repaint();
    }

    /**
     * Draws the GUI background of a highlighted tile onto the canvas.
     *
     * @param position The position of the tile to highlight.
     */
    private void highlightTransitionTile( Position position )
    {
        Point offset = this.getPointFromMapPosition( position, true );

        this.drawTile( position, Color.white );

        bufferGraphics.setColor( Color.black );

        bufferGraphics.drawLine(
                offset.x,
                offset.y,
                offset.x + this.tileSize,
                offset.y + this.tileSize
        );

        bufferGraphics.drawLine(
                offset.x + this.tileSize,
                offset.y,
                offset.x,
                offset.y + this.tileSize
        );

        bufferGraphics.drawLine(
                offset.x + this.tileSize / 2,
                offset.y,
                offset.x + this.tileSize / 2,
                offset.y + this.tileSize
        );

        bufferGraphics.drawLine(
                offset.x,
                offset.y + this.tileSize / 2,
                offset.x + this.tileSize,
                offset.y + this.tileSize / 2
        );
    }

    /**
     * Draws a highlighted tile outlet.
     *
     * @param position  The position of the tile the outlet belongs to.
     * @param direction The direction of the outlet.
     * @param color     The color of the outlet.
     */
    private void highlightTileOutlet( Position position, Direction direction, Color color )
    {
        Point offset = this.getPointFromMapPosition( position, true );

        bufferGraphics.setColor( color );

        switch ( direction )
        {
            case NORTH:
                bufferGraphics.fillRect(
                        offset.x + tileSize / 2 - tileSize / 8,
                        offset.y,
                        tileSize / 4,
                        tileSize / 4
                );
                break;

            case NORTHEAST:
                bufferGraphics.fillRect(
                        offset.x + tileSize - tileSize / 4,
                        offset.y,
                        tileSize / 4,
                        tileSize / 4
                );
                break;

            case EAST:
                bufferGraphics.fillRect(
                        offset.x + tileSize - tileSize / 4,
                        offset.y + tileSize / 2 - tileSize / 8,
                        tileSize / 4,
                        tileSize / 4
                );
                break;

            case SOUTHEAST:
                bufferGraphics.fillRect(
                        offset.x + tileSize - tileSize / 4,
                        offset.y + tileSize - tileSize / 4,
                        tileSize / 4,
                        tileSize / 4
                );
                break;

            case SOUTH:
                bufferGraphics.fillRect(
                        offset.x + tileSize / 2 - tileSize / 8,
                        offset.y + tileSize - tileSize / 4,
                        tileSize / 4,
                        tileSize / 4
                );
                break;

            case SOUTHWEST:
                bufferGraphics.fillRect(
                        offset.x,
                        offset.y + tileSize - tileSize / 4,
                        tileSize / 4,
                        tileSize / 4
                );
                break;

            case WEST:
                bufferGraphics.fillRect(
                        offset.x,
                        offset.y + tileSize / 2 - tileSize / 8,
                        tileSize / 4,
                        tileSize / 4
                );
                break;

            case NORTHWEST:
                bufferGraphics.fillRect(
                        offset.x,
                        offset.y,
                        tileSize / 4,
                        tileSize / 4
                );
                break;
        }

        bufferGraphics.setColor( Color.black );
    }

    /**
     * Repaints the buffer and projects it into the GUI.
     *
     * @param g Draw graphics of the on-screen canvas.
     */
    @Override
    public void paint( Graphics g )
    {
        if ( bufferSize != getSize() || buffer == null || bufferGraphics == null )
        {
            resetBuffer();
        }

        bufferGraphics.clearRect( 0, 0, bufferSize.width, bufferSize.height );

        paintBuffer();

        g.drawImage( buffer, 0, 0, this );
    }

    /**
     * Paints the off-screen buffer.
     */
    private void paintBuffer()
    {
        int mapWidth = board.getWidth();
        int mapHeight = board.getHeight();

        int tileWidth = ( this.getWidth() - ( mapWidth + 1 ) * this.borderSize ) / ( mapWidth + 2 );
        int tileHeight = ( this.getHeight() - ( mapHeight + 1 ) * this.borderSize ) / ( mapHeight + 2 );

        this.tileSize = ( tileHeight < tileWidth ) ? tileHeight : tileWidth;

        this.xMargin = this.tileSize + this.borderSize;
        this.yMargin = this.tileSize + this.borderSize;

        this.displayWidth = this.xMargin + mapWidth * ( this.tileSize + this.borderSize );
        this.displayHeight = this.yMargin + mapHeight * ( this.tileSize + this.borderSize );

        /*
         * Fit label font into tiles (50 will be the widest label)
         */
        int fontSize = 20;

        Font font = new Font( Font.SANS_SERIF, Font.PLAIN, fontSize );

        this.bufferGraphics.setFont( font );

        while ( this.getStringWidth( "50" ) > this.tileSize || this.getStringHeight( "50" ) > this.tileSize )
        {
            fontSize--;

            font = font.deriveFont( (float) fontSize );

            this.bufferGraphics.setFont( font );
        }

        this.bufferGraphics.setColor( Color.black );

        /*
         * Draw coordinate axes
         */
        for ( int x = 0; x < mapWidth; x++ )
        {
            this.drawTileCenteredLabel( new Position( x, -1 ), String.valueOf( x ), Color.black );
            this.drawTileCenteredLabel( new Position( x, mapHeight ), String.valueOf( x ), Color.black );
        }

        for ( int y = 0; y < mapHeight; y++ )
        {
            this.drawTileCenteredLabel( new Position( -1, y ), String.valueOf( y ), Color.black );
            this.drawTileCenteredLabel( new Position( mapWidth, y ), String.valueOf( y ), Color.black );
        }

        /*
         * Draw the board
         */
        BoardIterator iterator = board.getBoardIterator();

        for ( int y = 0; y < mapHeight; y++ )
        {
            for ( int x = 0; x < mapWidth; x++ )
            {
                Position currPos = new Position( x, y );

                iterator.moveTo( x, y );

                switch ( iterator.getTileType() )
                {
                    case ABSENT:
                        this.drawTile( currPos, Color.darkGray );
                        break;

                    case STANDARD:
                        this.drawTile( currPos, Color.white );
                        break;

                    case CHOICE:
                        this.drawTile( currPos, Color.magenta );
                        this.drawTileCenteredLabel( currPos, "C", Color.black );
                        break;

                    case INVERSION:
                        this.drawTile( currPos, Color.yellow );
                        this.drawTileCenteredLabel( currPos, "I", Color.black );
                        break;

                    case BONUS:
                        this.drawTile( currPos, Color.cyan );
                        this.drawTileCenteredLabel( currPos, "B", Color.black );
                        break;
                }

                if ( iterator.isOccupied() && !iterator.isOccupiedByExpansionStone() )
                {
                    this.drawStone( currPos, playerColorMap[ iterator.getOccupant() - 1 ] );
                    this.drawTileCenteredLabel( currPos, String.valueOf( iterator.getOccupant() ), Color.white );
                }
                else if ( iterator.isOccupiedByExpansionStone() )
                {
                    this.drawStone( currPos, Color.white );
                    this.drawTileCenteredLabel( currPos, "E", Color.black );
                }
            }
        }

        this.highlightTransitions();
    }

    /**
     * Resets the off-screen buffer.
     */
    private void resetBuffer()
    {
        this.bufferSize = this.getSize();

        if ( this.bufferGraphics != null )
        {
            this.bufferGraphics.dispose();
            this.bufferGraphics = null;
        }

        if ( this.buffer != null )
        {
            this.buffer.flush();
            this.buffer = null;
        }

        System.gc();

        this.buffer = createImage( this.bufferSize.width, this.bufferSize.height );
        this.bufferGraphics = (Graphics2D) buffer.getGraphics();

        this.bufferGraphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
    }
}
