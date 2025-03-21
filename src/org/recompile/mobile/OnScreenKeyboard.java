package org.recompile.mobile;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class OnScreenKeyboard {
    private static OnScreenKeyboard instance;
    private int lcdWidth;
    private int lcdHeight;
    private boolean enabled = false;
    
    T9Key[][] t9NormalKeys = null;
    T9Key[][] t9SymbolKeys = null;

    private int currentKeyPositionX = 0;
    private int currentKeyPositionY = 0;
    private String currentText = "";
    private int mode = 0;
    private boolean isUpperCase = false;

    private OnScreenKeyboard() 
    {
        lcdWidth = 320;
        lcdHeight = 240;
        initT9KeyBoard();
    }

    public void onCommitCharacter(String character) {

        String characterToAppend = isUpperCase ? character.toUpperCase() : character.toLowerCase();
        this.currentText += characterToAppend;

        this.sendUserInputStringToPlatformKeyboard();
    }

    public static synchronized OnScreenKeyboard getInstance() {
        if (instance == null) {
            instance = new OnScreenKeyboard();
        }
        return instance;
    }

    public void enable() 
    {
        enabled = true;
        currentText = "";
        currentKeyPositionX = 0;
        currentKeyPositionY = 0;
        getCurrentFocusedKey().onFocus();
    }

    public void disable() 
    {
        enabled = false;
        getCurrentFocusedKey().onLeave();
    }

    public boolean isShown() 
    {
        return enabled;
    }

    public void onInput(int keyCode, int action) {
        if (action == PlatformKeyboard.KEYCODE_DOWN) {
            switch(keyCode)
            {
                case 0: 

                // Up
                onKeyboardMoveUp();
                break;

                case 1: 

                // Down
                onKeynoardMoveDown();
                break;

                case 2: 

                // Left
                onKeyboardMoveLeft();
                break;

                case 3: 

                // Right
                onKeyboardMoveRight();

                break;

                case 7:

                // OK
                getCurrentFocusedKey().onKeyPress();
                
                break;

                case 8: 

                // Start
                changeCase();
                break ; 

                case 9:

                // Select
                changeMode();
                break;

                case 6:

                // 0
                backspace();
                break;
            }
        }
    }

    public void drawOnScreenGraphics(Graphics2D g2d) 
    {
        if (!enabled) {
            return;
        }

        showT9KeyboardLayout(g2d);
    }

    private T9Key[][] getCurrentT9Keys() {
        if (mode == 0) {
            return t9NormalKeys;
        } else {
            return t9SymbolKeys;
        }
    }

    private String[] getKeyDisplayNames() {
        if (mode == 0) {
            if (isUpperCase) {
                String[] t9KeyTexts = {"_ 0,1", "2 ABC", "3 DEF", "4 GHI", "5 JKL", "6 MNO", "7 PQRS", "8 TUV", "9 WXYZ"};
                return t9KeyTexts;
            } else {
                String[] t9KeyTexts = {"_ 0,1", "2 abc", "3 def", "4 ghi", "5 jkl", "6 mno", "7 pqrs", "8 TUV", "9 wxyz"};
                return t9KeyTexts;
            }
        } else {
            String[] t9Symbols = {". ,", "! $", "@ &", "-+", "*", "_", "#", "=", "/"};
            return t9Symbols;
        }
    }

    private void changeMode() {
        getCurrentFocusedKey().onLeave();
        mode = mode == 0 ? 1 : 0;
        currentKeyPositionX = 0;
        currentKeyPositionY = 0;
        getCurrentFocusedKey().onFocus();
    }

    private void changeCase() {
        isUpperCase = !isUpperCase;
    }

    private void clearText() {
        currentText = "";
    }

    private void backspace() {
        if (currentText.length() > 0) {
            currentText = currentText.substring(0, currentText.length() - 1);
        }
    }

    private void showT9KeyboardLayout(Graphics2D g2d) 
    {
        int buttonSize = 40;
        int padding = 8;
        int borderSize = 2;
        int startX = (lcdWidth - (3 * buttonSize + 2 * padding)) / 2;
        int startY = lcdHeight - (3 * buttonSize + 2 * padding) - 20;

        g2d.setColor(new Color(0, 0, 0, 128)); // 128 is the alpha value for 50% opacity
        g2d.fillRect(0, 0, lcdWidth, lcdHeight);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 10));

        g2d.setColor(Color.WHITE);
        g2d.fillRect(40, 50 , lcdWidth - 80, 20);

        g2d.setColor(Color.BLACK);
        g2d.drawRect(40, 50, lcdWidth - 80, 20);

        g2d.setColor(Color.BLACK);
        int textX = 40 + 5;
        int textY = 50 + 20 / 2 + g2d.getFontMetrics().getAscent() / 2;

        String uncommitCharacter = getCurrentFocusedKey().getUncommitCharacter(isUpperCase);
        String displayText = currentText + uncommitCharacter;
        if (displayText.length() > 38) {
            displayText = displayText.substring(displayText.length() - 38);
        }

        long currentTime = System.currentTimeMillis();
        boolean showCursor = (currentTime / 500) % 2 == 0;
        if (showCursor && uncommitCharacter.length() == 0) {
            displayText += "_";
        }

        g2d.drawString(displayText, textX, textY);
        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, 8));

        T9Key[][] t9Keys = getCurrentT9Keys();
        
        for (int row = 0; row < 3; row++) 
        {
            for (int col = 0; col < 3; col++) 
            {
                int x = startX + col * (buttonSize + padding);
                int y = startY + row * (buttonSize + padding);

                if (t9Keys[row][col].isFocused()) {
                    g2d.setColor(Color.RED);
                } else {
                    g2d.setColor(Color.BLACK);
                }
                g2d.fillRect(x - borderSize, y - borderSize, buttonSize + 2 * borderSize, buttonSize + 2 * borderSize);

                g2d.setColor(Color.LIGHT_GRAY);
                g2d.fillRect(x, y, buttonSize, buttonSize);

                g2d.setColor(Color.BLACK);
                g2d.drawString(getKeyDisplayNames()[row * 3 + col], x + buttonSize / 2 - 12, y + buttonSize / 2 + 5);
            }
        }

        g2d.dispose();
    }

    public void sendUserInputStringToPlatformKeyboard() {
        PlatformKeyboard.getInstance().onOnScreenKeyboardStringInput(currentText);
    }

    private void initT9KeyBoard() {

        t9NormalKeys = new T9Key[3][3];
        t9NormalKeys[0][0] = new T9Key(1, new String[]{" ", "0", "1"}, this);
        t9NormalKeys[0][1] = new T9Key(2, new String[]{"A", "B", "C", "2"}, this);
        t9NormalKeys[0][2] = new T9Key(3, new String[]{"D", "E", "F", "3"}, this);
        t9NormalKeys[1][0] = new T9Key(4, new String[]{"G", "H", "I", "4"}, this);
        t9NormalKeys[1][1] = new T9Key(5, new String[]{"J", "K", "L", "5"}, this);
        t9NormalKeys[1][2] = new T9Key(6, new String[]{"M", "N", "O", "6"}, this);
        t9NormalKeys[2][0] = new T9Key(7, new String[]{"P", "Q", "R", "S", "7"}, this);
        t9NormalKeys[2][1] = new T9Key(8, new String[]{"T", "U", "V", "8"}, this);
        t9NormalKeys[2][2] = new T9Key(9, new String[]{"W", "X", "Y", "Z", "9"}, this);
        t9NormalKeys[0][0].onFocus();

        t9SymbolKeys = new T9Key[3][3];
        t9SymbolKeys[0][0] = new T9Key(1, new String[]{".", ","}, this);
        t9SymbolKeys[0][1] = new T9Key(2, new String[]{"!", "$"}, this);
        t9SymbolKeys[0][2] = new T9Key(3, new String[]{"@", "&"}, this);
        t9SymbolKeys[1][0] = new T9Key(4, new String[]{"-+"}, this);
        t9SymbolKeys[1][1] = new T9Key(5, new String[]{"*"}, this);
        t9SymbolKeys[1][2] = new T9Key(6, new String[]{"_"}, this);
        t9SymbolKeys[2][0] = new T9Key(7, new String[]{"#"}, this);
        t9SymbolKeys[2][1] = new T9Key(8, new String[]{"="}, this);
        t9SymbolKeys[2][2] = new T9Key(9, new String[]{"/"}, this);
    }

    private T9Key getCurrentFocusedKey() {
        if (mode == 0) {
            return t9NormalKeys[currentKeyPositionY][currentKeyPositionX];
        } else {
            return t9SymbolKeys[currentKeyPositionY][currentKeyPositionX];
        }
    }

    private void onKeyboardMoveUp(){
        if (currentKeyPositionY == 0) {
            return;
        }
        T9Key currentFocusedKey = getCurrentFocusedKey();
        currentFocusedKey.onLeave();
        currentKeyPositionY--;
        currentFocusedKey = getCurrentFocusedKey();
        currentFocusedKey.onFocus();
    }

    private void onKeynoardMoveDown(){
        if (currentKeyPositionY == 2) {
            return;
        }
        T9Key currentFocusedKey = getCurrentFocusedKey();
        currentFocusedKey.onLeave();
        currentKeyPositionY++;
        currentFocusedKey = getCurrentFocusedKey();
        currentFocusedKey.onFocus();
    }

    private void onKeyboardMoveLeft(){
        if (currentKeyPositionX == 0) {
            return;
        }
        T9Key currentFocusedKey = getCurrentFocusedKey();
        currentFocusedKey.onLeave();
        currentKeyPositionX--;
        currentFocusedKey = getCurrentFocusedKey();
        currentFocusedKey.onFocus();
    }

    private void onKeyboardMoveRight(){
        if (currentKeyPositionX == 2) {
            return;
        }
        T9Key currentFocusedKey = getCurrentFocusedKey();
        currentFocusedKey.onLeave();
        currentKeyPositionX++;
        currentFocusedKey = getCurrentFocusedKey();
        currentFocusedKey.onFocus();
    }

    private class T9Key {
        private String[] characters;
        private int state = 0;
        private int currentIndex = -1;
        private boolean characterCommitted = false;

        private java.util.Timer timer = null;
        private java.util.TimerTask timerTask;
        private OnScreenKeyboard onScreenKeyboard;
        
        private void resetTimer() {
            cancelTimer();
            timer = new java.util.Timer();
            timerTask = new java.util.TimerTask() {
                @Override
                public void run() {
                    onScreenKeyboard.onCommitCharacter(getCharacter());
                    currentIndex = -1;
                    characterCommitted = true;
                }
            };
            timer.schedule(timerTask, 1000);
        }

        public T9Key(int keyNumber, String[] characters, OnScreenKeyboard onScreenKeyboard) {
            this.characters = characters;
            this.onScreenKeyboard = onScreenKeyboard;
        }

        public void onFocus() {
            state = 1;
            characterCommitted = true;
        }

        public void onKeyPress() {
            characterCommitted = false;
            if (currentIndex < characters.length - 1) {
                currentIndex++;
            } else {
                currentIndex = 0;
            }
            resetTimer();
        }

        public void onLeave() {
            if (!characterCommitted) {
                onScreenKeyboard.onCommitCharacter(getCharacter());
                characterCommitted = true;
            }
            cancelTimer();
            state = 0;
            currentIndex = -1;
        }

        public String getCharacter() {
            return characters[currentIndex];
        }

        public boolean isFocused() {
            return state == 1;
        }

        public String getUncommitCharacter(boolean isUpperCase) {
            if (currentIndex == -1 || characterCommitted == true) {
                return "";
            } else {
                return isUpperCase ? characters[currentIndex].toUpperCase() : characters[currentIndex].toLowerCase();
            }
        }

        public void cancelTimer() {
            try {
                if (timerTask != null)
                timerTask.cancel();
            } catch (Exception e) {
                // Ignore
            }

            try {
                if (timer != null)
                timer.cancel();
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}
