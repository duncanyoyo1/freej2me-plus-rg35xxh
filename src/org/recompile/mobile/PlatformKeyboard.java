package org.recompile.mobile;

public class PlatformKeyboard {
    public static final int KEYCODE_UP = 0;
    public static final int KEYCODE_DOWN = 1;

    private static PlatformKeyboard instance = null;
    private OnScreenKeyboard onScreenKeyboard = null;
    
    public static synchronized PlatformKeyboard getInstance() {
        if (instance == null) {
            instance = new PlatformKeyboard();
            instance.onScreenKeyboard = OnScreenKeyboard.getInstance();
        }
        return instance;
    }

    public OnScreenKeyboard getOnScreenKeyboard() {
        return onScreenKeyboard;
    }
    
    public void onDeviceInput(int keyCode, int action) {

        if (keyCode == 19 && action == KEYCODE_DOWN) {
            // Toggle on-screen keyboard
            if (onScreenKeyboard.isShown()) {
                onScreenKeyboard.disable();
            } else {
                onScreenKeyboard.enable();
            }
            return;
        }

        if (onScreenKeyboard.isShown()) {
            // Handle on-screen keyboard input
            onScreenKeyboard.onInput(keyCode, action);
            return;
        };

        // Handle input
        switch (action) {
            case KEYCODE_DOWN:
                MobilePlatform.pressedKeys[keyCode] = true;
                if (Mobile.getPlatform().midletSelectScreen != null)
                {
                    Mobile.getPlatform().midletSelectScreen.OnKey(keyCode);
                }
                break;
            case KEYCODE_UP:
                MobilePlatform.pressedKeys[keyCode] = false;
                break;
        }
    }

    public void onOnScreenKeyboardStringInput(String input) {
        Mobile.getPlatform().onStringInput(input);
    }
}
