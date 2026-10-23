package canteen.ui.components;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class WrapLayout extends FlowLayout {

    private int preferredWidth = 0;
    private int horizontalGap;
    private int verticalGap;

    public WrapLayout() {
        super(LEFT, 12, 12);
        this.horizontalGap = 12;
        this.verticalGap = 12;
    }

    public WrapLayout(int horizontalGap, int verticalGap) {
        super(LEFT, horizontalGap, verticalGap);
        this.horizontalGap = horizontalGap;
        this.verticalGap = verticalGap;
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        return layoutSize(target, false);
    }

    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getWidth();
            if (targetWidth == 0) {
                targetWidth = preferredWidth > 0 ? preferredWidth : Integer.MAX_VALUE;
            }

            int h = super.getVgap();
            int w = 0;
            int rowWidth = 0;
            int rowHeight = 0;

            int nmembers = target.getComponentCount();
            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);
                if (!m.isVisible()) continue;

                Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                if (rowWidth + d.width > targetWidth && rowWidth > 0) {
                    w = Math.max(w, rowWidth);
                    h += rowHeight + super.getVgap();
                    rowWidth = 0;
                    rowHeight = 0;
                }
                if (rowWidth > 0) {
                    rowWidth += super.getHgap();
                }
                rowWidth += d.width;
                rowHeight = Math.max(rowHeight, d.height);
            }

            w = Math.max(w, rowWidth);
            h += rowHeight;

            Insets insets = target.getInsets();
            return new Dimension(w + insets.left + insets.right, h + insets.top + insets.bottom);
        }
    }

    @Override
    public void layoutContainer(Container target) {
        synchronized (target.getTreeLock()) {
            Insets insets = target.getInsets();
            int targetWidth = target.getWidth() - insets.left - insets.right;
            int x = insets.left;
            int y = insets.top;
            int rowHeight = 0;
            int rowStart = insets.left;

            int nmembers = target.getComponentCount();
            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);
                if (!m.isVisible()) continue;

                Dimension d = m.getPreferredSize();
                if (x + d.width > insets.left + targetWidth && x > rowStart) {
                    x = insets.left;
                    y += rowHeight + super.getVgap();
                    rowHeight = 0;
                    rowStart = insets.left;
                }

                m.setBounds(x, y, d.width, d.height);
                x += d.width + super.getHgap();
                rowHeight = Math.max(rowHeight, d.height);
            }
        }
    }

    public void setPreferredWidth(int preferredWidth) {
        this.preferredWidth = preferredWidth;
    }
}