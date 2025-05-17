package data.hullmods;

import com.fs.starfarer.api.GameState;
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
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.everyframe.SUHullmodDisplayBlockScript;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import java.lang.invoke.StringConcatFactory;
import java.util.HashMap;
import java.util.Map;
import lunalib.lunaSettings.LunaSettings;
import org.lwjgl.input.Keyboard;

public class SUFullereneSpoolUpgrades extends BaseHullMod {
   public static final String DATA_PREFIX = "fullerene_spool_shu_check_";
   public static final String ITEM = "fullerene_spool";
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_FULLERENESPOOLHMOD_EXTRA_EFFECT;
   boolean disableSurveyingEquipmentIncompat = SUPlugin.DISABLE_SURVEYINGEQUIPMENT_INCOMPATIBILITY;
   float surveyReductionFrigate = SUPlugin.CM_FULLLERENESPOOL_SURVEY_REDUCTION_FRIGATE;
   float surveyReductionDestroyer = SUPlugin.CM_FULLLERENESPOOL_SURVEY_REDUCTION_DESTROYER;
   float surveyReductionCruiser = SUPlugin.CM_FULLLERENESPOOL_SURVEY_REDUCTION_CRUISER;
   float surveyReductionCapital = SUPlugin.CM_FULLLERENESPOOL_SURVEY_REDUCTION_CAPITAL;
   float salvageBonusFrigate = SUPlugin.CM_FULLLERENESPOOL_SALVAGE_BONUS_FRIGATE;
   float salvageBonusDestroyer = SUPlugin.CM_FULLLERENESPOOL_SALVAGE_BONUS_DESTROYER;
   float salvageBonusCruiser = SUPlugin.CM_FULLLERENESPOOL_SALVAGE_BONUS_CRUISER;
   float salvageBonusCapital = SUPlugin.CM_FULLLERENESPOOL_SALVAGE_BONUS_CAPITAL;
   float rareLootBonusFrigate = SUPlugin.CM_FULLLERENESPOOL_RARE_LOOT_BONUS_FRIGATE;
   float rareLootBonusDestroyer = SUPlugin.CM_FULLLERENESPOOL_RARE_LOOT_BONUS_DESTROYER;
   float rareLootBonusCruiser = SUPlugin.CM_FULLLERENESPOOL_RARE_LOOT_BONUS_CRUISER;
   float rareLootBonusCapital = SUPlugin.CM_FULLLERENESPOOL_RARE_LOOT_BONUS_CAPITAL;
   boolean toggleGeneralIncompat;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};

   private static final Map surveyVal = new HashMap();
   private static final Map salvageVal = new HashMap();
   private static final Map rareBonus = new HashMap();

   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_fullerenespool_upgrades") ? ALL_INCOMPAT_IDS : null;
   }

   public CargoStackAPI getRequiredItem() {
      return Global.getSettings().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData("fullerene_spool", null), null);
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
            TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("fullerene_spool").getIconName(), 20.0F);
            text.addPara("Requires " + aOrAn + " %s to install.", opad, color, new String[]{name});
            tooltip.addImageWithText(5.0F);
         } else if (currentVariant != null && member != null) {
            if (currentVariant.hasHullMod(this.spec.getId())) {
               if (!currentVariant.getHullSpec().getBuiltInMods().contains(this.spec.getId())) {
                  Color color = Misc.getPositiveHighlightColor();
                  TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("fullerene_spool").getIconName(), 20.0F);
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

               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("fullerene_spool").getIconName(), 20.0F);
               text3.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", color, opad);
               tooltip.addImageWithText(5.0F);
            }
         }
      }
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.surveyReductionFrigate = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_SURVEY_REDUCTION_FRIGATE");
         this.surveyReductionDestroyer = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_SURVEY_REDUCTION_DESTROYER");
         this.surveyReductionCruiser = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_SURVEY_REDUCTION_CRUISER");
         this.surveyReductionCapital = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_SURVEY_REDUCTION_CAPITAL");
         this.salvageBonusFrigate = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_SALVAGE_BONUS_FRIGATE");
         this.salvageBonusDestroyer = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_SALVAGE_BONUS_DESTROYER");
         this.salvageBonusCruiser = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_SALVAGE_BONUS_CRUISER");
         this.salvageBonusCapital = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_SALVAGE_BONUS_CAPITAL");
         this.rareLootBonusFrigate = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_RARE_LOOT_BONUS_FRIGATE");
         this.rareLootBonusDestroyer = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_RARE_LOOT_BONUS_DESTROYER");
         this.rareLootBonusCruiser = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_RARE_LOOT_BONUS_CRUISER");
         this.rareLootBonusCapital = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_RARE_LOOT_BONUS_CAPITAL");
      }

      if (this.enableCustomSM) {
         HullSize shipSize = stats.getVariant().getHullSpec().getHullSize();
         if (shipSize != null) {
            switch (shipSize) {
               case FRIGATE:
                  stats.getDynamic().getMod(Stats.getSurveyCostReductionId("heavy_machinery")).modifyFlat(id, this.surveyReductionFrigate);
                  stats.getDynamic().getMod(Stats.getSurveyCostReductionId("supplies")).modifyFlat(id, this.surveyReductionFrigate);
                  stats.getDynamic().getMod("salvage_value_bonus_ship").modifyFlat(id, this.salvageBonusFrigate * 0.01F);
                  stats.getDynamic().getStat("salvage_value_bonus_fleet").modifyFlat(id, this.rareLootBonusFrigate * 0.01F);
                  break;
               case DESTROYER:
                  stats.getDynamic().getMod(Stats.getSurveyCostReductionId("heavy_machinery")).modifyFlat(id, this.surveyReductionDestroyer);
                  stats.getDynamic().getMod(Stats.getSurveyCostReductionId("supplies")).modifyFlat(id, this.surveyReductionDestroyer);
                  stats.getDynamic().getMod("salvage_value_bonus_ship").modifyFlat(id, this.salvageBonusDestroyer * 0.01F);
                  stats.getDynamic().getStat("salvage_value_bonus_fleet").modifyFlat(id, this.rareLootBonusDestroyer * 0.01F);
                  break;
               case CRUISER:
                  stats.getDynamic().getMod(Stats.getSurveyCostReductionId("heavy_machinery")).modifyFlat(id, this.surveyReductionCruiser);
                  stats.getDynamic().getMod(Stats.getSurveyCostReductionId("supplies")).modifyFlat(id, this.surveyReductionCruiser);
                  stats.getDynamic().getMod("salvage_value_bonus_ship").modifyFlat(id, this.salvageBonusCruiser * 0.01F);
                  stats.getDynamic().getStat("salvage_value_bonus_fleet").modifyFlat(id, this.rareLootBonusCruiser * 0.01F);
                  break;
               case CAPITAL_SHIP:
                  stats.getDynamic().getMod(Stats.getSurveyCostReductionId("heavy_machinery")).modifyFlat(id, this.surveyReductionCapital);
                  stats.getDynamic().getMod(Stats.getSurveyCostReductionId("supplies")).modifyFlat(id, this.surveyReductionCapital);
                  stats.getDynamic().getMod("salvage_value_bonus_ship").modifyFlat(id, this.salvageBonusCapital * 0.01F);
                  stats.getDynamic().getStat("salvage_value_bonus_fleet").modifyFlat(id, this.rareLootBonusCapital * 0.01F);
                  break;
               case FIGHTER:
                  stats.getDynamic().getMod(Stats.getSurveyCostReductionId("heavy_machinery")).modifyFlat(id, 0.0F);
                  stats.getDynamic().getMod(Stats.getSurveyCostReductionId("supplies")).modifyFlat(id, 0.0F);
                  stats.getDynamic().getMod("salvage_value_bonus_ship").modifyFlat(id, 0.0F);
                  stats.getDynamic().getStat("salvage_value_bonus_fleet").modifyFlat(id, 0.0F);
            }
         }
      } else if (!this.enableCustomSM) {
         stats.getDynamic().getMod(Stats.getSurveyCostReductionId("heavy_machinery")).modifyFlat(id, (Float)surveyVal.get(hullSize));
         stats.getDynamic().getMod(Stats.getSurveyCostReductionId("supplies")).modifyFlat(id, (Float)surveyVal.get(hullSize));
         stats.getDynamic().getMod("salvage_value_bonus_ship").modifyFlat(id, (Float)salvageVal.get(hullSize));
         stats.getDynamic().getStat("salvage_value_bonus_fleet").modifyFlat(id, (Float)rareBonus.get(hullSize) * 0.01F);
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
         this.disableSurveyingEquipmentIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableSurveyingEquipmentIncompatibilityToggle");
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableFullereneSpoolFTCToggle");
      }

      if (!this.disableSurveyingEquipmentIncompat && ship.getVariant().getHullMods().contains("surveying_equipment")) {
         ship.getVariant().removeMod("surveying_equipment");
         SUHullmodDisplayBlockScript.showBlocked(ship);
      }

      if (ship.getVariant().getHullMods().contains("tow_cable")) {
         ship.getVariant().removeMod("tow_cable");
         SUHullmodDisplayBlockScript.showBlocked(ship);
      }

      if (ship.getVariant().getHullMods().contains("strikeCraft")) {
         ship.getVariant().removeMod("strikeCraft");
         SUHullmodDisplayBlockScript.showBlocked(ship);
      }

      if (ship.getVariant().getSMods().contains("specialsphmod_fullerenespool_upgrades")
         || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_fullerenespool_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_fullerenespool_upgrades");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_fullerenespool_upgrades")
         && !this.disableExtraEffect
         && (ship.getVariant().getHullSize() == HullSize.FRIGATE || ship.getVariant().getHullSize() == HullSize.DESTROYER)) {
         ship.getVariant().addPermaMod("specialsphmod_fullerene_tow_cable");
      }
   }

   public boolean isApplicableToShip(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (SUPlugin.HASLUNALIB) {
            this.disableSurveyingEquipmentIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableSurveyingEquipmentIncompatibilityToggle");
         }

         if (!this.disableSurveyingEquipmentIncompat && ship.getVariant().hasHullMod("surveying_equipment")) {
            return false;
         } else if (ship.getVariant().hasHullMod("tow_cable")) {
            return false;
         } else if (ship.getVariant().hasHullMod("strikeCraft")) {
            return false;
         } else {
            if (SUPlugin.HASLUNALIB) {
               this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
            } else {
               this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
            }

            return this.toggleGeneralIncompat || !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS);
         }
      } else {
         return false;
      }
   }

   public String getUnapplicableReason(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (SUPlugin.HASLUNALIB) {
            this.disableSurveyingEquipmentIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableSurveyingEquipmentIncompatibilityToggle");
         }

         if (!this.disableSurveyingEquipmentIncompat && ship.getVariant().hasHullMod("surveying_equipment")) {
            return "Incompatible with Surveying Equipment";
         } else if (ship.getVariant().hasHullMod("tow_cable")) {
            return "Incompatible with Monofilament Tow Cable";
         } else if (ship.getVariant().hasHullMod("strikeCraft")) {
            return "Not applicable to strikecrafts";
         } else {
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
         }
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
      if (!isForModSpec && ship != null) {
         if (Global.getSettings().getCurrentState() != GameState.TITLE) {
            if (SUPlugin.HASLUNALIB) {
               this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
               this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableFullereneSpoolFTCToggle");
               this.disableSurveyingEquipmentIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableSurveyingEquipmentIncompatibilityToggle");
               this.surveyReductionFrigate = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_SURVEY_REDUCTION_FRIGATE");
               this.surveyReductionDestroyer = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_SURVEY_REDUCTION_DESTROYER");
               this.surveyReductionCruiser = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_SURVEY_REDUCTION_CRUISER");
               this.surveyReductionCapital = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_SURVEY_REDUCTION_CAPITAL");
               this.salvageBonusFrigate = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_SALVAGE_BONUS_FRIGATE");
               this.salvageBonusDestroyer = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_SALVAGE_BONUS_DESTROYER");
               this.salvageBonusCruiser = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_SALVAGE_BONUS_CRUISER");
               this.salvageBonusCapital = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_SALVAGE_BONUS_CAPITAL");
               this.rareLootBonusFrigate = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_RARE_LOOT_BONUS_FRIGATE");
               this.rareLootBonusDestroyer = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_RARE_LOOT_BONUS_DESTROYER");
               this.rareLootBonusCruiser = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_RARE_LOOT_BONUS_CRUISER");
               this.rareLootBonusCapital = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FULLLERENESPOOL_RARE_LOOT_BONUS_CAPITAL");
            }

            if (this.enableCustomSM) {
               tooltip.addSectionHeading("Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
               tooltip.addPara(
                  "• Reduces survey operation cost: %s/%s/%s/%s\n• Increases resources gained from salvage: %s/%s/%s/%s\n• Increases chance of rare loot from salvage: %s/%s/%s/%s",
                  SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                  SUStringCodex.SHU_TOOLTIP_GREEN,
                  new String[]{
                     Misc.getRoundedValue(this.surveyReductionFrigate),
                     Misc.getRoundedValue(this.surveyReductionDestroyer),
                     Misc.getRoundedValue(this.surveyReductionCruiser),
                     Misc.getRoundedValue(this.surveyReductionCapital),
                     Misc.getRoundedValue(this.salvageBonusFrigate) + "%",
                     Misc.getRoundedValue(this.salvageBonusDestroyer) + "%",
                     Misc.getRoundedValue(this.salvageBonusCruiser) + "%",
                     Misc.getRoundedValue(this.salvageBonusCapital) + "%",
                     Misc.getRoundedValue(this.rareLootBonusFrigate) + "%",
                     Misc.getRoundedValue(this.rareLootBonusDestroyer) + "%",
                     Misc.getRoundedValue(this.rareLootBonusCruiser) + "%",
                     Misc.getRoundedValue(this.rareLootBonusCapital) + "%"
                  }
               );
            } else if (!this.enableCustomSM) {
               tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               tooltip.addPara(
                  "• Reduces survey operation cost: %s/%s/%s/%s\n• Increases resources gained from salvage: %s/%s/%s/%s\n• Increases chance of rare loot from salvage: %s/%s/%s/%s",
                  SUStringCodex.SHU_TOOLTIP_PADMAIN,
                  SUStringCodex.SHU_TOOLTIP_GREEN,
                  new String[]{
                     Misc.getRoundedValue(10.0F),
                     Misc.getRoundedValue(20.0F),
                     Misc.getRoundedValue(30.0F),
                     Misc.getRoundedValue(50.0F),
                     Misc.getRoundedValue(5.0F) + "%",
                     Misc.getRoundedValue(10.0F) + "%",
                     Misc.getRoundedValue(15.0F) + "%",
                     Misc.getRoundedValue(20.0F) + "%",
                     Misc.getRoundedValue(2.0F) + "%",
                     Misc.getRoundedValue(4.0F) + "%",
                     Misc.getRoundedValue(6.0F) + "%",
                     Misc.getRoundedValue(8.0F) + "%"
                  }
               );
            }

            CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
            int machinery = (int)Misc.getFleetwideTotalMod(fleet, Stats.getSurveyCostReductionId("heavy_machinery"), 0.0F, ship);
            int supplies = (int)Misc.getFleetwideTotalMod(fleet, Stats.getSurveyCostReductionId("supplies"), 0.0F, ship);
            TooltipMakerAPI txtsvy = tooltip.beginImageWithText(Global.getSettings().getSpriteName("tooltips", "survey_gear"), 30.0F);
            txtsvy.addPara(
               "Your fleet's total planetary survey cost reduction are %s supplies and %s heavy machinery. This stacks additively together with other sources.",
               SUStringCodex.SHU_TOOLTIP_PADZERO,
               Misc.getHighlightColor(),
               new String[]{supplies + "", machinery + ""}
            );
            tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
            if (!this.disableExtraEffect) {
               if ((
                     ship.getVariant().hasHullMod("specialsphmod_fullerene_tow_cable") && ship.getHullSize() == HullSize.FRIGATE
                        || ship.getHullSize() == HullSize.DESTROYER
                  )
                  && Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
                  tooltip.addSectionHeading("Additional Effect", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
                  TooltipMakerAPI extrasystxt = tooltip.beginImageWithText(
                     Global.getSettings().getSpriteName("tooltips", "fullerene_cable"), SUStringCodex.SHU_TOOLTIP_IMG
                  );
                  extrasystxt.addPara(
                     "Fullerene Tow Cable",
                     SUStringCodex.SHU_TOOLTIP_PADZERO,
                     Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                     new String[]{"Fullerene Tow Cable"}
                  );
                  extrasystxt.addPara(
                     "The %s will tow the slowest ship in your fleet and increase its maximum burn speed by %s. This will also reduce the fuel consumption of towed mothballed ship by %s. Due to the specialized equipment needed to attach the spool to the target vessel, this effect does not stack.",
                     SUStringCodex.SHU_TOOLTIP_PADZERO,
                     SUStringCodex.SHU_TOOLTIP_GREEN,
                     new String[]{
                        ship.getName(),
                        Misc.getRoundedValue(1.0F),
                        Misc.getRoundedValue(20.0F) + "%"
                     }
                  );
                  tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
               }

               if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))
                     && ship.getVariant().hasHullMod("specialsphmod_fullerene_tow_cable")
                     && ship.getHullSize() == HullSize.FRIGATE
                  || !Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))
                     && ship.getVariant().hasHullMod("specialsphmod_fullerene_tow_cable")
                     && ship.getHullSize() == HullSize.DESTROYER) {
                  tooltip.addPara(
                        "Press and hold [%s] to view its additional effect.",
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
               if (!this.disableSurveyingEquipmentIncompat) {
                  text.addPara(
                     "Not compatible with %s, %s",
                     SUStringCodex.SHU_TOOLTIP_PADZERO,
                     Misc.getNegativeHighlightColor(),
                     new String[]{"Surveying Equipment", "Other Special Upgrade Hullmods"}
                  );
               } else if (this.disableSurveyingEquipmentIncompat) {
                  text.addPara(
                     "Not compatible with %s",
                     SUStringCodex.SHU_TOOLTIP_PADZERO,
                     Misc.getNegativeHighlightColor(),
                     new String[]{"Other Special Upgrade Hullmods"}
                  );
               }

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

   static {
      surveyVal.put(HullSize.FIGHTER, 0.0F);
      surveyVal.put(HullSize.FRIGATE, 10.0F);
      surveyVal.put(HullSize.DESTROYER, 20.0F);
      surveyVal.put(HullSize.CRUISER, 30.0F);
      surveyVal.put(HullSize.CAPITAL_SHIP, 50.0F);
      salvageVal.put(HullSize.FIGHTER, 0.0F);
      salvageVal.put(HullSize.FRIGATE, 0.05F);
      salvageVal.put(HullSize.DESTROYER, 0.1F);
      salvageVal.put(HullSize.CRUISER, 0.15F);
      salvageVal.put(HullSize.CAPITAL_SHIP, 0.2F);
      rareBonus.put(HullSize.FIGHTER, 0.0F);
      rareBonus.put(HullSize.FRIGATE, 2.0F);
      rareBonus.put(HullSize.DESTROYER, 4.0F);
      rareBonus.put(HullSize.CRUISER, 6.0F);
      rareBonus.put(HullSize.CAPITAL_SHIP, 8.0F);
   }
}
