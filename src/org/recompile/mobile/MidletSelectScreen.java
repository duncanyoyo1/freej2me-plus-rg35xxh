package org.recompile.mobile;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class MidletSelectScreen {
    private List<String> midlets;
    private int selectedIndex = 0;
    private int lastSelectIndex = -1;
    private int textScrollIndex = 0;
    private long lastScrollTime = 0;

    private ScheduledExecutorService executorService;

    public MidletSelectScreen() {
        executorService = java.util.concurrent.Executors.newScheduledThreadPool(1);;
        midlets = Mobile.getPlatform().loader.getMIDletNames();
    }

    public void Start() {
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                showMidletSelect();
            }
        }, 0, 16, TimeUnit.MILLISECONDS);
    }

    public void OnKey(int keyCode) {
        switch (keyCode) {
            // UP
            case 0:
                if (selectedIndex >= 1) {
                    textScrollIndex = 0;
                    --selectedIndex;
                }
                break;
            // Down
            case 1:
                if (selectedIndex < midlets.size()-1) {
                    textScrollIndex = 0;
                    ++selectedIndex;
                }
                break;
            // Fire
            case 7:

                // Stop the executor service
                try {
                    executorService.shutdownNow();
                } catch (Exception e) {

                }

                if (midlets.size() == 0) {
                    return;
                }

                // Start the selected MIDlet
                Mobile.getPlatform().startMidlet(selectedIndex);
                break;
            default:
                return;
        }
    }

    public void showMidletSelect() {

        long currentTime = System.currentTimeMillis();
        int width = Mobile.getPlatform().getLCD().getWidth();
        int height = Mobile.getPlatform().getLCD().getHeight();

        if (selectedIndex != lastSelectIndex) {
            lastSelectIndex = selectedIndex;
            textScrollIndex = 0; // Reset text scroll index when changing selection
            lastScrollTime = currentTime; // Reset scroll time when changing selection
        } else if (currentTime - lastScrollTime >= 500) {
            textScrollIndex++;
            if (textScrollIndex > midlets.get(selectedIndex).length()) {
                textScrollIndex = 0;
            }
            lastScrollTime = currentTime;
        }

        PlatformGraphics gc = Mobile.getPlatform().gePlatformGraphics();

		BufferedImage overlayImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D overlayGraphics = overlayImage.createGraphics();

		gc.getGraphics2D().setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
		gc.getGraphics2D().setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		// Set the overlay background
		overlayGraphics.setColor(new Color(0, 0, 0, 255)); // BG is a solid black
		overlayGraphics.fillRect(0, 0, width, height);

		// Adjust the font size
		int fontSize = 10; // Base font size
		overlayGraphics.setFont(overlayGraphics.getFont().deriveFont((float) fontSize));
		overlayGraphics.setColor(new Color(255, 175, 0, 255)); // Text color is orange

        if (midlets.size() == 0) {
            overlayGraphics.drawString("No MIDlets found!", 3, 10);
            overlayGraphics.dispose(); // Clean up graphics
            gc.getGraphics2D().drawImage(overlayImage, 0, 0, width, height, null);
            return;
        }

		// Draw the MIDlet selection text
        String midletText = "Select MIDlet:";
		overlayGraphics.drawString(midletText, 3, 10);

		// Draw the list of MIDlets
		int yPosition = 20;
		int maxItems = (width - 2) / 10;

        for (int i = 0; i < midlets.size(); i++) {
            if (i == selectedIndex) {
                overlayGraphics.setColor(new Color(255, 255, 255, 255)); // Selected entry color is white
                String midletName = midlets.get(i);
                if (midletName.length() < maxItems) {
                    overlayGraphics.drawString(midletName, 3, yPosition);
                } else {
                    String displayedText = midletName.substring(textScrollIndex);
                    overlayGraphics.drawString(displayedText, 3, yPosition);
                    if (displayedText.length() < maxItems) {
                        textScrollIndex = 0;
                    }
                }
            } else {
                overlayGraphics.setColor(new Color(255, 175, 0, 255)); // Non-selected entry color is orange
                overlayGraphics.drawString(midlets.get(i), 3, yPosition);
            }
            yPosition += 10;
        }

		overlayGraphics.dispose(); // Clean up graphics

		// Scale the overlay image to fit the screen
		gc.getGraphics2D().drawImage(overlayImage, 0, 0, width, height, null);
	}
}
