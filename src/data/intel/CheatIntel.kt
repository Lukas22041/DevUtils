package data.intel

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.DebugFlags
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import data.scripts.util.MagicSettings
import javafx.scene.input.MouseButton
import org.json.JSONObject
import org.lazywizard.lazylib.JSONUtils
import org.lazywizard.lazylib.JSONUtils.CommonDataJSONObject
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import sun.org.mozilla.javascript.internal.json.JsonParser
import java.awt.Color
import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseEvent



class CheatIntel() : BaseIntelPlugin()
{

    var remove = false

    override fun reportPlayerClickedOn() {
        super.reportPlayerClickedOn()
    }

    override fun reportRemovedIntel() {
        super.reportRemovedIntel()
        Global.getSector().removeScript(this)
    }

    override fun shouldRemoveIntel(): Boolean {
        return remove
    }

    override fun getSmallDescriptionTitle(): String {
        return "Cheats"
    }

    override fun createIntelInfo(info: TooltipMakerAPI, mode: IntelInfoPlugin.ListInfoMode?) {
        val c = Misc.getHighlightColor()
        info.addPara(smallDescriptionTitle, c, 1f)
    }

    override fun advance(amount: Float) {
    }

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {
        val tc = Misc.getTextColor()
        val UIColor: Color = Global.getSector().playerFaction.baseUIColor
        val UIColorDark: Color = Global.getSector().playerFaction.darkUIColor
        val mainColor = Global.getSector().playerFaction.baseUIColor
        val highlightColor = Misc.getHighlightColor()

        var playerfleet = Global.getSector().playerFleet

        info.addButton("Gain 1.000.000 Credits", "CREDITS", UIColor, UIColorDark, width, 20f, 10f * 2f);
        info.addButton("Positive Rep to every other faction", "POS_REP", UIColor, UIColorDark, width, 20f, 10f);
        info.addButton("Negative Rep to every other faction", "NEG_REP", UIColor, UIColorDark, width, 20f, 10f);
        info.addSpacer(5f)

        info.addButton("Add all Ships to Cargo", "SHIPS", UIColor, UIColorDark, width, 20f, 10f);
        info.addButton("Add all Weapons to Cargo", "WEAPONS", UIColor, UIColorDark, width, 20f, 10f);
        info.addButton("Add all Hullmods to Cargo", "HULLMODS", UIColor, UIColorDark, width, 20f, 10f);
    }



    override fun doesButtonHaveConfirmDialog(buttonId: Any?): Boolean {
        return false
    }

    override fun buttonPressConfirmed(buttonId: Any, ui: IntelUIAPI) {
        val button = buttonId as String

        when (button)
        {
            "DEVMODE" ->
            {
                Global.getSettings().isDevMode = !Global.getSettings().isDevMode
                DebugFlags.setStandardConfig()
            }
            "CREDITS" ->
            {
                Global.getSector().playerFleet.cargo.credits.add(1000000f)
            }
            "POS_REP" ->
            {
                var playerFaction: FactionAPI = Global.getSector().getFaction("player")
                var factions = Global.getSector().allFactions

                for (faction in factions)
                {
                    if (faction.id == "player") continue;
                    playerFaction.setRelationship(faction.id, 100f)
                }
            }
            "NEG_REP" ->
            {
                var playerFaction: FactionAPI = Global.getSector().getFaction("player")
                var factions = Global.getSector().allFactions

                for (faction in factions)
                {
                    if (faction.id == "player") continue;
                    playerFaction.setRelationship(faction.id, -100f)
                }
            }
            "SHIPS" ->
            {
                var specs = Global.getSettings().allShipHullSpecs

                for (spec in specs)
                {
                    if (spec.hullName.contains("(D)")) continue
                    var member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, spec.baseHullId + "_Hull");
                    Global.getSector().playerFleet.fleetData.addFleetMember(member)
                }
            }
            "WEAPONS" ->
            {
                var specs = Global.getSettings().allWeaponSpecs
                for (spec in specs)
                {
                    Global.getSector().playerFleet.cargo.addWeapons(spec.weaponId, 10)
                }
            }
            "HULLMODS" ->
            {
                var specs = Global.getSettings().allHullModSpecs
                for (spec in specs)
                {
                    Global.getSector().playerFleet.cargo.addHullmods(spec.id, 1)
                }
            }
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