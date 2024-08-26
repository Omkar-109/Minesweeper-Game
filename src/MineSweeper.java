import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.*;

public class MineSweeper implements MineFunctions {

    private class MineTile extends JButton {
        int r, c;

        public MineTile(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    int titleSize = 60;
    int numRows = 4;
    int numCols = numRows;
    int mineCount = 5;
    boolean gameOver = false;

    JFrame frame = new JFrame("MineSweeper");
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();
    JComboBox<String> levelSelector = new JComboBox<>(new String[]{"Level 1 (4x4)", "Level 2 (8x8)", "Level 3 (10x10)"});
    JButton resetButton = new JButton("Reset");

    MineTile[][] board;
    ArrayList<MineTile> mineList;
    Random random = new Random();
    int tilesClicked = 0;

    // Load and scale images
    ImageIcon bombIcon;
    ImageIcon flagIcon;

    MineSweeper() {
        loadImages();
        initializeFrame();
        initializeBoard();
        setMines();
        frame.setVisible(true);
    }
//for images
    private void loadImages() {
        try {
            // Load images
            BufferedImage bombImage = ImageIO.read(getClass().getResource("/images/bombimg.png"));
            BufferedImage flagImage = ImageIO.read(getClass().getResource("/images/flagimg.png"));

            // Scale images to fit the button size
            bombIcon = new ImageIcon(bombImage.getScaledInstance(titleSize-10, titleSize-10, Image.SCALE_SMOOTH));
            flagIcon = new ImageIcon(flagImage.getScaledInstance(titleSize-10, titleSize-10, Image.SCALE_SMOOTH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeFrame() {
        frame.setSize(600, 600);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        textLabel.setFont(new Font("Arial", Font.BOLD, 16));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Minesweeper: " + mineCount);

        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(textLabel);
        textPanel.add(levelSelector);
        textPanel.add(resetButton);

        frame.add(textPanel, BorderLayout.NORTH);

        resetButton.addActionListener(e -> resetGame());
        levelSelector.addActionListener(e -> changeLevel());

        boardPanel.setLayout(new GridLayout(numRows, numCols));
        frame.add(boardPanel);
    }

    private void initializeBoard() {
        boardPanel.removeAll();
        boardPanel.setLayout(new GridLayout(numRows, numCols));
        board = new MineTile[numRows][numCols];

        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                MineTile tile = new MineTile(r, c);
                board[r][c] = tile;
                tile.setFocusable(false);
                tile.setMargin(new Insets(0, 0, 0, 0));
                tile.setFont(new Font("Arial Unicode MS", Font.PLAIN, titleSize / 3));
                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (gameOver) {
                            return;
                        }

                        MineTile tile = (MineSweeper.MineTile) e.getSource();

                        if (e.getButton() == MouseEvent.BUTTON1) {
                        	//if (tile.getText().isEmpty()) {
                            if (tile.getIcon() == null) {
                                if (mineList.contains(tile)) {
                                    revealMines();
                                } else {
                                    checkMine(tile.r, tile.c);
                                }
                            }
                        } else if (e.getButton() == MouseEvent.BUTTON3) {
                            
                        	//emoji
//                        	if (tile.getText().isEmpty() && tile.isEnabled()) {
//                                tile.setText("🚩");
//                            } else if (tile.getText().equals("🚩")) {
//                                tile.setText("");
                        	
                        	if (tile.getIcon() == null && tile.isEnabled()) {
                                tile.setIcon(flagIcon);
                            } else if (tile.getIcon() == flagIcon) {
                                tile.setIcon(null);
                            }
                        }
                    }
                });
                boardPanel.add(tile);
            }
        }

        boardPanel.revalidate();
        boardPanel.repaint();
    }

    public void setMines() {
        mineList = new ArrayList<>();
        int mineLeft = mineCount;
        while (mineLeft > 0) {
            int r = random.nextInt(numRows);
            int c = random.nextInt(numCols);

            MineTile tile = board[r][c];
            if (!mineList.contains(tile)) {
                mineList.add(tile);
                mineLeft--;
            }
        }
    }

    public void revealMines() {
        for (MineTile tile : mineList) {
        	//tile.setText("💣");
            tile.setIcon(bombIcon);
        }

        gameOver = true;
        textLabel.setText("Game Over!");
    }

    public void checkMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) {
            return;
        }

        MineTile tile = board[r][c];
        if (!tile.isEnabled()) {
            return;
        }

        tile.setEnabled(false);
        tilesClicked++;

        int minesFound = 0;

        // Check surrounding tiles for mines
        minesFound += countMine(r - 1, c - 1); // Top left
        minesFound += countMine(r - 1, c);     // Top
        minesFound += countMine(r - 1, c + 1); // Top right
        minesFound += countMine(r, c - 1);     // Left
        minesFound += countMine(r, c + 1);     // Right
        minesFound += countMine(r + 1, c - 1); // Bottom left
        minesFound += countMine(r + 1, c);     // Bottom
        minesFound += countMine(r + 1, c + 1); // Bottom right

        if (minesFound > 0) {
            tile.setText(Integer.toString(minesFound));
        } else {
            tile.setText("");

            // Recursively check adjacent tiles
            checkMine(r - 1, c - 1); // Top left
            checkMine(r - 1, c);     // Top
            checkMine(r - 1, c + 1); // Top right
            checkMine(r, c - 1);     // Left
            checkMine(r, c + 1);     // Right
            checkMine(r + 1, c - 1); // Bottom left
            checkMine(r + 1, c);     // Bottom
            checkMine(r + 1, c + 1); // Bottom right
        }

        if (tilesClicked == numRows * numCols - mineList.size()) {
            gameOver = true;
            textLabel.setText("Mines Cleared! You Won!");
        }
    }

    public int countMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) {
            return 0;
        }
        return mineList.contains(board[r][c]) ? 1 : 0;
    }

    private void resetGame() {
        gameOver = false;
        tilesClicked = 0;
        textLabel.setText("Minesweeper: " + mineCount);
        initializeBoard();
        setMines();
    }

    private void changeLevel() {
        String selectedLevel = (String) levelSelector.getSelectedItem();
        switch (selectedLevel) {
            case "Level 1 (4x4)":
                numRows = 4;
                numCols = 4;
                mineCount = 5;
                break;
            case "Level 2 (8x8)":
                numRows = 8;
                numCols = 8;
                mineCount = 10;
                break;
            case "Level 3 (10x10)":
                numRows = 10;
                numCols = 10;
                mineCount = 20;
                break;
        }
        frame.setSize(numCols * titleSize, numRows * titleSize + 100);
        resetGame();
    }
}
