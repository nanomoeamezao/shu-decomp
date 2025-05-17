package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
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
import java.util.ArrayList;
import java.util.List;
import lunalib.lunaSettings.LunaSettings;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.input.Keyboard;

public class SUFusionLampUpgrades extends BaseHullMod {
   public static final String DATA_PREFIX = "fusionlamp_reactor_shu_check_";
   public static final String ITEM = "orbital_fusion_lamp";
   private static final float DAMAGE_BONUS = 5.0F;
   private static final float FLUX_REDUC = 10.0F;
   private static final float SHIELD_DAMAGE_BONUS = 15.0F;
   private static final float RANGE_BONUS = 20.0F;
   public static final float BONUS_HIT_CHANCE = 0.1F;
   public static final float EMP_MULT = 1.2F;
   private final String ID;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_FUSIONLAMPHMOD_EXTRA_EFFECT;
   boolean disableAdvancedOpticsIncompat = SUPlugin.DISABLE_ADVANCEDOPTICS_INCOMPATIBILITY;
   float damageBonus = SUPlugin.CM_FUSIONLAMP_DAMAGE_BONUS;
   float fluxReduction = SUPlugin.CM_FUSIONLAMP_FLUX_REDUC;
   float shieldDamageBonus = SUPlugin.CM_FUSIONLAMP_SHIELD_DAMAGE_BONUS;
   float rangeBonus = SUPlugin.CM_FUSIONLAMP_RANGE_BONUS;
   boolean toggleGeneralIncompat;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};


   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_fusionlampreactor_upgrades") ? ALL_INCOMPAT_IDS : null;
   }

   public SUFusionLampUpgrades() {
      this.ID = "SUFusionLampUpgrades";
   }

   public CargoStackAPI getRequiredItem() {
      return Global.getSettings().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData("orbital_fusion_lamp", null), null);
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
            TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("orbital_fusion_lamp").getIconName(), 20.0F);
            text.addPara("Requires " + aOrAn + " %s to install.", opad, color, new String[]{name});
            tooltip.addImageWithText(5.0F);
         } else if (currentVariant != null && member != null) {
            if (currentVariant.hasHullMod(this.spec.getId())) {
               if (!currentVariant.getHullSpec().getBuiltInMods().contains(this.spec.getId())) {
                  Color color = Misc.getPositiveHighlightColor();
                  TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("orbital_fusion_lamp").getIconName(), 20.0F);
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

               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("orbital_fusion_lamp").getIconName(), 20.0F);
               text3.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", color, opad);
               tooltip.addImageWithText(5.0F);
            }
         }
      }
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.damageBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FUSIONLAMP_DAMAGE_BONUS");
         this.fluxReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FUSIONLAMP_FLUX_REDUC");
         this.shieldDamageBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FUSIONLAMP_SHIELD_DAMAGE_BONUS");
         this.rangeBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FUSIONLAMP_RANGE_BONUS");
      }

      if (this.enableCustomSM) {
         stats.getBeamWeaponDamageMult().modifyPercent(id, this.damageBonus);
         stats.getEnergyWeaponDamageMult().modifyPercent(id, this.damageBonus);
         stats.getBeamWeaponFluxCostMult().modifyPercent(id, -this.fluxReduction);
         stats.getDamageToTargetShieldsMult().modifyPercent(id, this.shieldDamageBonus);
         stats.getBeamWeaponRangeBonus().modifyPercent(id, this.rangeBonus);
         stats.getEnergyWeaponRangeBonus().modifyPercent(id, this.rangeBonus);
      } else if (!this.enableCustomSM) {
         stats.getBeamWeaponDamageMult().modifyPercent(id, 5.0F);
         stats.getEnergyWeaponDamageMult().modifyPercent(id, 5.0F);
         stats.getBeamWeaponFluxCostMult().modifyPercent(id, -10.0F);
         stats.getDamageToTargetShieldsMult().modifyPercent(id, 15.0F);
         stats.getBeamWeaponRangeBonus().modifyPercent(id, 20.0F);
         stats.getEnergyWeaponRangeBonus().modifyPercent(id, 20.0F);
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
         this.disableAdvancedOpticsIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableAdvancedOpticsIncompatibilityToggle");
      }

      if (!this.disableAdvancedOpticsIncompat && ship.getVariant().getHullMods().contains("advancedoptics")) {
         ship.getVariant().removeMod("advancedoptics");
         SUHullmodDisplayBlockScript.showBlocked(ship);
      }

      if (ship.getVariant().getSMods().contains("specialsphmod_fusionlampreactor_upgrades")
         || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_fusionlampreactor_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_fusionlampreactor_upgrades");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_fusionlampreactor_upgrades")) {
         ship.getVariant().addPermaMod("specialsphmod_fusionlampreactor_utilityscript");
         if (currentShipStats != null) {
            SUHullmodUpgradeInstaller.applyHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_fusionlampreactor_extension");
         }
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (SUPlugin.HASLUNALIB) {
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableFusionLampReactorSCToggle");
      }

      if (!this.disableExtraEffect) {
         CombatEngineAPI engine = Global.getCombatEngine();
         ShipAPI playerShip = Global.getCombatEngine().getPlayerShip();
         if (!ship.isAlive()) {
            return;
         }

         List<DamagingProjectileAPI> EMPdmg = new ArrayList<>();
         List<DamagingProjectileAPI> projhit = new ArrayList<>();
         List<DamagingProjectileAPI> cleaneffect = new ArrayList<>();
         if (!ship.isHulk() && !ship.isPiece() && ship.getFluxLevel() >= 0.3F) {
            if (ship == playerShip) {
               engine.maintainStatusForPlayerShip(
                  this.ID + "_TOOLTIP_ONE",
                  Global.getSettings().getSpriteName("tooltips", "supercharged"),
                  "Fusion Capacitors: Supercharged!",
                  "Special effect is activated",
                  false
               );
            }

            ShipAPI target = AIUtils.getNearestEnemy(ship);
            if (target == null) {
               return;
            }

            for (DamagingProjectileAPI proj : CombatUtils.getProjectilesWithinRange(ship.getLocation(), 500.0F)) {
               if (ship == proj.getSource() && !EMPdmg.contains(proj) && !projhit.contains(proj)) {
                  if (Math.random() < 0.1F) {
                     EMPdmg.add(proj);
                  } else {
                     projhit.add(proj);
                  }
               }
            }

            for (DamagingProjectileAPI projx : EMPdmg) {
               if (projx.didDamage() && !projhit.contains(projx)) {
                  float empDamage = projx.getEmpAmount() * 1.2F;
                  DamageType var10000 = projx.getDamageType();
                  projx.getDamageType();
                  if (var10000 == DamageType.ENERGY) {
                     projhit.add(projx);
                     if (projx.getDamageTarget() instanceof ShipAPI && projx.getDamageAmount() != 0.0F) {
                        Global.getCombatEngine()
                           .spawnEmpArc(
                              projx.getSource(),
                              target.getLocation(),
                              target,
                              target,
                              DamageType.ENERGY,
                              projx.getDamageAmount() / 2.0F,
                              empDamage,
                              3000.0F,
                              "system_emp_emitter_impact",
                              15.0F,
                              new Color(200, 60, 10, 55),
                              new Color(240, 70, 40, 255)
                           );
                        Global.getCombatEngine()
                           .spawnEmpArcVisual(
                              projx.getLocation(), projx, target.getLocation(), target, 15.0F, new Color(200, 60, 10, 50), new Color(240, 70, 40, 255)
                           );
                     }
                  }
               }
            }

            for (DamagingProjectileAPI projxx : projhit) {
               if (!Global.getCombatEngine().isEntityInPlay(projxx)) {
                  EMPdmg.remove(projxx);
                  cleaneffect.add(projxx);
               }
            }

            for (DamagingProjectileAPI projxxx : cleaneffect) {
               projhit.remove(projxxx);
            }

            cleaneffect.clear();
         }
      }
   }

   public boolean isApplicableToShip(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (ship.getVariant().getHullSize() == HullSize.FRIGATE) {
            return false;
         } else {
            if (SUPlugin.HASLUNALIB) {
               this.disableAdvancedOpticsIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableAdvancedOpticsIncompatibilityToggle");
            }

            if (!this.disableAdvancedOpticsIncompat && ship.getVariant().hasHullMod("advancedoptics")) {
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
         if (ship.getVariant().getHullSize() == HullSize.FRIGATE) {
            return "Can not be installed on frigate ships";
         } else {
            if (SUPlugin.HASLUNALIB) {
               this.disableAdvancedOpticsIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableAdvancedOpticsIncompatibilityToggle");
            }

            if (!this.disableAdvancedOpticsIncompat && ship.getVariant().hasHullMod("advancedoptics")) {
               return "Incompatible with Advanced Optics";
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
      return status == 0
         ? "This installation is not applicable to modules, please install it on the main module"
         : super.getCanNotBeInstalledNowReason(ship, marketOrNull, mode);
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         if (SUPlugin.HASLUNALIB) {
            this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableFusionLampReactorSCToggle");
            this.disableAdvancedOpticsIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableAdvancedOpticsIncompatibilityToggle");
            this.damageBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FUSIONLAMP_DAMAGE_BONUS");
            this.fluxReduction = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FUSIONLAMP_FLUX_REDUC");
            this.shieldDamageBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FUSIONLAMP_SHIELD_DAMAGE_BONUS");
            this.rangeBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_FUSIONLAMP_RANGE_BONUS");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Increases beam and energy weapon damage: %s\n• Reduces flux cost of beam weapons: %s\n• Increases damage dealt to shields: %s\n• Increases beam and energy weapon range: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(this.damageBonus) + "%",
                  Misc.getRoundedValue(this.fluxReduction) + "%",
                  Misc.getRoundedValue(this.shieldDamageBonus) + "%",
                  Misc.getRoundedValue(this.rangeBonus) + "%"
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Increases beam and energy weapon damage: %s\n• Reduces flux cost of beam weapons: %s\n• Increases damage dealt to shields: %s\n• Increases beam and energy weapon range: %s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(5.0F) + "%", Misc.getRoundedValue(10.0F) + "%", Misc.getRoundedValue(15.0F) + "%", Misc.getRoundedValue(20.0F) + "%"
               }
            );
         }

         if (!this.disableExtraEffect) {
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addSectionHeading("Passive System", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI text2 = tooltip.beginImageWithText(
                  Global.getSettings().getSpriteName("tooltips", "supercharged_alt"), SUStringCodex.SHU_TOOLTIP_IMG
               );
               text2.addPara(
                  "Supercharged Capacitors",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                  new String[]{"Supercharged Capacitors"}
               );
               text2.addPara(
                  "When the ship's flux level is above %s, the installed Fusion Capacitors will become supercharged. Each energy-type projectile has a %s chance to arc to weapons and engines, dealing extra damage %s of the original hit.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{Misc.getRoundedValue(30.0F) + "%", Misc.getRoundedValue(10.0F) + "%", "half"}
               );
               tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
            }

            if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addPara(
                     "Press and hold [%s] to view its passive system.",
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
            if (!this.disableAdvancedOpticsIncompat) {
               text.addPara(
                  "Not compatible with %s, %s",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getNegativeHighlightColor(),
                  new String[]{"Advanced Optics", "Other Special Upgrade Hullmods"}
               );
            } else if (this.disableAdvancedOpticsIncompat) {
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
