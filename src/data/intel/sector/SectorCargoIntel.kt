package data.intel.sector

import DevUtilsPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.loading.FighterWingSpecAPI
import com.fs.starfarer.api.loading.HullModSpecAPI
import com.fs.starfarer.api.loading.WeaponSpecAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TextFieldAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.loading.specs.FighterWingSpec
import data.scripts.util.MagicSettings
import java.awt.Color
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.logging.Filter

class SectorCargoIntel() : BaseIntelPlugin()
{

    var remove = false
    var idField: TextFieldAPI? = null

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
        return "Cargo"
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




        info.addPara("List of all Commodities, Weapons, Fighters and Ships loaded in to the game." +
                "Shows any entity that matches with atleast one Filter.\n\n-Seperated by Comma.\n-Spaces are ignored.\n-Upper/Lowercase is ignored.", 10f)

        var idFieldText = ""

        if (idField != null) idFieldText = idField!!.text

        info.addSectionHeading("Filter by Name/ID", Alignment.MID ,10f)
        idField = info.addTextField(width, 3f)
        idField!!.text = idFieldText


        info.addButton("Filter", "FILTER", UIColor, UIColorDark, width, 20f, 10f);
        info.addSpacer(5f)

        var Cap = 0

        var IDfilter = idField!!.text.lowercase().filter{!it.isWhitespace()}.split(",");

        var ships = ArrayList<ShipHullSpecAPI>()
        var fighters = ArrayList<FighterWingSpecAPI>()
        var weapons = ArrayList<WeaponSpecAPI>()
        var hullmods = ArrayList<HullModSpecAPI>()
        var commodities = ArrayList<CommoditySpecAPI>()

        for (filter in IDfilter)
        {
            Global.getSettings().allShipHullSpecs.forEach { ship ->
                if (Cap > DevUtilsPlugin.maxEntries) return@forEach
                if (ship.isDHull) return@forEach
                if (ship.hullId.lowercase().contains(filter) || ship.hullName.lowercase().contains(filter))
                {
                    ships.add(ship)
                    Cap++
                }
            }
            Global.getSettings().allFighterWingSpecs.forEach { fighter ->
                if (Cap > DevUtilsPlugin.maxEntries) return@forEach
                if (fighter.id.lowercase().contains(filter) || fighter.wingName.lowercase().contains(filter))
                {
                    fighters.add(fighter)
                    Cap++
                }
            }
            Global.getSettings().allWeaponSpecs.forEach { weapon ->
                if (Cap > DevUtilsPlugin.maxEntries) return@forEach
                if (weapon.weaponId.lowercase().contains(filter) || weapon.weaponName.lowercase().contains(filter))
                {
                    weapons.add(weapon)
                    Cap++
                }
            }
            Global.getSettings().allHullModSpecs.forEach { hullmod ->
                if (Cap > DevUtilsPlugin.maxEntries) return@forEach
                if (hullmod.id.lowercase().contains(filter) || hullmod.displayName.lowercase().contains(filter))
                {
                    hullmods.add(hullmod)
                    Cap++
                }
            }
            Global.getSettings().allCommoditySpecs.forEach { commodity ->
                if (Cap > DevUtilsPlugin.maxEntries) return@forEach
                if (commodity.id.lowercase().contains(filter) || commodity.name.lowercase().contains(filter))
                {
                    commodities.add(commodity)
                    Cap++
                }
            }
        }

        for (commodity in commodities)
        {
            info.addSectionHeading("${commodity.name} (Commodity)", Alignment.MID, 10f)
            var image = info.beginImageWithText(commodity.iconName, 32f)

            image.addPara("Name: ${commodity.name}", 10f, Misc.getHighlightColor(), "Name:")
            image.addPara("Id: ${commodity.id}", 10f, Misc.getHighlightColor(), "Id: ")
            info.addImageWithText(10f)
            info.addButton("Add 50x to Cargo", commodity, UIColor, UIColorDark, width, 20f, 10f);
        }

        for (ship in ships)
        {
            info.addSectionHeading("${ship.hullName} (Ship)", Alignment.MID, 10f)
            var image = info.beginImageWithText(ship.spriteName, 32f)

            image.addPara("Name: ${ship.hullName}", 10f, Misc.getHighlightColor(), "Name:")
            image.addPara("Id: ${ship.hullId}", 10f, Misc.getHighlightColor(), "Id: ")
            info.addImageWithText(10f)
            info.addButton("Add 1x to Fleet", ship, UIColor, UIColorDark, width, 20f, 10f);
        }

        for (weapon in weapons)
        {
            info.addSectionHeading("${weapon.weaponName} (Weapon)", Alignment.MID, 10f)
            var image = info.beginImageWithText(weapon.hardpointSpriteName, 32f)

            image.addPara("Name: ${weapon.weaponName}", 10f, Misc.getHighlightColor(), "Name:")
            image.addPara("Id: ${weapon.weaponId}", 10f, Misc.getHighlightColor(), "Id: ")
            info.addImageWithText(10f)
            info.addButton("Add 5x to Cargo", weapon, UIColor, UIColorDark, width, 20f, 10f);
        }

        for (hullmod in hullmods)
        {
            info.addSectionHeading("${hullmod.displayName} (Hullmod)", Alignment.MID, 10f)
            var image = info.beginImageWithText(hullmod.spriteName, 32f)

            image.addPara("Name: ${hullmod.displayName}", 10f, Misc.getHighlightColor(), "Name:")
            image.addPara("Id: ${hullmod.id}", 10f, Misc.getHighlightColor(), "Id: ")
            info.addImageWithText(10f)
            info.addButton("Add 1x to Cargo", hullmod, UIColor, UIColorDark, width, 20f, 10f);
        }

        for (fighter in fighters)
        {
            info.addSectionHeading("${fighter.wingName} (Fighter)", Alignment.MID, 10f)
            info.addPara("Name: ${fighter.wingName}", 10f, Misc.getHighlightColor(), "Name:")
            info.addPara("Id: ${fighter.id}", 10f, Misc.getHighlightColor(), "Id: ")
            info.addButton("Add 2x to Cargo", fighter, UIColor, UIColorDark, width, 20f, 10f);
        }
    }


    override fun doesButtonHaveConfirmDialog(buttonId: Any?): Boolean {
        return false
    }

    override fun buttonPressConfirmed(buttonId: Any, ui: IntelUIAPI)
    {
        when (buttonId)
        {
            is String ->
            {
                if (buttonId.toString() == "FILTER")
                {
                    ui.recreateIntelUI()
                }
            }

            is CommoditySpecAPI ->
            {
                Global.getSector().playerFleet.cargo.addCommodity(buttonId.id, 50f)
            }
            is ShipHullSpecAPI ->
            {
                var member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, buttonId.baseHullId + "_Hull");
                Global.getSector().playerFleet.fleetData.addFleetMember(member)
            }
            is HullModSpecAPI ->
            {
                Global.getSector().playerFleet.cargo.addHullmods(buttonId.id, 1)
            }
            is WeaponSpecAPI ->
            {
                Global.getSector().playerFleet.cargo.addWeapons(buttonId.weaponId, 5)
            }
            is FighterWingSpecAPI ->
            {
                Global.getSector().playerFleet.cargo.addFighters(buttonId.id, 2)
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