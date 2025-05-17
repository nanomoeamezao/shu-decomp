package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.HullModItemManager;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.everyframe.SUHullmodDisplayBlockScript;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import lunalib.lunaSettings.LunaSettings;
import org.lwjgl.input.Keyboard;

public class SUSynchrotonCoreUpgrades extends BaseHullMod {
   public static final String DATA_PREFIX = "synchroton_shu_check_";
   public static final String ITEM = "synchrotron";
   private static final float MAX_BURN_BONUS = 1.0F;
   private static final int MAX_FLEET_CALIBRATION = 6;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_SYNCHROTONCOREHMOD_EXTRA_EFFECT;
   float maxBurnBonus = SUPlugin.CM_SYNCHROTON_MAX_BURN_BONUS;
   float bombardmentReductionFrigate = SUPlugin.CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_FRIGATE_BONUS;
   float bombardmentReductionDestroyer = SUPlugin.CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_DESTROYER_BONUS;
   float bombardmentReductionCruiser = SUPlugin.CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_CRUISER_BONUS;
   float bombardmentReductionCapital = SUPlugin.CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_CAPITAL_BONUS;
   boolean toggleGeneralIncompat;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};

   private static final Map GENEVA_CORRECTIONS = new HashMap();

   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_synchrotoncore_upgrades") ? ALL_INCOMPAT_IDS : null;
   }

   public CargoStackAPI getRequiredItem() {
      return Global.getSettings().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData("synchrotron", null), null);
   }

   public void addRequiredItemSection(
      TooltipMakerAPI tooltip, FleetMemberAPI member, ShipVariantAPI currentVariant, MarketAPI dockedAt, float width, boolean isForModSpec
   ) {
      CargoStackAPI req = this.getRequiredItem();
      if (req != null) {
         float opad = 2.0F;
         if (isForModSpec || Global.CODEX_TOOLTIP_MODE) {
            Color color = Misc.getBasePlayerColor();
            if (isForModSpec) {
               color = Misc.getHighlightColor();
            }

            String name = req.getDisplayName();
            String aOrAn = Misc.getAOrAnFor(name);
            TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("synchrotron").getIconName(), 20.0F);
            text.addPara("Requires " + aOrAn + " %s to install.", opad, color, new String[]{name});
            tooltip.addImageWithText(5.0F);
         } else if (currentVariant != null && member != null) {
            if (currentVariant.hasHullMod(this.spec.getId())) {
               if (!currentVariant.getHullSpec().getBuiltInMods().contains(this.spec.getId())) {
                  Color color = Misc.getPositiveHighlightColor();
                  TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("synchrotron").getIconName(), 20.0F);
                  text2.addPara("Using item: " + req.getDisplayName(), color, opad);
                  tooltip.addImageWithText(5.0F);
               }
            } else {
               int available = HullModItemManager.getInstance().getNumAvailableMinusUnconfirmed(req, member, currentVariant, dockedAt);
               Color color = Misc.getPositiveHighlightColor();
               if (available < 1) {
                  color = Misc.getNegativeHighlightColor();
               }

               if (available < 0) {
                  available = 0;
               }

               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("synchrotron").getIconName(), 20.0F);
               text3.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", color, opad);
               tooltip.addImageWithText(5.0F);
            }
         }
      }
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.maxBurnBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SYNCHROTON_MAX_BURN_BONUS");
         this.bombardmentReductionFrigate = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_FRIGATE_BONUS");
         this.bombardmentReductionDestroyer = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_DESTROYER_BONUS");
         this.bombardmentReductionCruiser = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_CRUISER_BONUS");
         this.bombardmentReductionCapital = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_CAPITAL_BONUS");
      }

      if (this.enableCustomSM) {
         stats.getMaxBurnLevel().modifyFlat(id, this.maxBurnBonus);
         HullSize shipSize = stats.getVariant().getHullSpec().getHullSize();
         if (shipSize != null) {
            switch (shipSize) {
               case FRIGATE:
                  stats.getDynamic().getMod("fleet_bombard_cost_reduction").modifyFlat(id, this.bombardmentReductionFrigate);
                  break;
               case DESTROYER:
                  stats.getDynamic().getMod("fleet_bombard_cost_reduction").modifyFlat(id, this.bombardmentReductionDestroyer);
                  break;
               case CRUISER:
                  stats.getDynamic().getMod("fleet_bombard_cost_reduction").modifyFlat(id, this.bombardmentReductionCruiser);
                  break;
               case CAPITAL_SHIP:
                  stats.getDynamic().getMod("fleet_bombard_cost_reduction").modifyFlat(id, this.bombardmentReductionCapital);
                  break;
               case FIGHTER:
                  stats.getDynamic().getMod("fleet_bombard_cost_reduction").modifyFlat(id, 0.0F);
            }
         }
      } else if (!SUPlugin.ENABLE_CUSTOM_STATS_MODE) {
         stats.getMaxBurnLevel().modifyFlat(id, 1.0F);
         stats.getDynamic().getMod("fleet_bombard_cost_reduction").modifyFlat(id, (Float)GENEVA_CORRECTIONS.get(hullSize));
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
      } else {
         this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
      }

      if (!this.toggleGeneralIncompat) {
         for (String blockedMod : ALL_INCOMPAT_IDS) {
            if (ship.getVariant().getHullMods().contains(blockedMod)) {
               ship.getVariant().removeMod(blockedMod);
               SUHullmodDisplayBlockScript.showBlocked(ship);
            }
         }
      }

      if (SUPlugin.HASLUNALIB) {
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableSynchrotonCoreAMCToggle");
      }

      if (ship.getVariant().getSMods().contains("specialsphmod_synchrotoncore_upgrades")
         || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_synchrotoncore_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_synchrotoncore_upgrades");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_synchrotoncore_upgrades") && !this.disableExtraEffect) {
         ship.getVariant().addPermaMod("specialsphmod_synchroton_amfuel_extension");
      }
   }

   public boolean isApplicableToShip(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (SUPlugin.HASLUNALIB) {
            this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
         } else {
            this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
         }

         return this.toggleGeneralIncompat || !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS);
      } else {
         return false;
      }
   }

   public String getUnapplicableReason(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (SUPlugin.HASLUNALIB) {
            this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
         } else {
            this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
         }

         return this.toggleGeneralIncompat
               || !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS)
                  && !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), this.getIncompatibleIds())
            ? super.getUnapplicableReason(ship)
            : "Only one type of special upgrade hullmod can be installed per ship";
      } else {
         return "Unable to locate ship!";
      }
   }

   public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      int status = SUHullmodUpgradeInstaller.isPlayerShip(ship, super.spec.getId());
      return status == 0 ? false : super.canBeAddedOrRemovedNow(ship, marketOrNull, mode);
   }

   public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      int status = SUHullmodUpgradeInstaller.isPlayerShip(ship, super.spec.getId());
      return status == 0 ? "This installation is not applicable to modules" : super.getCanNotBeInstalledNowReason(ship, marketOrNull, mode);
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
      FleetMemberAPI currShips = null;
      if (!isForModSpec && ship != null) {
         if (SUPlugin.HASLUNALIB) {
            this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableCatalyticCoreMRAToggle");
            this.maxBurnBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SYNCHROTON_MAX_BURN_BONUS");
            this.bombardmentReductionFrigate = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_FRIGATE_BONUS");
            this.bombardmentReductionDestroyer = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_DESTROYER_BONUS");
            this.bombardmentReductionCruiser = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_CRUISER_BONUS");
            this.bombardmentReductionCapital = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_CAPITAL_BONUS");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Increases ship's max burn level: %s\n• Reduces fuel cost for orbital bombardment: %s/%s/%s/%s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(SUPlugin.CM_SYNCHROTON_MAX_BURN_BONUS),
                  Misc.getRoundedValue(SUPlugin.CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_FRIGATE_BONUS),
                  Misc.getRoundedValue(SUPlugin.CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_DESTROYER_BONUS),
                  Misc.getRoundedValue(SUPlugin.CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_CRUISER_BONUS),
                  Misc.getRoundedValue(SUPlugin.CM_SYNCHROTON_FLEET_BOMBARDMENT_REDUCTION_CAPITAL_BONUS)
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Increases ship's max burn level: %s\n• Reduces fuel cost for orbital bombardment: %s/%s/%s/%s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(1.0F),
                  Misc.getRoundedValue(30.0F),
                  Misc.getRoundedValue(60.0F),
                  Misc.getRoundedValue(80.0F),
                  Misc.getRoundedValue(100.0F)
               }
            );
         }

         if (playerFleet != null) {
            boolean hasSynchroCoreHMOD = ship.getVariant().hasHullMod("specialsphmod_synchrotoncore_upgrades");
            String fleetPlayerID = ship.getFleetMemberId();
            int fleetFuelCalibrationNum = 0;
            if (fleetPlayerID != null) {
               for (FleetMemberAPI fleetMember : playerFleet.getFleetData().getMembersListCopy()) {
                  if (fleetMember.getId().equals(fleetPlayerID)) {
                     currShips = fleetMember;
                     break;
                  }
               }

               for (FleetMemberAPI fleetFuelCalibrator : playerFleet.getFleetData().getMembersListCopy()) {
                  if (fleetFuelCalibrator.getVariant().hasHullMod("specialsphmod_synchroton_amfuel_extension")) {
                     fleetFuelCalibrationNum++;
                  }

                  if (fleetFuelCalibrationNum == 6) {
                     break;
                  }
               }
            }

            if (currShips != null) {
               float effectMult = fleetFuelCalibrationNum;
               int totalAMFCbonus = (int)(5.0F * effectMult);
               boolean isMothballed = currShips.isMothballed();
               if (!this.disableExtraEffect) {
                  if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
                     tooltip.addSectionHeading("Fleet-Wide Effect", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
                     TooltipMakerAPI extrasystxt = tooltip.beginImageWithText(
                        Global.getSettings().getSpriteName("tooltips", "am_fuel_calibrator"), SUStringCodex.SHU_TOOLTIP_IMG
                     );
                     extrasystxt.addPara(
                        "AM Fuel Calibrator",
                        SUStringCodex.SHU_TOOLTIP_PADZERO,
                        Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                        new String[]{"AM Fuel Calibrator"}
                     );
                     extrasystxt.addPara(
                        "A hacked configuration of Synchroton Core that calibrates the fleet drive system to consume less fuel, allowing for a sustainable and longer interstellar travel. The effect %s with another ship that has Synchro-Fuel Loader and the fleet-wide fuel cost reduction caps at %s.",
                        SUStringCodex.SHU_TOOLTIP_PADZERO,
                        Misc.getHighlightColor(),
                        new String[]{"stacks", Misc.getRoundedValue(30.0F) + "%"}
                     );
                     tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
                     tooltip.addPara(
                        "• Reduces fuel consumption cost: %s\n• Current total fleet-wide bonus: %s",
                        SUStringCodex.SHU_TOOLTIP_PADMAIN,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(5.0F) + "%", totalAMFCbonus + "%"}
                     );
                  }

                  if (hasSynchroCoreHMOD && isMothballed) {
                     tooltip.addPara(
                        "%s",
                        SUStringCodex.SHU_TOOLTIP_PADMAIN,
                        Misc.getNegativeHighlightColor(),
                        new String[]{"This ship is currently mothballed, preventing the anti-matter fuel calibrator from functioning."}
                     );
                  } else if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
                     tooltip.addPara(
                           "Press and hold [%s] to view its fleet-wide effect.",
                           SUStringCodex.SHU_TOOLTIP_PADMAIN,
                           Misc.getGrayColor(),
                           Misc.getStoryBrightColor(),
                           new String[]{"F1"}
                        )
                        .setAlignment(Alignment.MID);
                  }
               }

               boolean disableDestruction = SUPlugin.DISABLE_ITEMDESTRUCTION;
               if (SUPlugin.HASLUNALIB) {
                  this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
                  disableDestruction = LunaSettings.getBoolean("mayu_specialupgrades", "shu_itemdestructionToggle");
               } else {
                  this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
               }

               if (!this.toggleGeneralIncompat) {
                  tooltip.addSectionHeading(
                     "Incompatibilities",
                     SUStringCodex.SHU_HULLMOD_NEGATIVE_TEXT_COLOR,
                     SUStringCodex.SHU_HULLMOD_NEGATIVE_HEADER_BG,
                     Alignment.MID,
                     SUStringCodex.SHU_TOOLTIP_PADMAIN
                  );
                  TooltipMakerAPI text = tooltip.beginImageWithText(
                     Global.getSettings().getSpriteName("tooltips", "hullmod_incompatible"), SUStringCodex.SHU_TOOLTIP_IMG
                  );
                  text.addPara(
                     "Not compatible with %s",
                     SUStringCodex.SHU_TOOLTIP_PADZERO,
                     Misc.getNegativeHighlightColor(),
                     new String[]{"Other Special Upgrade Hullmods"}
                  );
                  tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
               }

               if (!disableDestruction) {
                  tooltip.addPara(
                     "%s",
                     SUStringCodex.SHU_TOOLTIP_PADNOTE,
                     Misc.getGrayColor(),
                     new String[]{
                        "This hullmod counts as a special upgrade and it can work in conjunction with Armament Support System hullmod. Furthermore, the item is forever lost when the ship gets destroyed in combat."
                     }
                  );
               } else {
                  tooltip.addPara(
                     "%s",
                     SUStringCodex.SHU_TOOLTIP_PADNOTE,
                     Misc.getGrayColor(),
                     new String[]{"This hullmod counts as a special upgrade and it can work in conjunction with Armament Support System hullmod."}
                  );
               }
            }
         }
      }
   }

   static {
      GENEVA_CORRECTIONS.put(HullSize.FIGHTER, 0.0F);
      GENEVA_CORRECTIONS.put(HullSize.FRIGATE, 30.0F);
      GENEVA_CORRECTIONS.put(HullSize.DESTROYER, 60.0F);
      GENEVA_CORRECTIONS.put(HullSize.CRUISER, 80.0F);
      GENEVA_CORRECTIONS.put(HullSize.CAPITAL_SHIP, 100.0F);
   }
}
