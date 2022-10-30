package data.intel

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.loading.HullModSpecAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class FactionIntel(faction: FactionAPI) : BaseIntelPlugin()
{

    var remove = false
    var faction: FactionAPI

    var ships: MutableList<ShipHullSpecAPI> = ArrayList()
    var hullmods: MutableList<HullModSpecAPI> = ArrayList()
    var weapons: MutableList<WeaponSpecAPI> = ArrayList()

    init {
        this.faction = faction
        var KnownShips = faction.knownShips.toMutableList()
        var Hullspecs = Global.getSettings().allShipHullSpecs

        //Filters for Faction Ships
        for (spec in Hullspecs)
        {
            if (KnownShips.contains(spec.baseHullId) && !spec.hullName.contains("(D)"))
            {
                ships.add(spec)
            }

        }

        //Filters for Hullmods
        var KnownHullmods = faction.knownHullMods.toMutableList()
        var HullModSpecs = Global.getSettings().allHullModSpecs
        for (spec in HullModSpecs)
        {
            if (KnownHullmods.contains(spec.id))
            {
                hullmods.add(spec)
            }
        }

        //Filters for Weapons
        var KnownWeapons = faction.knownWeapons.toMutableList()
        var WeaponSpecs = Global.getSettings().allWeaponSpecs
        for (spec in WeaponSpecs)
        {
            if (KnownWeapons.contains(spec.weaponId))
            {
                weapons.add(spec)
            }
        }

        if (ships.isEmpty() && hullmods.isEmpty() && weapons.isEmpty())
        {
            remove = true
        }
    }

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
        return "${faction.displayName}"
    }

    override fun createIntelInfo(info: TooltipMakerAPI, mode: IntelInfoPlugin.ListInfoMode?) {
        val c = faction.color
        info.addPara(smallDescriptionTitle, c, 1f)
    }

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {
        val tc = Misc.getTextColor()
        val UIColor: Color = Global.getSector().playerFaction.baseUIColor
        val UIColorDark: Color = Global.getSector().playerFaction.darkUIColor
        val mainColor = Global.getSector().playerFaction.baseUIColor
        val highlightColor = Misc.getHighlightColor()



        var markets = Global.getSector().economy.marketsCopy
        var factionMarkets: MutableList<MarketAPI>? = ArrayList()
        for (market in markets)
        {
            if (market.factionId == faction.id)
            {
                factionMarkets!!.add(market)
            }
        }
        info.addSpacer(5f)

        info.addPara("Faction Name: ${faction.displayName}", 3f, Misc.getHighlightColor(), "Faction Name: ")
        info.addPara("Faction Id: ${faction.id}", 3f, Misc.getHighlightColor(), "Faction Id")

        info.addSpacer(10f)
        if (!factionMarkets!!.isEmpty())
        {
            info.addPara("Markets: ${factionMarkets.size} ", 3f, Misc.getHighlightColor(), "Markets:")
            for (market in factionMarkets!!)
            {
                info.addPara("Name: ${market.name}, Size: ${market.size}", 3f, Misc.getHighlightColor(), "Name", "Size")
            }
            info.addSpacer(10f)
        }

        if(!ships.isEmpty()) info.addButton("Add all Faction Ships to Cargo", "SHIPS", UIColor, UIColorDark, width, 20f, 10f);
        if(!weapons.isEmpty()) info.addButton("Add all Faction Weapons to Cargo", "WEAPONS", UIColor, UIColorDark, width, 20f, 10f);
        if(!hullmods.isEmpty()) info.addButton("Add all Faction Hullmods to Cargo", "HULLMODS", UIColor, UIColorDark, width, 20f, 10f);

        info.addSpacer(10f)

        info.addSectionHeading("Faction Ships", Alignment.MID, 3f)
        for (ship in ships)
        {
            var image = info.beginImageWithText(ship.spriteName, 64f)
            image.addPara("Ship Name: ${ship.hullName}", 10f, Misc.getHighlightColor(), "Ship Name")
            image.addPara("Ship Size: ${ship.hullSize}", 10f, Misc.getHighlightColor(), "Ship Size")

            info.addImageWithText(3f)
            info.addButton("Add ${ship.hullName} to Fleet", "ADD_TO_FLEET${ship.baseHullId}", width, 16f, 3f)
        }
        info.addSpacer(10f)
        info.addSectionHeading("Faction Weapons", Misc.getTextColor(), Color(140, 0, 35, 255),Alignment.MID, 3f)
        for (weapon in weapons)
        {
            var image = info.beginImageWithText(weapon.hardpointSpriteName, 48f)
            image.addPara("Weapon Name: ${weapon.weaponName}", 10f, Color(140, 0, 35, 255), "Weapon Name")
            info.addImageWithText(3f)
            info.addButton("Add ${weapon.weaponName} to Cargo", "ADD_WEAPON${weapon.weaponId}",  Misc.getTextColor(), Color(140, 0, 35, 255), width, 16f, 3f)
        }
        info.addSpacer(10f)
        info.addSectionHeading("Faction Hullmods", Misc.getTextColor(), Color(140, 80, 0, 255), Alignment.MID, 3f)
        for (hullmod in hullmods)
        {
            var image = info.beginImageWithText(hullmod.spriteName, 32f)
            image.addPara("Hullmod Name: ${hullmod.displayName}", 10f, Color(140, 80, 0, 255), "Hullmod Name")
            info.addImageWithText(3f)
            info.addButton("Add ${hullmod.displayName} to Cargo", "ADD_HULLMOD${hullmod.id}", Misc.getTextColor(), Color(140, 80, 0, 255), width, 16f, 3f)
        }
    }

    override fun doesButtonHaveConfirmDialog(buttonId: Any?): Boolean {
        return false
    }

    override fun buttonPressConfirmed(buttonId: Any, ui: IntelUIAPI) {
        val button = buttonId as String

        var cargo = Global.getSector().playerFleet.cargo

        when (button)
        {
            "SHIPS" ->
            {
                for (spec in ships)
                {
                    if (spec.hullName.contains("(D)")) continue
                    var member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, spec.baseHullId + "_Hull");
                    Global.getSector().playerFleet.fleetData.addFleetMember(member)
                }
            }
            "WEAPONS" ->
            {
                for (spec in weapons)
                {
                    Global.getSector().playerFleet.cargo.addWeapons(spec.weaponId, 10)
                }
            }
            "HULLMODS" ->
            {
                for (spec in hullmods)
                {
                    Global.getSector().playerFleet.cargo.addHullmods(spec.id, 1)
                }
            }
        }

        if (button.contains("ADD_TO_FLEET"))
        {
            var removedADD = button.replace("ADD_TO_FLEET", "")
            removedADD += "_Hull"
            var member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, removedADD);
            Global.getSector().playerFleet.fleetData.addFleetMember(member)
        }
        if (button.contains("ADD_WEAPON"))
        {
            var removedADD = button.replace("ADD_WEAPON", "")
            cargo.addWeapons(removedADD, 1)
        }
        if (button.contains("ADD_HULLMOD"))
        {
            var removedADD = button.replace("ADD_HULLMOD", "")
            cargo.addHullmods(removedADD, 1)
        }
    }

    override fun getIcon(): String? {
        return faction.logo
    }

    override fun getIntelTags(map: SectorMapAPI?): Set<String>? {
        val tags = super.getIntelTags(map)
        tags.add("DevUtils - Factions")
        return tags
    }

    override fun getCommMessageSound(): String? {
        return super.getCommMessageSound()
    }

}