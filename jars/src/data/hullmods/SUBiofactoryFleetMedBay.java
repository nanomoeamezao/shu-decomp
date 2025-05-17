package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.combat.HullModFleetEffect;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class SUBiofactoryFleetMedBay implements HullModEffect, HullModFleetEffect {
   private final String ID = "SUBiofactoryFleetMedBay";

   public void init(HullModSpecAPI spec) {
   }

   public void addRequiredItemSection(TooltipMakerAPI tmapi, FleetMemberAPI fmapi, ShipVariantAPI svapi, MarketAPI mapi, float f, boolean bln) {
   }

   public CargoStackAPI getRequiredItem() {
      return null;
   }

   public void applyEffectsAfterShipAddedToCombatEngine(ShipAPI ship, String id) {
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      if (!ship.getVariant().hasHullMod("specialsphmod_biofactoryembryo_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_biofactory_fleet_med");
         ship.getVariant().removeMod("specialsphmod_biofactory_fleet_med");
      }
   }

   public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
   }

   public void onFleetSync(CampaignFleetAPI fleet) {
      List<FleetMemberAPI> fleetMembers = new ArrayList<>();
      List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
      float sunriderBonus = 0.0F;

      for (FleetMemberAPI member : members) {
         if (!member.isMothballed()) {
            for (String hullmodID : member.getVariant().getHullMods()) {
               if ("specialsphmod_biofactory_fleet_med".equals(hullmodID)) {
                  fleetMembers.add(member);
               }
            }
         }
      }

      int index = 0;

      for (FleetMemberAPI fleetShip : fleetMembers) {
         for (int i = 0; i < members.size(); i++) {
            FleetMemberAPI memberx = members.get(i);
            if (!memberx.getVariant().hasHullMod("automated") && fleetShip == memberx) {
               if (Global.getSettings().getModManager().isModEnabled("Sunrider") && memberx.getVariant().getHullSpec().getBaseHullId().contains("Sunridership")
                  )
                {
                  sunriderBonus = 25.0F;
               }

               if (i <= 0) {
                  memberx.getStats().getCrewLossMult().unmodify(this.ID);
                  memberx.getStats().getDynamic().getStat("fighter_crew_loss_mult").unmodify(this.ID);
               }

               memberx.getStats().getCrewLossMult().modifyMult(this.ID, -24.0F + sunriderBonus * 0.01F);
               memberx.getStats().getDynamic().getStat("fighter_crew_loss_mult").modifyMult(this.ID, -24.0F + sunriderBonus * 0.01F);
               break;
            }
         }
      }
   }

   public void advanceInCampaign(FleetMemberAPI member, float amount) {
   }

   public void advanceInCampaign(CampaignFleetAPI fleet) {
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
   }

   public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
      return true;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         tooltip.addPara(
            "%s",
            SUStringCodex.SHU_TOOLTIP_PADMAIN,
            Misc.getGrayColor(),
            new String[]{"This hullmod is an extension of Biologic Commodity Replicator. It will remove itself when its parent hullmod is no longer present."}
         );
      }
   }

   public boolean affectsOPCosts() {
      return false;
   }

   public Color getBorderColor() {
      return null;
   }

   public Color getNameColor() {
      return null;
   }

   public int getDisplaySortOrder() {
      return 0;
   }

   public int getDisplayCategoryIndex() {
      return 0;
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
      return null;
   }

   public boolean isApplicableToShip(ShipAPI ship) {
      return false;
   }

   public String getUnapplicableReason(ShipAPI ship) {
      return null;
   }

   public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      return false;
   }

   public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      return null;
   }

   public boolean withAdvanceInCampaign() {
      return false;
   }

   public boolean withOnFleetSync() {
      return true;
   }

   public boolean hasSModEffectSection(HullSize hs, ShipAPI sapi, boolean bln) {
      return false;
   }

   public void addSModSection(TooltipMakerAPI tmapi, HullSize hs, ShipAPI sapi, float f, boolean bln, boolean bln1) {
   }

   public void addSModEffectSection(TooltipMakerAPI tmapi, HullSize hs, ShipAPI sapi, float f, boolean bln, boolean bln1) {
   }

   public boolean hasSModEffect() {
      return false;
   }

   public String getSModDescriptionParam(int i, HullSize hs) {
      return null;
   }

   public String getSModDescriptionParam(int i, HullSize hs, ShipAPI sapi) {
      return null;
   }

   public float getTooltipWidth() {
      return 0.0F;
   }

   public boolean isSModEffectAPenalty() {
      return false;
   }

   public boolean showInRefitScreenModPickerFor(ShipAPI sapi) {
      return false;
   }
}
