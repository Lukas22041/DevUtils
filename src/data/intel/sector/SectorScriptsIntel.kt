package data.intel.sector

import DevUtilsPlugin
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TextFieldAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import data.scripts.util.MagicSettings
import java.awt.Color
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.logging.Filter

class SectorScriptsIntel() : BaseIntelPlugin()
{

    var remove = false
    companion object
    {
        var idField: TextFieldAPI? = null
    }

    override fun reportPlayerClickedOn() {
        super.reportPlayerClickedOn()
    }

    override fun reportRemovedIntel() {
        super.reportRemovedIntel()
    }

    override fun shouldRemoveIntel(): Boolean {
        return remove
    }

    override fun getSmallDescriptionTitle(): String {
        return "Sector Scripts"
    }

    override fun createIntelInfo(info: TooltipMakerAPI, mode: IntelInfoPlugin.ListInfoMode?) {
        val c = Misc.getHighlightColor()
        info.addPara(smallDescriptionTitle, c, 1f)
    }

    override fun advance(amount: Float) {
        super.advance(amount)
    }

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {
        val tc = Misc.getTextColor()
        val UIColor: Color = Global.getSector().playerFaction.baseUIColor
        val UIColorDark: Color = Global.getSector().playerFaction.darkUIColor
        val mainColor = Global.getSector().playerFaction.baseUIColor
        val highlightColor = Misc.getHighlightColor()

        var playerfleet = Global.getSector().playerFleet
        var sector = Global.getSector()

        var scripts: MutableList<Any> = ArrayList()

        scripts.addAll(Global.getSector().scripts)
        scripts.addAll(Global.getSector().transientScripts)
        scripts.addAll(Global.getSector().allListeners)
        scripts.addAll(Global.getSector().intelManager.intel)

        var systems = Global.getSector().starSystems

        info.addPara("List of all Everyframe/Listener/Intel scripts attached to the Sector. " +
                "Shows any script that matches by name.\n\nClass-Scope Variables of Scripts can be pinned to the HUD.", 10f)

        var idFieldText = ""
        var tagsFieldText = ""

        if (idField != null) idFieldText = idField!!.text

        info.addSectionHeading("Filter by Name", Alignment.MID ,10f)
        idField = info.addTextField(width, 3f)
        idField!!.text = idFieldText

        info.addButton("Filter", "FILTER", UIColor, UIColorDark, width, 20f, 10f);
        info.addButton("Unpin active Script", "UNPIN", UIColor, UIColorDark, width, 20f, 10f);
        info.addSpacer(2f)

        var Cap = 0

        var IDfilter = idField!!.text.lowercase().filter{!it.isWhitespace()}.split(",");

        var validScripts = ArrayList<Any>()

        scripts.forEach { script ->
            if (Cap > DevUtilsPlugin.maxEntries) return@forEach
            for (filter in IDfilter)
            {
                if (script.javaClass.simpleName.lowercase().contains(filter))
                {
                    if (Cap > DevUtilsPlugin.maxEntries) break
                    validScripts.add(script)
                    Cap++
                }
                if (filter.contains(script.javaClass.simpleName.lowercase()))
                {
                    if (Cap > DevUtilsPlugin.maxEntries) break
                    validScripts.add(script)
                    Cap++
                }
            }
        }

    for (script in validScripts)
    {
        var type = when(script)
        {
            is IntelInfoPlugin -> "Intel"
            is EveryFrameScript -> "EveryFrameScript"
            is CampaignEventListener -> "Listener"
            else -> "Unknown"
        }

        info.addSectionHeading("${script.javaClass.simpleName}", Alignment.MID, 10f)
        info.addPara("Name: ${script.javaClass.simpleName}", 10f, Misc.getHighlightColor(), "Name: ")
        info.addPara("Type: ${type}", 10f, Misc.getHighlightColor(), "Type: ")

        var buttonText = "Pin Script to UI"
        if (script.javaClass.declaredFields.isEmpty()) buttonText = "No variables found"

        var button = info.addButton(buttonText, script, UIColor, UIColorDark, width, 20f, 10f);
        if (script.javaClass.declaredFields.isEmpty()) button.isEnabled = false
    }

}

override fun doesButtonHaveConfirmDialog(buttonId: Any?): Boolean {
    return false
}

override fun buttonPressConfirmed(buttonId: Any, ui: IntelUIAPI)
{
    if (buttonId is String)
    {
        if (buttonId.toString() == "FILTER")
        {
            ui.recreateIntelUI()
        }
        if (buttonId.toString() == "UNPIN")
        {
            Global.getSector().memoryWithoutUpdate.set("\$DevUtils_Hud", null)
        }
    }
    else
    {
        Global.getSector().memoryWithoutUpdate.set("\$DevUtils_Hud", buttonId)
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