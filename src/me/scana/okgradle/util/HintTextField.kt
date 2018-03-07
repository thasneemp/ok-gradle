package me.scana.okgradle.util

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JTextField

class HintTextField(var hint: String = "") : JTextField() {
    override fun paint(g: Graphics?) {
        super.paint(g)
        if (text.isEmpty()) {
            (g as Graphics2D).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            val ins = insets
            val fm = g.getFontMetrics()
            g.setColor(Color.GRAY)
            g.drawString(hint, ins.left, height / 2 + fm.ascent / 2 - 2)
        }
    }
}