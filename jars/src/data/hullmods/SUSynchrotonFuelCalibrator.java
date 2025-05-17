package data.hullmods;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.BuffManagerAPI.Buff;
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

public class SUSynchrotonFuelCalibrator implements HullModEffect, HullModFleetEffect {
   private static final int MAX_FLEET_SYNCHRO = 6;
   private static final float FUEL_REDUCTION = 5.0F;
   private static final float INTERVALDURATION = 0.01F;
   private final String ID = "SUSynchrotonFuelCalibrator";

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
      FleetMemberAPI ship = stats.getFleetMember();
      if (ship != null) {
         if (ship.getFleetData() != null) {
            if (ship.getFleetData().getFleet() != null) {
               if (ship.getFleetData().getMembersListCopy() != null) {
                  int fleetFuelCalibrateNum = 0;

                  for (FleetMemberAPI fleetSynchro : ship.getFleetData().getMembersListCopy()) {
                     if (fleetSynchro.getVariant().hasHullMod("specialsphmod_synchroton_amfuel_extension")) {
                        fleetFuelCalibrateNum++;
                     }

                     if (fleetFuelCalibrateNum == 6) {
                        break;
                     }
                  }

                  float effectMult = fleetFuelCalibrateNum;
                  stats.getFuelUseMod().modifyPercent(id, -5.0F * effectMult);
               }
            }
         }
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      if (!ship.getVariant().hasHullMod("specialsphmod_synchrotoncore_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_synchroton_amfuel_extension");
         ship.getVariant().removeMod("specialsphmod_synchroton_amfuel_extension");
      }
   }

   public void advanceInCampaign(CampaignFleetAPI fleet) {
      if (fleet.isInCurrentLocation()) {
         boolean hasSynchroBuff = false;

         for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (!member.isMothballed() && member.getVariant() != null && member.getVariant().hasHullMod("specialsphmod_synchroton_amfuel_extension")) {
               hasSynchroBuff = true;
               break;
            }
         }

         if (hasSynchroBuff) {
            boolean forceFleetSync = false;

            for (FleetMemberAPI buffMember : fleet.getFleetData().getMembersListCopy()) {
               if (buffMember.getVariant() != null && !buffMember.getVariant().hasHullMod("specialsphmod_synchroton_amfuel_extension")) {
                  Buff buff = buffMember.getBuffManager().getBuff(this.ID);
                  if (buff instanceof SUSynchrotonFuelCalibrator.SynchroAMFuelCalibrator) {
                     ((SUSynchrotonFuelCalibrator.SynchroAMFuelCalibrator)buff).setDuration(0.01F);
                  } else {
                     buffMember.getBuffManager().addBuff(new SUSynchrotonFuelCalibrator.SynchroAMFuelCalibrator(this.ID, 0.01F));
                     forceFleetSync = true;
                  }
               }
            }

            if (forceFleetSync) {
               fleet.forceSync();
            }
         }
      }
   }

   public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
   }

   public void advanceInCampaign(FleetMemberAPI member, float amount) {
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
      return this.getDescriptionParam(index, hullSize);
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
            new String[]{"This hullmod is an extension of Synchro-Fuel Loader. It will remove itself when its parent hullmod is no longer present."}
         );
      }
   }

   public void onFleetSync(CampaignFleetAPI fleet) {
   }

   public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      return true;
   }

   public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      return null;
   }

   public boolean isApplicableToShip(ShipAPI ship) {
      return false;
   }

   public String getUnapplicableReason(ShipAPI ship) {
      return null;
   }

   public boolean affectsOPCosts() {
      return false;
   }

   public boolean withAdvanceInCampaign() {
      return true;
   }

   public boolean withOnFleetSync() {
      return true;
   }

   public Color getBorderColor() {
      return null;
   }

   public Color getNameColor() {
      return null;
   }

   public int getDisplaySortOrder() {
      return 80;
   }

   public int getDisplayCategoryIndex() {
      return -1;
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

   public class SynchroAMFuelCalibrator implements Buff {
      private final String buffID;
      private float buffDuration;

      public SynchroAMFuelCalibrator(final String id, final float dur) {
         this.buffID = id;
         this.buffDuration = dur;
      }

      public float getDur() {
         return this.buffDuration;
      }

      public void setDuration(float dur) {
         this.buffDuration = dur;
      }

      public String getId() {
         return this.buffID;
      }

      public boolean isExpired() {
         return this.buffDuration <= 0.0F;
      }

      public void apply(FleetMemberAPI member) {
         if (member != null) {
            if (member.getFleetData() != null) {
               if (member.getFleetData().getFleet() != null) {
                  int fleetFuelCalibrateNum = 0;

                  for (FleetMemberAPI fleetSynchro : member.getFleetData().getMembersListCopy()) {
                     if (fleetSynchro.getVariant().hasHullMod("specialsphmod_synchroton_amfuel_extension")) {
                        fleetFuelCalibrateNum++;
                     }

                     if (fleetFuelCalibrateNum == 6) {
                        break;
                     }
                  }

                  float effectMult = fleetFuelCalibrateNum;
                  member.getStats().getFuelUseMod().modifyPercent(this.buffID, -5.0F * effectMult);
               }
            }
         }
      }

      public void advance(float days) {
         this.buffDuration -= days;
      }
   }
}
