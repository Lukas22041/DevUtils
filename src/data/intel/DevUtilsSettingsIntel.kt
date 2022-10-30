package data.intel

import DevUtilsPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TextFieldAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.JSONUtils
import org.lazywizard.lazylib.JSONUtils.CommonDataJSONObject
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.awt.Color
import java.lang.NumberFormatException


class DevUtilsSettingsIntel() : BaseIntelPlugin()
{

    var remove = false

    var receivingInput = false

    companion object
    {
        var amountOfMaxEntries: TextFieldAPI? = null
        var devModeIntelButton: ButtonAPI? = null
    }


    //Have to get the IntelUIAPI from a button press because there is no other way to access it for some reason...
    var UIAPI: IntelUIAPI? = null

    init {
        Global.getSector().addScript(this)
    }

    override fun reportPlayerClickedOn() {
        super.reportPlayerClickedOn()
    }

    override fun reportRemovedIntel() {
        super.reportRemovedIntel()
        Global.getSector().removeScript(this)
    }

    override fun runWhilePaused(): Boolean {
        return true
    }

    override fun shouldRemoveIntel(): Boolean {
        return remove
    }

    override fun getSmallDescriptionTitle(): String {
        return "Settings"
    }

    override fun createIntelInfo(info: TooltipMakerAPI, mode: IntelInfoPlugin.ListInfoMode?) {
        val c = Misc.getHighlightColor()
        info.addPara(smallDescriptionTitle, c, 1f)
    }

    override fun advance(amount: Float)
    {
        if (receivingInput)
        {
            if (Keyboard.getEventKeyState())
            {
                DevUtilsPlugin.PressedKey = Keyboard.getEventKey()
                receivingInput = false
                UIAPI!!.recreateIntelUI()


            }
            if (Mouse.getEventButtonState() && Mouse.getEventButton() != -1 && Mouse.getEventButton() != 0 && Mouse.getEventButton() != 1)
            {
                DevUtilsPlugin.PressedMouseButton = Mouse.getEventButton()
                receivingInput = false
                UIAPI!!.recreateIntelUI()
                UIAPI = null

            }
        }
    }

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {
        val tc = Misc.getTextColor()
        val UIColor: Color = Global.getSector().playerFaction.baseUIColor
        val UIColorDark: Color = Global.getSector().playerFaction.darkUIColor
        val mainColor = Global.getSector().playerFaction.baseUIColor
        val highlightColor = Misc.getHighlightColor()

        var playerfleet = Global.getSector().playerFleet

        info.addPara("Configuring window for some of DevUtils settings. Press Save to apply the changes.", 10f)

        info.addButton("Save Changes", "SAVE", UIColor, UIColorDark, width, 20f, 10f * 2f);
        info.addSectionHeading("Keybinds", Alignment.MID, 10f)

        info.addSpacer(10f)

        info.addPara("Devmode Keybind: ${Keyboard.getKeyName(DevUtilsPlugin.PressedKey)}", 10f, Misc.getHighlightColor(), "Devmode Keybind:")
        info.addPara("Devmode Mouse Button: ${Mouse.getButtonName( DevUtilsPlugin.PressedMouseButton)}", 10f, Misc.getHighlightColor(), "Devmode Mouse Button:")

        var keyText = "Register new Devmode Keybind"
        if (receivingInput)
        {
            keyText = "Press Key/Click Mousebutton"
        }

        info.addButton(keyText, "KEYBIND", UIColor, UIColorDark, width, 20f, 10f * 2f);
        info.addSpacer(5f)

        info.addPara("-Mouse 0 & 1 are blacklisted.\n-Dont move your mouse while pressing the button, javas Library reports it as button Input.", 10f)

        info.addSectionHeading("Script Overlay", Alignment.MID, 10f)

        info.addPara("The Script overlay can be moved with the arrow keys and numpad keys. The zoom can be changed with the +/- keys on the Numpad.\n\n" +
                "Pressing \"Save Changes\" will also save the position and zoom of the overlay for future sessions.", 10f)

        var color = when (DevUtilsPlugin.OverlayColor)
        {
            0 -> Misc.getBasePlayerColor()
            1 -> Misc.getHighlightColor()
            2-> Misc.getPositiveHighlightColor()
            3 -> Misc.getNegativeHighlightColor()
            else -> Misc.getBasePlayerColor()
        }

        info.addButton("Change overlay color", "OVERLAY_COLOR", color, Color(50, 50, 50), width, 20f, 10f * 2f);

        info.addSectionHeading("Misc", Alignment.MID, 10f)

        info.addPara("Maximum amount of Entries shown in search lists. Only accepts whole numbers.", 10f)
        amountOfMaxEntries = info.addTextField(width, 10f)
        amountOfMaxEntries!!.text = DevUtilsPlugin.maxEntries.toString()

        devModeIntelButton = info.addCheckbox(32f, 32f, "Only show DevUtils in devmode", ButtonAPI.UICheckboxSize.SMALL, 10f)
        devModeIntelButton!!.isChecked = DevUtilsPlugin.devModeOnlyIntel



    }

    override fun doesButtonHaveConfirmDialog(buttonId: Any?): Boolean {
        return false
    }

    override fun buttonPressConfirmed(buttonId: Any, ui: IntelUIAPI) {
        UIAPI = ui
        if(buttonId == "KEYBIND")
        {
            receivingInput = true
            ui.recreateIntelUI()
        }
        if (buttonId == "SAVE")
        {
            DevUtilsPlugin.devModeOnlyIntel = devModeIntelButton!!.isChecked

            try {
                DevUtilsPlugin.maxEntries = Integer.parseInt(amountOfMaxEntries!!.text)
            }
            catch(e: NumberFormatException) {
                DevUtilsPlugin.maxEntries = 50
            }

            DevUtilsPlugin.saveDataToJson()
            ui.recreateIntelUI()
        }
        if (buttonId == "OVERLAY_COLOR")
        {
            if (DevUtilsPlugin.OverlayColor < 3)
            {
                DevUtilsPlugin.OverlayColor++
            }
            else
            {
                DevUtilsPlugin.OverlayColor = 0
            }
            ui.recreateIntelUI()
        }
    }

    override fun getIcon(): String? {
        return Global.getSettings().getSpriteName("intel", "important")
    }

    override fun getIntelTags(map: SectorMapAPI?): Set<String>? {
        val tags = super.getIntelTags(map)
        tags.add("DevUtils - Sector")
        return tags
    }

    override fun getCommMessageSound(): String? {
        return super.getCommMessageSound()
    }



}