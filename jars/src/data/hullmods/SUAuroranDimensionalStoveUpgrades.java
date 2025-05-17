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

public class SUAuroranDimensionalStoveUpgrades extends BaseHullMod {
   public static final String DATA_PREFIX_COLLAB = "dimensionalstove_shu_uaf_check_";
   public static final String ITEM = "uaf_dimen_microwave";
   public static final float MAX_CR_BONUS = 0.1F;
   public static final float SYSTEM_COOLDOWN_REDUCTION = 30.0F;
   public static final float SYSTEM_REGEN_BONUS = 40.0F;
   public static final float MAX_FLEET_STOVE = 5.0F;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_DIMENSIONALSTOVE_EXTRA_EFFECT;
   float maxCRBonus = SUPlugin.CM_UAF_DIMENSIONALSTOVE_MAX_CR_BONUS;
   float systemCDReduction = SUPlugin.CM_UAF_DIMENSIONALSTOVE_SYSTEM_COOLDOWN_REDUCTION;
   float systemRegenBonus = SUPlugin.CM_UAF_DIMENSIONALSTOVE_SYSTEM_REGEN_BONUS;
   boolean toggleGeneralIncompat;
   boolean isUAFPresent = Global.getSettings().getModManager().isModEnabled("uaf");
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};

   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_uaf_dimensionalstove_upgrades") ? ALL_INCOMPAT_IDS : null;
   }

   public CargoStackAPI getRequiredItem() {
      return this.isUAFPresent ? Global.getSettings().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData("uaf_dimen_microwave", null), null) : null;
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
            TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("uaf_dimen_microwave").getIconName(), 20.0F);
            text.addPara("Requires " + aOrAn + " %s to install.", opad, color, new String[]{name});
            tooltip.addImageWithText(5.0F);
         } else if (currentVariant != null && member != null) {
            if (currentVariant.hasHullMod(this.spec.getId())) {
               if (!currentVariant.getHullSpec().getBuiltInMods().contains(this.spec.getId())) {
                  Color color = Misc.getPositiveHighlightColor();
                  TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("uaf_dimen_microwave").getIconName(), 20.0F);
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

               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("uaf_dimen_microwave").getIconName(), 20.0F);
               text3.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", color, opad);
               tooltip.addImageWithText(5.0F);
            }
         }
      }
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.maxCRBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_DIMENSIONALSTOVE_MAX_CR_BONUS");
         this.systemCDReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_DIMENSIONALSTOVE_SYSTEM_COOLDOWN_REDUCTION");
         this.systemRegenBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_DIMENSIONALSTOVE_SYSTEM_REGEN_BONUS");
      }

      if (this.enableCustomSM) {
         stats.getMaxCombatReadiness().modifyFlat(id, this.maxCRBonus / 100.0F, "Dynamic Utility System");
         stats.getSystemCooldownBonus().modifyPercent(id, -this.systemCDReduction);
         stats.getSystemRegenBonus().modifyPercent(id, this.systemRegenBonus);
      } else if (!this.enableCustomSM) {
         stats.getMaxCombatReadiness().modifyFlat(id, 0.1F, "Dynamic Utility System");
         stats.getSystemCooldownBonus().modifyPercent(id, -30.0F);
         stats.getSystemRegenBonus().modifyPercent(id, 40.0F);
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
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableDimensionalStoveRFCToggle");
      }

      if (ship.getVariant().getSMods().contains("specialsphmod_uaf_dimensionalstove_upgrades")
         || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_uaf_dimensionalstove_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_uaf_dimensionalstove_upgrades");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_uaf_dimensionalstove_upgrades") && !this.disableExtraEffect) {
         ship.getVariant().addPermaMod("specialsphmod_uaf_dimensionalstove_extension");
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
         return !this.isUAFPresent && status != 2 && !SUHullmodUpgradeInstaller.playerHasSpecialItem("uaf_dimen_microwave")
            ? false
            : super.canBeAddedOrRemovedNow(ship, marketOrNull, mode);
      }
   }

   public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
      int status = SUHullmodUpgradeInstaller.isPlayerShip(ship, super.spec.getId());
      if (status == 0) {
         return "This installation is not applicable to modules";
      } else {
         return !this.isUAFPresent && status != 2 && !SUHullmodUpgradeInstaller.playerHasCommodity("uaf_dimen_microwave")
            ? "Installation requires [Auroran Dimensional Stove] (1)"
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
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableDimensionalStoveRFCToggle");
            this.maxCRBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_DIMENSIONALSTOVE_MAX_CR_BONUS");
            this.systemCDReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_DIMENSIONALSTOVE_SYSTEM_COOLDOWN_REDUCTION");
            this.systemRegenBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_UAF_DIMENSIONALSTOVE_SYSTEM_REGEN_BONUS");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Increases max combat readines: %s\n• Decreases ship's system cooldown: %s\n• Increases ship's system regeneration rate: %s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(this.maxCRBonus) + "%",
                  Misc.getRoundedValue(this.systemCDReduction) + "%",
                  Misc.getRoundedValue(this.systemRegenBonus) + "%"
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Increases max combat readines: %s\n• Decreases ship's system cooldown: %s\n• Increases ship's system regeneration rate: %s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{Misc.getRoundedValue(10.0F) + "%", Misc.getRoundedValue(30.0F) + "%", Misc.getRoundedValue(40.0F) + "%"}
            );
         }

         if (playerFleet != null) {
            boolean hasFleetCafeHMOD = ship.getVariant().hasHullMod("specialsphmod_uaf_modularpurifier_upgrades");
            String fleetPlayerID = ship.getFleetMemberId();
            int fleetRecreationalCafeNum = 0;
            if (fleetPlayerID != null) {
               for (FleetMemberAPI fleetMember : playerFleet.getFleetData().getMembersListCopy()) {
                  if (fleetMember.getId().equals(fleetPlayerID)) {
                     currShips = fleetMember;
                     break;
                  }
               }

               for (FleetMemberAPI fleetEconomicalAdjustment : playerFleet.getFleetData().getMembersListCopy()) {
                  if (fleetEconomicalAdjustment.getVariant().hasHullMod("specialsphmod_uaf_dimensionalstove_extension")) {
                     fleetRecreationalCafeNum++;
                  }

                  if (fleetRecreationalCafeNum == 5.0F) {
                     break;
                  }
               }
            }

            if (currShips != null) {
               float effectMult = fleetRecreationalCafeNum;
               int totalFleetCafebonus = (int)(10.0F * effectMult);
               boolean isMothballed = currShips.isMothballed();
               if (!this.disableExtraEffect) {
                  if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
                     tooltip.addSectionHeading("Fleet-Wide Effect", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
                     TooltipMakerAPI extrasystxt = tooltip.beginImageWithText(
                        Global.getSettings().getSpriteName("tooltips", "fleet_bakery"), SUStringCodex.SHU_TOOLTIP_IMG
                     );
                     extrasystxt.addPara(
                        "Recreational Fleet Cafe",
                        SUStringCodex.SHU_TOOLTIP_PADZERO,
                        Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                        new String[]{"Recreational Fleet Cafe"}
                     );
                     extrasystxt.addPara(
                        "The installed Auroran Dimensional Stove produces signature premium confectioneries from New Auroria, the heavenly flavor and goodness of the baked goods uplifts every crew members giving them a boost of morale and motivation. The effect %s with another ship that has Dynamic Utility System and the fleet-wide CR decay reduction caps at %s.",
                        SUStringCodex.SHU_TOOLTIP_PADZERO,
                        Misc.getHighlightColor(),
                        new String[]{"stacks", Misc.getRoundedValue(50.0F) + "%"}
                     );
                     tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
                     tooltip.addPara(
                        "• Reduces ship's CR decay rate: %s\n• Current total fleet-wide bonus: %s",
                        SUStringCodex.SHU_TOOLTIP_PADMAIN,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(10.0F) + "%", totalFleetCafebonus + "%"}
                     );
                  }

                  if (hasFleetCafeHMOD && isMothballed) {
                     tooltip.addPara(
                        "%s",
                        SUStringCodex.SHU_TOOLTIP_PADMAIN,
                        Misc.getNegativeHighlightColor(),
                        new String[]{"This ship is currently mothballed, preventing the Recreational Fleet Cafe from functioning."}
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
}
