package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
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

public class SUCombatDroneReplicatorUpgrades extends BaseHullMod {
   public static final String DATA_PREFIX = "dronereplicator_shu_check_";
   public static final String ITEM = "drone_replicator";
   private static final float FIGHTER_REPLACEMENT_RATE_BONUS = 10.0F;
   private static final float RATE_DECREASE_MODIFIER = 20.0F;
   private static final float COST_REDUCTION_LPC = 1.0F;
   private static final float EXTRA_BAY = 1.0F;
   private static final float RAID_BONUS_ZERO = 0.0F;
   private static final float RAID_BONUS_ONE = 20.0F;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_DRONEREPLICATORHMOD_EXTRA_EFFECT;
   boolean disableExpandedDeckCrewIncompat = SUPlugin.DISABLE_EXPANDEDDECKCREW_INCOMPATIBILITY;
   float fighterReplacementRate = SUPlugin.CM_DRONE_REPLICATOR_FIGHTER_REPLACEMENT_RATE_BONUS;
   float fighterRateDecrease = SUPlugin.CM_DRONE_REPLICATOR_RATE_DECREASE_MODIFIER;
   float fighterordnancePointReductionCost = SUPlugin.CM_DRONE_REPLICATOR_COST_REDUCTION_FIGHTER_LPC;
   float bomberordnancePointReductionCost = SUPlugin.CM_DRONE_REPLICATOR_COST_REDUCTION_BOMBER_LPC;
   float extraFighterBay = SUPlugin.CM_DRONE_REPLICATOR_EXTRA_BAY;
   boolean toggleGeneralIncompat;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};


   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_combatdronereplicator_upgrades") ? ALL_INCOMPAT_IDS : null;
   }

   public CargoStackAPI getRequiredItem() {
      return Global.getSettings().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData("drone_replicator", null), null);
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
            TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("drone_replicator").getIconName(), 20.0F);
            text.addPara("Requires " + aOrAn + " %s to install.", opad, color, new String[]{name});
            tooltip.addImageWithText(5.0F);
         } else if (currentVariant != null && member != null) {
            if (currentVariant.hasHullMod(this.spec.getId())) {
               if (!currentVariant.getHullSpec().getBuiltInMods().contains(this.spec.getId())) {
                  Color color = Misc.getPositiveHighlightColor();
                  TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("drone_replicator").getIconName(), 20.0F);
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

               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("drone_replicator").getIconName(), 20.0F);
               text3.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", color, opad);
               tooltip.addImageWithText(5.0F);
            }
         }
      }
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableCombatDroneReplicatorAIRToggle");
         this.fighterReplacementRate = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DRONE_REPLICATOR_FIGHTER_REPLACEMENT_RATE_BONUS");
         this.fighterRateDecrease = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DRONE_REPLICATOR_RATE_DECREASE_MODIFIER");
         this.fighterordnancePointReductionCost = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DRONE_REPLICATOR_COST_REDUCTION_FIGHTER_LPC");
         this.bomberordnancePointReductionCost = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DRONE_REPLICATOR_COST_REDUCTION_BOMBER_LPC");
         this.extraFighterBay = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DRONE_REPLICATOR_EXTRA_BAY");
      }

      if (this.enableCustomSM) {
         float timeMultCSM = 1.0F / ((100.0F + this.fighterReplacementRate) / 100.0F);
         stats.getDynamic().getStat("replacement_rate_decrease_mult").modifyMult(id, 1.0F - this.fighterRateDecrease / 100.0F);
         stats.getFighterRefitTimeMult().modifyMult(id, timeMultCSM);
         stats.getDynamic().getMod("all_fighter_cost_mod").modifyFlat(id, -this.fighterordnancePointReductionCost);
         stats.getDynamic().getMod("fighter_cost_mod").modifyFlat(id, -this.fighterordnancePointReductionCost);
         stats.getDynamic().getMod("interceptor_cost_mod").modifyFlat(id, -this.fighterordnancePointReductionCost);
         stats.getDynamic().getMod("support_cost_mod").modifyFlat(id, -this.fighterordnancePointReductionCost);
         stats.getDynamic().getMod("bomber_cost_mod").modifyFlat(id, -this.bomberordnancePointReductionCost);
         stats.getNumFighterBays().modifyFlat(id, this.extraFighterBay);
      } else if (!this.enableCustomSM) {
         float timeMult = 0.9090909F;
         stats.getDynamic().getStat("replacement_rate_decrease_mult").modifyMult(id, 0.8F);
         stats.getFighterRefitTimeMult().modifyMult(id, 0.9090909F);
         stats.getDynamic().getMod("all_fighter_cost_mod").modifyFlat(id, -1.0F);
         stats.getDynamic().getMod("fighter_cost_mod").modifyFlat(id, -1.0F);
         stats.getDynamic().getMod("interceptor_cost_mod").modifyFlat(id, -1.0F);
         stats.getDynamic().getMod("support_cost_mod").modifyFlat(id, -1.0F);
         stats.getDynamic().getMod("bomber_cost_mod").modifyFlat(id, -2.0F);
         stats.getNumFighterBays().modifyFlat(id, 1.0F);
      }

      if (!this.disableExtraEffect) {
         int avLPC = stats.getVariant().getFittedWings().size();
         switch (avLPC) {
            case 0:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 0.0F);
               break;
            case 1:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 20.0F, "Atmospheric Invasion Refit");
               break;
            case 2:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 40.0F, "Atmospheric Invasion Refit");
               break;
            case 3:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 60.0F, "Atmospheric Invasion Refit");
               break;
            case 4:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 80.0F, "Atmospheric Invasion Refit");
               break;
            case 5:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 100.0F, "Atmospheric Invasion Refit");
               break;
            case 6:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 120.0F, "Atmospheric Invasion Refit");
               break;
            case 7:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 140.0F, "Atmospheric Invasion Refit");
               break;
            case 8:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 160.0F, "Atmospheric Invasion Refit");
               break;
            case 9:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 180.0F, "Atmospheric Invasion Refit");
               break;
            case 10:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 200.0F, "Atmospheric Invasion Refit");
               break;
            case 11:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 220.0F, "Atmospheric Invasion Refit");
               break;
            case 12:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 240.0F, "Atmospheric Invasion Refit");
               break;
            case 13:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 260.0F, "Atmospheric Invasion Refit");
               break;
            case 14:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 280.0F, "Atmospheric Invasion Refit");
               break;
            case 15:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 300.0F, "Atmospheric Invasion Refit");
               break;
            case 16:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 320.0F, "Atmospheric Invasion Refit");
               break;
            case 17:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 340.0F, "Atmospheric Invasion Refit");
               break;
            case 18:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 360.0F, "Atmospheric Invasion Refit");
               break;
            case 19:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 380.0F, "Atmospheric Invasion Refit");
               break;
            case 20:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 400.0F, "Atmospheric Invasion Refit");
               break;
            case 21:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 420.0F, "Atmospheric Invasion Refit");
               break;
            case 22:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 440.0F, "Atmospheric Invasion Refit");
               break;
            case 23:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 460.0F, "Atmospheric Invasion Refit");
               break;
            case 24:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 480.0F, "Atmospheric Invasion Refit");
               break;
            case 25:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 500.0F, "Atmospheric Invasion Refit");
               break;
            case 26:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 520.0F, "Atmospheric Invasion Refit");
               break;
            case 27:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 540.0F, "Atmospheric Invasion Refit");
               break;
            case 28:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 560.0F, "Atmospheric Invasion Refit");
               break;
            case 29:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 580.0F, "Atmospheric Invasion Refit");
               break;
            case 30:
               stats.getDynamic().getMod("ground_support").modifyFlat(id, 600.0F, "Atmospheric Invasion Refit");
         }
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      ShipVariantAPI shipVariant = ship.getVariant();
      MutableCharacterStatsAPI currentShipStats = ship.getCaptain() == null ? null : ship.getCaptain().getStats();
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
         this.disableExpandedDeckCrewIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableExpandedDeckCrewIncompatibilityToggle");
      }

      if (!this.disableExpandedDeckCrewIncompat && ship.getVariant().getHullMods().contains("expanded_deck_crew")) {
         ship.getVariant().removeMod("expanded_deck_crew");
         SUHullmodDisplayBlockScript.showBlocked(ship);
      }

      if (ship.getVariant().getSMods().contains("specialsphmod_combatdronereplicator_upgrades")
         || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_combatdronereplicator_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_combatdronereplicator_upgrades");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_combatdronereplicator_upgrades")) {
         ship.getVariant().addPermaMod("specialsphmod_combatdronereplicator_utilityscript");
         if (currentShipStats != null) {
            SUHullmodUpgradeInstaller.applyHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_combatdronereplicator_extension");
         }
      }
   }

   public boolean isApplicableToShip(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (SUPlugin.HASLUNALIB) {
            this.disableExpandedDeckCrewIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableExpandedDeckCrewIncompatibilityToggle");
         }

         if (!this.disableExpandedDeckCrewIncompat && ship.getVariant().hasHullMod("expanded_deck_crew")) {
            return false;
         } else if (ship.getMutableStats().getNumFighterBays().getBaseValue() <= 0.0F) {
            return false;
         } else {
            int builtIn = ship.getHullSpec().getBuiltInWings().size();
            int bays = Math.round(ship.getMutableStats().getNumFighterBays().getBaseValue());
            if (builtIn >= bays) {
               return false;
            } else {
               if (SUPlugin.HASLUNALIB) {
                  this.toggleGeneralIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_generalUpgradesIncompatibilityToggle");
               } else {
                  this.toggleGeneralIncompat = SUPlugin.DISABLE_GENERALUPGRADEINCOMPATIBILITY;
               }

               return this.toggleGeneralIncompat || !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS);
            }
         }
      } else {
         return false;
      }
   }

   public String getUnapplicableReason(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (SUPlugin.HASLUNALIB) {
            this.disableExpandedDeckCrewIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableExpandedDeckCrewIncompatibilityToggle");
         }

         if (!this.disableExpandedDeckCrewIncompat && ship.getVariant().hasHullMod("expanded_deck_crew")) {
            return "Incompatible with Expanded Deck Crew";
         } else {
            int builtIn = ship.getHullSpec().getBuiltInWings().size();
            int bays = Math.round(ship.getMutableStats().getNumFighterBays().getBaseValue());
            if (ship.getMutableStats().getNumFighterBays().getBaseValue() <= 0.0F) {
               return "Ship does not have standard fighter bays";
            } else if (builtIn >= bays) {
               return "Requires at least one modular fighter bay";
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

   public boolean affectsOPCosts() {
      return true;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         if (SUPlugin.HASLUNALIB) {
            this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableCombatDroneReplicatorAIRToggle");
            this.disableExpandedDeckCrewIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableExpandedDeckCrewIncompatibilityToggle");
            this.fighterReplacementRate = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DRONE_REPLICATOR_FIGHTER_REPLACEMENT_RATE_BONUS");
            this.fighterRateDecrease = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DRONE_REPLICATOR_RATE_DECREASE_MODIFIER");
            this.fighterordnancePointReductionCost = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DRONE_REPLICATOR_COST_REDUCTION_FIGHTER_LPC");
            this.bomberordnancePointReductionCost = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DRONE_REPLICATOR_COST_REDUCTION_BOMBER_LPC");
            this.extraFighterBay = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_DRONE_REPLICATOR_EXTRA_BAY");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Reduces decay of fighter replacement rate: %s\n• Reduces fighter refit time: %s\n• Reduces OP cost of fighter & bomber LPC: %s/%s\n• Adds extra fighter bay: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(this.fighterReplacementRate) + "%",
                  Misc.getRoundedValue(this.fighterRateDecrease) + "%",
                  Misc.getRoundedValue(this.fighterordnancePointReductionCost),
                  Misc.getRoundedValue(this.bomberordnancePointReductionCost),
                  Misc.getRoundedValue(this.extraFighterBay)
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Reduces decay of fighter replacement rate: %s\n• Reduces fighter refit time: %s\n• Reduces OP cost of fighter & bomber LPC: %s/%s\n• Adds extra fighter bay: %s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(10.0F) + "%",
                  Misc.getRoundedValue(20.0F) + "%",
                  Misc.getRoundedValue(1.0F),
                  Misc.getRoundedValue(2.0F),
                  Misc.getRoundedValue(1.0F)
               }
            );
         }

         if (!this.disableExtraEffect) {
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1")) && ship.getVariant().hasHullMod("specialsphmod_combatdronereplicator_upgrades")) {
               tooltip.addSectionHeading("Additional Effect", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI extrasystxt = tooltip.beginImageWithText(
                  Global.getSettings().getSpriteName("tooltips", "atmosphere_invader"), SUStringCodex.SHU_TOOLTIP_IMG
               );
               extrasystxt.addPara(
                  "Atmospheric Invasion Refit",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                  new String[]{"Atmospheric Invasion Refit"}
               );
               extrasystxt.addPara(
                  "Another hacked input forces the Replicator to modify the chassis of the installed LPC with the advanced atmosphere-reentry suite. %s wing squadron improves the effective strength of ground operation by %s up to the total number of marines in the fleet.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{"Each", Misc.getRoundedValue(20.0F)}
               );
               tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
               int avLPC = ship.getVariant().getFittedWings().size();
               switch (avLPC) {
                  case 0:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons, no bonus applied.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        Misc.getNegativeHighlightColor(),
                        new String[]{Misc.getRoundedValue(0.0F)}
                     );
                     break;
                  case 1:
                     tooltip.addPara(
                        "• There are %s available fighter squadron.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(1.0F), Misc.getRoundedValue(20.0F)}
                     );
                     break;
                  case 2:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(2.0F), Misc.getRoundedValue(40.0F)}
                     );
                     break;
                  case 3:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(3.0F), Misc.getRoundedValue(60.0F)}
                     );
                     break;
                  case 4:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(4.0F), Misc.getRoundedValue(80.0F)}
                     );
                     break;
                  case 5:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(5.0F), Misc.getRoundedValue(100.0F)}
                     );
                     break;
                  case 6:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(6.0F), Misc.getRoundedValue(120.0F)}
                     );
                     break;
                  case 7:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(7.0F), Misc.getRoundedValue(140.0F)}
                     );
                     break;
                  case 8:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(8.0F), Misc.getRoundedValue(160.0F)}
                     );
                     break;
                  case 9:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(9.0F), Misc.getRoundedValue(180.0F)}
                     );
                     break;
                  case 10:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(10.0F), Misc.getRoundedValue(200.0F)}
                     );
                     break;
                  case 11:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(11.0F), Misc.getRoundedValue(220.0F)}
                     );
                     break;
                  case 12:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(12.0F), Misc.getRoundedValue(240.0F)}
                     );
                     break;
                  case 13:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(13.0F), Misc.getRoundedValue(260.0F)}
                     );
                     break;
                  case 14:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(14.0F), Misc.getRoundedValue(280.0F)}
                     );
                     break;
                  case 15:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(15.0F), Misc.getRoundedValue(300.0F)}
                     );
                     break;
                  case 16:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(16.0F), Misc.getRoundedValue(320.0F)}
                     );
                     break;
                  case 17:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(17.0F), Misc.getRoundedValue(340.0F)}
                     );
                     break;
                  case 18:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(18.0F), Misc.getRoundedValue(360.0F)}
                     );
                     break;
                  case 19:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(19.0F), Misc.getRoundedValue(380.0F)}
                     );
                     break;
                  case 20:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(20.0F), Misc.getRoundedValue(400.0F)}
                     );
                     break;
                  case 21:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(21.0F), Misc.getRoundedValue(420.0F)}
                     );
                     break;
                  case 22:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(22.0F), Misc.getRoundedValue(440.0F)}
                     );
                     break;
                  case 23:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(23.0F), Misc.getRoundedValue(460.0F)}
                     );
                     break;
                  case 24:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(24.0F), Misc.getRoundedValue(480.0F)}
                     );
                     break;
                  case 25:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(25.0F), Misc.getRoundedValue(500.0F)}
                     );
                     break;
                  case 26:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(26.0F), Misc.getRoundedValue(520.0F)}
                     );
                     break;
                  case 27:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(27.0F), Misc.getRoundedValue(540.0F)}
                     );
                     break;
                  case 28:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(28.0F), Misc.getRoundedValue(560.0F)}
                     );
                     break;
                  case 29:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(29.0F), Misc.getRoundedValue(580.0F)}
                     );
                     break;
                  case 30:
                     tooltip.addPara(
                        "• There are %s available fighter squadrons.\n• Increased raid efficiency by %s.",
                        SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                        SUStringCodex.SHU_TOOLTIP_GREEN,
                        new String[]{Misc.getRoundedValue(30.0F), Misc.getRoundedValue(600.0F)}
                     );
               }
            }

            if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F1")) && ship.getVariant().hasHullMod("specialsphmod_combatdronereplicator_upgrades")) {
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
            if (!this.disableExpandedDeckCrewIncompat) {
               text.addPara(
                  "Not compatible with %s, %s",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getNegativeHighlightColor(),
                  new String[]{"Expanded Deck Crew", "Other Special Upgrade Hullmods"}
               );
            } else if (this.disableExpandedDeckCrewIncompat) {
               text.addPara(
                  "Not compatible with %s", SUStringCodex.SHU_TOOLTIP_PADZERO, Misc.getNegativeHighlightColor(), new String[]{"Other Special Upgrade Hullmods"}
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
