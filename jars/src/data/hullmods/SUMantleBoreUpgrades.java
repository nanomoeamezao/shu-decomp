package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
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

public class SUMantleBoreUpgrades extends BaseHullMod {
   public static final String DATA_PREFIX = "mantle_bore_shu_check_";
   public static final String ITEM = "mantle_bore";
   private static final float BALLISTIC_RANGE_BONUS = 10.0F;
   private static final float HIT_STR = 1.1F;
   private static final float PROJ_SPEED_BONUS = 30.0F;
   private static final float RECOIL_BONUS = 60.0F;
   public static final float BONUS_HIT_CHANCE = 0.01F;
   public static final float BONUS_HIT_DMG = 1.1F;
   public static final float PROJ_RANGE = 600.0F;
   public static final int TEXT_SIZE = 25;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_MANTLEBOREHMOD_EXTRA_EFFECT;
   float ballisticRangeBonus = SUPlugin.CM_MANTLEBORE_BALLISTIC_RANGE_BONUS;
   float hitStrength = SUPlugin.CM_MANTLEBORE_HIT_STR;
   float projectileSpeedBonus = SUPlugin.CM_MANTLEBORE_PROJ_SPEED_BONUS;
   float recoilBonus = SUPlugin.CM_MANTLEBORE_RECOIL_BONUS;
   boolean toggleGeneralIncompat;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};


   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_mantlebore_upgrades") ? ALL_INCOMPAT_IDS : null;
   }

   public CargoStackAPI getRequiredItem() {
      return Global.getSettings().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData("mantle_bore", null), null);
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
            TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("mantle_bore").getIconName(), 20.0F);
            text.addPara("Requires " + aOrAn + " %s to install.", opad, color, new String[]{name});
            tooltip.addImageWithText(5.0F);
         } else if (currentVariant != null && member != null) {
            if (currentVariant.hasHullMod(this.spec.getId())) {
               if (!currentVariant.getHullSpec().getBuiltInMods().contains(this.spec.getId())) {
                  Color color = Misc.getPositiveHighlightColor();
                  TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("mantle_bore").getIconName(), 20.0F);
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

               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("mantle_bore").getIconName(), 20.0F);
               text3.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", color, opad);
               tooltip.addImageWithText(5.0F);
            }
         }
      }
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.ballisticRangeBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_MANTLEBORE_BALLISTIC_RANGE_BONUS");
         this.hitStrength = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_MANTLEBORE_HIT_STR");
         this.projectileSpeedBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_MANTLEBORE_PROJ_SPEED_BONUS");
         this.recoilBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_MANTLEBORE_RECOIL_BONUS");
      }

      if (this.enableCustomSM) {
         stats.getBallisticWeaponRangeBonus().modifyPercent(id, this.ballisticRangeBonus);
         stats.getHitStrengthBonus().modifyPercent(id, this.hitStrength);
         stats.getProjectileSpeedMult().modifyPercent(id, this.projectileSpeedBonus);
         stats.getMaxRecoilMult().modifyMult(id, 1.0F - 0.01F * this.recoilBonus);
         stats.getRecoilPerShotMult().modifyMult(id, 1.0F - 0.01F * this.recoilBonus);
         stats.getRecoilDecayMult().modifyMult(id, 1.0F - 0.01F * this.recoilBonus);
      } else if (!this.enableCustomSM) {
         stats.getBallisticWeaponRangeBonus().modifyPercent(id, 10.0F);
         stats.getHitStrengthBonus().modifyMult(id, 1.1F);
         stats.getProjectileSpeedMult().modifyPercent(id, 30.0F);
         stats.getMaxRecoilMult().modifyMult(id, 0.40000004F);
         stats.getRecoilPerShotMult().modifyMult(id, 0.40000004F);
         stats.getRecoilDecayMult().modifyMult(id, 0.40000004F);
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

      if (ship.getVariant().getSMods().contains("specialsphmod_mantlebore_upgrades")
         || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_mantlebore_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_mantlebore_upgrades");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_mantlebore_upgrades")) {
         ship.getVariant().addPermaMod("specialsphmod_mantlebore_utilityscript");
         if (currentShipStats != null) {
            SUHullmodUpgradeInstaller.applyHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_mantlebore_extension");
         }
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (SUPlugin.HASLUNALIB) {
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableMantleBoreUVRToggle");
      }

      if (!this.disableExtraEffect) {
         if (!ship.isAlive()) {
            return;
         }

         List<DamagingProjectileAPI> criticaldmg = new ArrayList<>();
         List<DamagingProjectileAPI> projhit = new ArrayList<>();
         List<DamagingProjectileAPI> cleaneffect = new ArrayList<>();
         if (!ship.isHulk() && !ship.isPiece()) {
            ShipAPI target = AIUtils.getNearestEnemy(ship);
            if (target == null) {
               return;
            }

            for (DamagingProjectileAPI proj : CombatUtils.getProjectilesWithinRange(ship.getLocation(), 600.0F)) {
               if (ship == proj.getSource() && !criticaldmg.contains(proj) && !projhit.contains(proj)) {
                  if (Math.random() <= 0.01F) {
                     criticaldmg.add(proj);
                     proj.setDamageAmount(proj.getDamageAmount() * 1.1F);
                  } else {
                     projhit.add(proj);
                  }
               }
            }

            for (DamagingProjectileAPI projx : criticaldmg) {
               if (projx.didDamage() && !projhit.contains(projx)) {
                  DamageType var10000 = projx.getDamageType();
                  projx.getDamageType();
                  if (var10000 == DamageType.KINETIC) {
                     projhit.add(projx);
                     if (projx.getDamageTarget() instanceof ShipAPI && projx.getDamageAmount() != 0.0F) {
                        Global.getCombatEngine().addFloatingText(projx.getLocation(), "Critical Hit!!!", 25.0F, Color.ORANGE, projx, 3.0F, 1.0F);
                        Global.getCombatEngine().addHitParticle(projx.getLocation(), projx.getVelocity(), 50.0F, 1.0F, 0.05F, Color.ORANGE);
                     }
                  }
               }
            }

            for (DamagingProjectileAPI projxx : projhit) {
               if (!Global.getCombatEngine().isEntityInPlay(projxx)) {
                  criticaldmg.remove(projxx);
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
         if (ship.getVariant().getHullSize() == HullSize.FRIGATE) {
            return "Can not be installed on frigate ships";
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
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableMantleBoreUVRToggle");
            this.ballisticRangeBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_MANTLEBORE_BALLISTIC_RANGE_BONUS");
            this.hitStrength = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_MANTLEBORE_HIT_STR");
            this.projectileSpeedBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_MANTLEBORE_PROJ_SPEED_BONUS");
            this.recoilBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_MANTLEBORE_RECOIL_BONUS");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Increases ballistic weapon range: %s\n• Increases hit strength of weapons to armor: %s\n• Increases weapon projectile speed: %s\n• Reduces weapon recoil: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(this.ballisticRangeBonus) + "%",
                  Misc.getRoundedValue(this.hitStrength) + "%",
                  Misc.getRoundedValue(this.projectileSpeedBonus) + "%",
                  Misc.getRoundedValue(this.recoilBonus) + "%"
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Increases ballistic weapon range: %s\n• Increases hit strength of weapons to armor: %s\n• Increases weapon projectile speed: %s\n• Reduces weapon recoil: %s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(10.0F) + "%", Misc.getRoundedValue(10.0F) + "%", Misc.getRoundedValue(30.0F) + "%", Misc.getRoundedValue(60.0F) + "%"
               }
            );
         }

         if (!this.disableExtraEffect) {
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addSectionHeading("Passive System", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI text2 = tooltip.beginImageWithText(
                  Global.getSettings().getSpriteName("tooltips", "hv_projectile"), SUStringCodex.SHU_TOOLTIP_IMG
               );
               text2.addPara(
                  "Ultra-Velocity Rounds",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                  new String[]{"Ultra-Velocity Rounds"}
               );
               text2.addPara(
                  "A hacked weapon configuration that takes advantage of the throttled firing chambers. The accelerated projectile has a %s chance of dealing additional %s damage on-hit. This only applies to %s damage.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{Misc.getRoundedValue(3.0F) + "%", Misc.getRoundedValue(50.0F) + "%", "kinetic"}
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
            text.addPara(
               "Not compatible with %s", SUStringCodex.SHU_TOOLTIP_PADZERO, Misc.getNegativeHighlightColor(), new String[]{"Other Special Upgrade Hullmods"}
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
