package data.intel

import DevUtilsPlugin
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.util.Misc
import data.PrintData
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ui.LazyFont
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11.*


class DevUtilsOverlay() : EveryFrameScript {


    private var toDraw: LazyFont.DrawableString? = null

    init {
        var font: LazyFont
        font = LazyFont.loadFont("graphics/fonts/insignia15LTaa.fnt");
        toDraw = font.createText("Pinned Script:", Misc.getBasePlayerColor(), 30f);
    }

    override fun advance(amount: Float)
    {
        if (Global.getSector().isFastForwardIteration()) return;
        var script: Any? = Global.getSector().memoryWithoutUpdate.get("\$DevUtils_Hud") ?: return

        val width = (Display.getWidth() * Display.getPixelScaleFactor()).toInt()
        val height = (Display.getHeight() * Display.getPixelScaleFactor()).toInt()

        val paused = Global.getSector().campaignUI.currentCoreTab == CoreUITabId.FLEET || Global.getSector().campaignUI.currentCoreTab == null
                && !Global.getSector().campaignUI.isShowingDialog && !Global.getSector().campaignUI.isShowingMenu

        if (!paused) return

        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) DevUtilsPlugin.OverlayPositionX += 0.005f
        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) DevUtilsPlugin.OverlayPositionX -= 0.005f
        if (Keyboard.isKeyDown(Keyboard.KEY_UP)) DevUtilsPlugin.OverlayPositionY += 0.005f
        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))DevUtilsPlugin. OverlayPositionY -= 0.005f
        if (Keyboard.isKeyDown(Keyboard.KEY_ADD)) DevUtilsPlugin.OverlayZoom += 0.005f
        if (Keyboard.isKeyDown(Keyboard.KEY_SUBTRACT)) DevUtilsPlugin.OverlayZoom -= 0.005f

        DevUtilsPlugin.OverlayPositionX = MathUtils.clamp(DevUtilsPlugin.OverlayPositionX, 0f, 0.9f)
        DevUtilsPlugin.OverlayPositionY = MathUtils.clamp(DevUtilsPlugin.OverlayPositionY, 0.1f, 1f)
        DevUtilsPlugin.OverlayZoom = MathUtils.clamp(DevUtilsPlugin.OverlayZoom, 0.1f, 1f)

        toDraw!!.text = script!!.javaClass.simpleName + " Script\n\n"
        toDraw!!.fontSize = 30f * DevUtilsPlugin.OverlayZoom

        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glMatrixMode(GL_PROJECTION)
        glPushMatrix()
        glLoadIdentity()
        glViewport(0, 0, width, height)
        glOrtho(0.0, width.toDouble(), 0.0, height.toDouble(), -1.0, 1.0)
        glMatrixMode(GL_MODELVIEW)
        glPushMatrix()
        glLoadIdentity()
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glTranslatef(0.01f, 0.01f, 0f)

        var textMap = PrintData().DisplayScriptValues(script)


        var color = when (DevUtilsPlugin.OverlayColor)
        {
            0 -> Misc.getBasePlayerColor()
            1 -> Misc.getHighlightColor()
            2-> Misc.getPositiveHighlightColor()
            3 -> Misc.getNegativeHighlightColor()
            else -> Misc.getBasePlayerColor()
        }

        toDraw!!.baseColor = color

        textMap.forEach {
            toDraw!!.append(it.key + ": ", color)
            toDraw!!.append(it.value, color)
            toDraw!!.append("\n", color)
        }

        toDraw!!.draw(width * DevUtilsPlugin.OverlayPositionX, height * DevUtilsPlugin.OverlayPositionY)

        glDisable(GL_BLEND)
        glPopMatrix()
        glMatrixMode(GL_PROJECTION)
        glPopMatrix()
        glPopAttrib()
    }


    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }


}