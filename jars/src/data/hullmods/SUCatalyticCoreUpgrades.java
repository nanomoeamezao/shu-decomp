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
import lunalib.lunaSettings.LunaSettings;
import org.lwjgl.input.Keyboard;

public class SUCatalyticCoreUpgrades extends BaseHullMod {
   public static final String DATA_PREFIX = "catalytic_core_shu_check_";
   public static final String ITEM = "catalytic_core";
   private static final float MALFUNCTION_REDUCTION = 0.75F;
   private static final float DEGRADE_REDUCTION_PERCENT = 30.0F;
   private static final float CR_PEAK_BONUS = 40.0F;
   private static final float SUPPLY_REDUCTION = 0.5F;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_CATALYTICCOREHMOD_EXTRA_EFFECT;
   boolean disableEfficiencyOverhaulIncompat = SUPlugin.DISABLE_EFFICIENCYOVERHAUL_INCOMPATIBILITY;
   float malfunctionReduction = SUPlugin.CM_CATALYTIC_CORE_MALFUNCTION_REDUCTION;
   float crDegradeReduction = SUPlugin.CM_CATALYTIC_DEGRADE_REDUCTION_PERCENT;
   float crPeakBonus = SUPlugin.CM_CATALYTIC_CR_PEAK_BONUS;
   float supplyReduction = SUPlugin.CM_CATALYTIC_SUPPLY_REDUCTION;
   boolean toggleGeneralIncompat;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};


   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_catalyticcore_upgrades") ? ALL_INCOMPAT_IDS : null;
   }

   public CargoStackAPI getRequiredItem() {
      return Global.getSettings().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData("catalytic_core", null), null);
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
            TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("catalytic_core").getIconName(), 20.0F);
            text.addPara("Requires " + aOrAn + " %s to install.", opad, color, new String[]{name});
            tooltip.addImageWithText(5.0F);
         } else if (currentVariant != null && member != null) {
            if (currentVariant.hasHullMod(this.spec.getId())) {
               if (!currentVariant.getHullSpec().getBuiltInMods().contains(this.spec.getId())) {
                  Color color = Misc.getPositiveHighlightColor();
                  TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("catalytic_core").getIconName(), 20.0F);
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

               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("catalytic_core").getIconName(), 20.0F);
               text3.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", color, opad);
               tooltip.addImageWithText(5.0F);
            }
         }
      }
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.malfunctionReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CATALYTIC_CORE_MALFUNCTION_REDUCTION");
         this.crDegradeReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CATALYTIC_DEGRADE_REDUCTION_PERCENT");
         this.crPeakBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CATALYTIC_CR_PEAK_BONUS");
         this.supplyReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CATALYTIC_SUPPLY_REDUCTION");
      }

      if (this.enableCustomSM) {
         stats.getWeaponMalfunctionChance().modifyMult(id, 1.0F - this.malfunctionReduction / 100.0F);
         stats.getEngineMalfunctionChance().modifyMult(id, 1.0F - this.malfunctionReduction / 100.0F);
         stats.getCriticalMalfunctionChance().modifyMult(id, 1.0F - this.malfunctionReduction / 100.0F);
         stats.getCRLossPerSecondPercent().modifyMult(id, 1.0F - this.crDegradeReduction / 100.0F);
         stats.getPeakCRDuration().modifyPercent(id, this.crPeakBonus);
         stats.getSuppliesPerMonth().modifyMult(id, 1.0F - this.supplyReduction / 100.0F);
         stats.getSuppliesToRecover().modifyMult(id, (1.0F - this.supplyReduction / 100.0F) / 2.0F);
      } else if (!this.enableCustomSM) {
         stats.getWeaponMalfunctionChance().modifyMult(id, 0.75F);
         stats.getEngineMalfunctionChance().modifyMult(id, 0.75F);
         stats.getCriticalMalfunctionChance().modifyMult(id, 0.75F);
         stats.getCRLossPerSecondPercent().modifyMult(id, 0.7F);
         stats.getPeakCRDuration().modifyPercent(id, 40.0F);
         stats.getSuppliesPerMonth().modifyMult(id, 0.5F);
         stats.getSuppliesToRecover().modifyMult(id, 0.25F);
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
         this.disableEfficiencyOverhaulIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableEfficiencyOverhaulIncompatibilityToggle");
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableCatalyticCoreMRAToggle");
      }

      if (!this.disableEfficiencyOverhaulIncompat && ship.getVariant().getHullMods().contains("efficiency_overhaul")) {
         ship.getVariant().removeMod("efficiency_overhaul");
         SUHullmodDisplayBlockScript.showBlocked(ship);
      }

      if (ship.getVariant().getSMods().contains("specialsphmod_catalyticcore_upgrades")
         || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_catalyticcore_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_catalyticcore_upgrades");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_catalyticcore_upgrades")
         && !this.disableExtraEffect
         && ship.getVariant().getHullSize() == HullSize.CAPITAL_SHIP) {
         ship.getVariant().addPermaMod("specialsphmod_catalytic_repair_autofactory");
      }
   }

   public boolean isApplicableToShip(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (SUPlugin.HASLUNALIB) {
            this.disableEfficiencyOverhaulIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableEfficiencyOverhaulIncompatibilityToggle");
         }

         if (!this.disableEfficiencyOverhaulIncompat && ship.getVariant().hasHullMod("efficiency_overhaul")) {
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
            this.disableEfficiencyOverhaulIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableEfficiencyOverhaulIncompatibilityToggle");
         }

         if (!this.disableEfficiencyOverhaulIncompat && ship.getVariant().hasHullMod("efficiency_overhaul")) {
            return "Incompatible with Efficiency Overhaul";
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
      CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
      FleetMemberAPI currShips = null;
      if (!isForModSpec && ship != null) {
         if (SUPlugin.HASLUNALIB) {
            this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableCatalyticCoreMRAToggle");
            this.disableEfficiencyOverhaulIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableEfficiencyOverhaulIncompatibilityToggle");
            this.malfunctionReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CATALYTIC_CORE_MALFUNCTION_REDUCTION");
            this.crDegradeReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CATALYTIC_DEGRADE_REDUCTION_PERCENT");
            this.crPeakBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CATALYTIC_CR_PEAK_BONUS");
            this.supplyReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CATALYTIC_SUPPLY_REDUCTION");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Reduces chance of ship's critical malfunctions: %s\n• Reduces the CR degradation rate: %s\n• Increases maximum peak operating time: %s\n• Decreases the supply usage: %s ",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(this.malfunctionReduction) + "%",
                  Misc.getRoundedValue(this.crDegradeReduction) + "%",
                  Misc.getRoundedValue(this.crPeakBonus) + "%",
                  Misc.getRoundedValue(this.supplyReduction) + "%"
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Reduces chance of ship's critical malfunctions: %s\n• Reduces the CR degradation rate: %s\n• Increases maximum peak operating time: %s\n• Decreases the supply usage: %s ",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(25.0F) + "%", Misc.getRoundedValue(30.0F) + "%", Misc.getRoundedValue(40.0F) + "%", Misc.getRoundedValue(50.0F) + "%"
               }
            );
         }

         if (playerFleet != null) {
            if (!this.disableExtraEffect) {
               boolean hasCatalyticCoreHMOD = ship.getVariant().hasHullMod("specialsphmod_catalyticcore_upgrades");
               boolean isCapitalShip = ship.getVariant().getHullSize() == HullSize.CAPITAL_SHIP;
               String fleetPlayerID = ship.getFleetMemberId();
               int fleetMobileRepairNum = 0;
               if (fleetPlayerID != null) {
                  for (FleetMemberAPI fleetMember : playerFleet.getFleetData().getMembersListCopy()) {
                     if (fleetMember.getId().equals(fleetPlayerID)) {
                        currShips = fleetMember;
                        break;
                     }
                  }

                  for (FleetMemberAPI fleetAutofactory : playerFleet.getFleetData().getMembersListCopy()) {
                     if (fleetAutofactory.getVariant().hasHullMod("specialsphmod_catalytic_repair_autofactory")) {
                        fleetMobileRepairNum++;
                     }

                     if (fleetMobileRepairNum == 4) {
                        break;
                     }
                  }
               }

               if (currShips == null) {
                  return;
               }

               float effectMult = fleetMobileRepairNum;
               int totalRAbonus = (int)(50.0F * effectMult);
               boolean isMothballed = currShips.isMothballed();
               if (hasCatalyticCoreHMOD && isCapitalShip && Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
                  tooltip.addSectionHeading("Fleet-Wide Effect", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
                  TooltipMakerAPI extrasystxt = tooltip.beginImageWithText(
                     Global.getSettings().getSpriteName("tooltips", "mobile_autofactory"), SUStringCodex.SHU_TOOLTIP_IMG
                  );
                  extrasystxt.addPara(
                     "Mobile Repair Autofactory",
                     SUStringCodex.SHU_TOOLTIP_PADZERO,
                     Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                     new String[]{"Mobile Repair Autofactory"}
                  );
                  extrasystxt.addPara(
                     "This vessel utilizes the fleet-wide repair function of the installed Catalytic Core. The effect %s with another ship that has Maintenance Optimizer and the repair bonus caps at %s.",
                     SUStringCodex.SHU_TOOLTIP_PADZERO,
                     Misc.getHighlightColor(),
                     new String[]{"stacks", Misc.getRoundedValue(200.0F) + "%"}
                  );
                  tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
                  tooltip.addPara(
                     "• Increases fleet CR recovery rate: %s\n• Improves fleet repair speed: %s\n• Current total fleet-wide bonus: %s",
                     SUStringCodex.SHU_TOOLTIP_PADMAIN,
                     SUStringCodex.SHU_TOOLTIP_GREEN,
                     new String[]{Misc.getRoundedValue(50.0F) + "%", Misc.getRoundedValue(50.0F) + "%", totalRAbonus + "%"}
                  );
               }

               if (hasCatalyticCoreHMOD && isMothballed) {
                  tooltip.addPara(
                     "%s",
                     SUStringCodex.SHU_TOOLTIP_PADMAIN,
                     Misc.getNegativeHighlightColor(),
                     new String[]{"This ship is currently mothballed, preventing the mobile repair autofactory from functioning."}
                  );
               } else if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F1")) && hasCatalyticCoreHMOD && isCapitalShip) {
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
               if (!this.disableEfficiencyOverhaulIncompat) {
                  text.addPara(
                     "Not compatible with %s, %s",
                     SUStringCodex.SHU_TOOLTIP_PADZERO,
                     Misc.getNegativeHighlightColor(),
                     new String[]{"Efficiency Overhaul", "Other Special Upgrade Hullmods"}
                  );
               } else if (this.disableEfficiencyOverhaulIncompat) {
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
}
