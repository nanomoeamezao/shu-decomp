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

public class SUAuroranModularPurifierUpgrades extends BaseHullMod {
   public static final String DATA_PREFIX_COLLAB = "modular_purifier_shu_check_";
   public static final String ITEM = "uaf_modular_purifier";
   private static final float MIN_CREW_REDUCTION = 30.0F;
   private static final float SUPPLIES_TO_RECOVER = 60.0F;
   private static final float DEPLOYMENT_POINTS_REDUC = 5.0F;
   private static final int MAX_FLEET_MITIGATOR = 5;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_MODULARPURIFIER_EXTRA_EFFECT;
   float deploymentCostReduction = SUPlugin.CM_UAF_MODULARPURIFIER_DEPLOYMENT_COST_REDUCTION_REDUCTION;
   float minCrewRequirementReduction = SUPlugin.CM_UAF_MODULARPURIFIER_MIN_CREW_REQUIREMENT_REDUCTION;
   float supplyCostRecoverReduction = SUPlugin.CM_UAF_MODULARPURIFIER_SUPPLY_COST_RECOVER_FROM_DEPLOYMENT_REDUCTION;
   boolean toggleGeneralIncompat;
   boolean isUAFPresent = Global.getSettings().getModManager().isModEnabled("uaf");
   private static final Map HULLDP = new HashMap();
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};


   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_uaf_modularpurifier_upgrades") ? ALL_INCOMPAT_IDS : null;
   }

   public CargoStackAPI getRequiredItem() {
      return this.isUAFPresent ? Global.getSettings().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData("uaf_modular_purifier", null), null) : null;
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
            TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("uaf_modular_purifier").getIconName(), 20.0F);
            text.addPara("Requires " + aOrAn + " %s to install.", opad, color, new String[]{name});
            tooltip.addImageWithText(5.0F);
         } else if (currentVariant != null && member != null) {
            if (currentVariant.hasHullMod(this.spec.getId())) {
               if (!currentVariant.getHullSpec().getBuiltInMods().contains(this.spec.getId())) {
                  Color color = Misc.getPositiveHighlightColor();
                  TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("uaf_modular_purifier").getIconName(), 20.0F);
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

               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("uaf_modular_purifier").getIconName(), 20.0F);
               text3.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", color, opad);
               tooltip.addImageWithText(5.0F);
            }
         }
      }
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.minCrewRequirementReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_MODULARPURIFIER_MIN_CREW_REQUIREMENT_REDUCTION");
         this.supplyCostRecoverReduction = LunaSettings.getFloat(
            "mayu_specialupgrades", "LUNA_CM_UAF_MODULARPURIFIER_SUPPLY_COST_RECOVER_FROM_DEPLOYMENT_REDUCTION"
         );
         this.deploymentCostReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_MODULARPURIFIER_DEPLOYMENT_COST_REDUCTION_REDUCTION");
      }

      if (this.enableCustomSM) {
         stats.getMinCrewMod().modifyPercent(id, -this.minCrewRequirementReduction);
         stats.getSuppliesToRecover().modifyMult(id, 1.0F - this.supplyCostRecoverReduction / 100.0F);
         stats.getDynamic().getMod("deployment_points_mod").modifyPercent(id, -this.deploymentCostReduction);
      } else if (!this.enableCustomSM) {
         stats.getMinCrewMod().modifyPercent(id, -30.0F);
         stats.getSuppliesToRecover().modifyMult(id, 0.59999996F);
         stats.getDynamic().getMod("deployment_points_mod").modifyPercent(id, -(Float)HULLDP.get(hullSize));
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
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableModularPurifierMMToggle");
      }

      if (ship.getVariant().getSMods().contains("specialsphmod_uaf_modularpurifier_upgrades")
         || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_uaf_modularpurifier_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_uaf_modularpurifier_upgrades");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_uaf_modularpurifier_upgrades") && !this.disableExtraEffect) {
         ship.getVariant().addPermaMod("specialsphmod_uaf_modularpurifier_extension");
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
      if (status == 0) {
         return false;
      } else {
         return !this.isUAFPresent && status != 2 && !SUHullmodUpgradeInstaller.playerHasSpecialItem("uaf_modular_purifier")
            ? false
            : super.canBeAddedOrRemovedNow(ship, marketOrNull, mode);
      }
   }

   public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      int status = SUHullmodUpgradeInstaller.isPlayerShip(ship, super.spec.getId());
      if (status == 0) {
         return "This installation is not applicable to modules";
      } else {
         return !this.isUAFPresent && status != 2 && !SUHullmodUpgradeInstaller.playerHasCommodity("uaf_modular_purifier")
            ? "Installation requires [Auroran Modular Purifier] (1)"
            : super.getCanNotBeInstalledNowReason(ship, marketOrNull, mode);
      }
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
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableModularPurifierMMToggle");
            this.minCrewRequirementReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_MODULARPURIFIER_MIN_CREW_REQUIREMENT_REDUCTION");
            this.supplyCostRecoverReduction = LunaSettings.getFloat(
               "mayu_specialupgrades", "LUNA_CM_UAF_MODULARPURIFIER_SUPPLY_COST_RECOVER_FROM_DEPLOYMENT_REDUCTION"
            );
            this.deploymentCostReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_MODULARPURIFIER_DEPLOYMENT_COST_REDUCTION_REDUCTION");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Reduces minimum crew requirement: %s\n• Reduces supply cost to recover from deployment: %s\n• Reduces deployment cost: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(this.minCrewRequirementReduction) + "%",
                  Misc.getRoundedValue(this.supplyCostRecoverReduction) + "%",
                  Misc.getRoundedValue(this.deploymentCostReduction) + "%"
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Reduces minimum crew requirement: %s\n• Reduces supply cost to recover from deployment: %s\n• Reduces deployment cost: %s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{Misc.getRoundedValue(30.0F) + "%", Misc.getRoundedValue(40.0F) + "%", Misc.getRoundedValue(5.0F) + "%"}
            );
         }

         if (playerFleet != null) {
            boolean hasFleetMaintenanceUpgradeHMOD = ship.getVariant().hasHullMod("specialsphmod_uaf_modularpurifier_upgrades");
            String fleetPlayerID = ship.getFleetMemberId();
            int fleetEconomicalConfigurationNum = 0;
            if (fleetPlayerID != null) {
               for (FleetMemberAPI fleetMember : playerFleet.getFleetData().getMembersListCopy()) {
                  if (fleetMember.getId().equals(fleetPlayerID)) {
                     currShips = fleetMember;
                     break;
                  }
               }

               for (FleetMemberAPI fleetEconomicalAdjustment : playerFleet.getFleetData().getMembersListCopy()) {
                  if (fleetEconomicalAdjustment.getVariant().hasHullMod("specialsphmod_uaf_modularpurifier_extension")) {
                     fleetEconomicalConfigurationNum++;
                  }

                  if (fleetEconomicalConfigurationNum == 5) {
                     break;
                  }
               }
            }

            if (currShips != null) {
               float effectMult = fleetEconomicalConfigurationNum;
               int totalMaintMitigbonus = (int)(4.0F * effectMult);
               boolean isMothballed = currShips.isMothballed();
               if (!this.disableExtraEffect) {
                  if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
                     tooltip.addSectionHeading("Fleet-Wide Effect", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
                     TooltipMakerAPI extrasystxt = tooltip.beginImageWithText(
                        Global.getSettings().getSpriteName("tooltips", "logistic_analyzer"), SUStringCodex.SHU_TOOLTIP_IMG
                     );
                     extrasystxt.addPara(
                        "Maintenance Mitigator",
                        SUStringCodex.SHU_TOOLTIP_PADZERO,
                        Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                        new String[]{"Maintenance Mitigator"}
                     );
                     extrasystxt.addPara(
                        "An Auroran Delta-level AI is assigned to have full control of the fleet's entire maintenance management system, it will efficiently handle the logistic computations of the whole fleet, reducing the supply cost to a degree. The effect %s with another ship that has Logistic Analyzer and the fleet-wide supply cost reduction caps at %s.",
                        SUStringCodex.SHU_TOOLTIP_PADZERO,
                        Misc.getHighlightColor(),
                        new String[]{"stacks", Misc.getRoundedValue(20.0F) + "%"}
                     );
                     tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
                     tooltip.addPara(
                        "• Reduces ship's supply cost: %s\n• Current total fleet-wide bonus: %s",
                        SUStringCodex.SHU_TOOLTIP_PADMAIN,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(4.0F) + "%", totalMaintMitigbonus + "%"}
                     );
                  }

                  if (hasFleetMaintenanceUpgradeHMOD && isMothballed) {
                     tooltip.addPara(
                        "%s",
                        SUStringCodex.SHU_TOOLTIP_PADMAIN,
                        Misc.getNegativeHighlightColor(),
                        new String[]{"This ship is currently mothballed, preventing the Maintenance Mitigator from functioning."}
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
      HULLDP.put(HullSize.FIGHTER, 0.0F);
      HULLDP.put(HullSize.FRIGATE, 5.0F);
      HULLDP.put(HullSize.DESTROYER, 5.0F);
      HULLDP.put(HullSize.CRUISER, 5.0F);
      HULLDP.put(HullSize.CAPITAL_SHIP, 5.0F);
   }
}
