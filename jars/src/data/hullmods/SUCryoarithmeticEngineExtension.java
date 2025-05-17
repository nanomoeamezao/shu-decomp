package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.util.id.SUStringCodex;
import java.util.List;
import lunalib.lunaSettings.LunaSettings;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

public class SUCryoarithmeticEngineExtension extends BaseHullMod {
   private static final float FLUX_REDUC_PERCENT = 25.0F;
   private static final float ENGINE_HEALTH_BONUS = 100.0F;
   public final IntervalUtil flareInterval = new IntervalUtil(0.05F, 0.05F);
   public final String ID;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_CRYOARITHMETICHMOD_EXTRA_EFFECT;
   float fluxReductionBonus = SUPlugin.CM_CRYOARITHMETICENGINE_FLUX_REDUC_PERCENT;
   float engineHealthBonus = SUPlugin.CM_CRYOARITHMETICENGINE_ENGINE_HEALTH_BONUS;
   boolean toggleGeneralIncompat;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};


   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_cryoarithmeticengine_extension") ? ALL_INCOMPAT_IDS : null;
   }

   public SUCryoarithmeticEngineExtension() {
      this.ID = "SUCryoarithmeticEngineExtension";
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.fluxReductionBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CRYOARITHMETICENGINE_FLUX_REDUC_PERCENT");
         this.engineHealthBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CRYOARITHMETICENGINE_ENGINE_HEALTH_BONUS");
      }

      if (this.enableCustomSM) {
         stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -this.fluxReductionBonus);
         stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -this.fluxReductionBonus);
         stats.getBeamWeaponFluxCostMult().modifyPercent(id, -this.fluxReductionBonus);
         stats.getMissileWeaponFluxCostMod().modifyPercent(id, -this.fluxReductionBonus);
         stats.getEngineHealthBonus().modifyPercent(id, this.engineHealthBonus);
      } else if (!this.enableCustomSM) {
         stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -25.0F);
         stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -25.0F);
         stats.getBeamWeaponFluxCostMult().modifyPercent(id, -25.0F);
         stats.getMissileWeaponFluxCostMod().modifyPercent(id, -25.0F);
         stats.getEngineHealthBonus().modifyPercent(id, 100.0F);
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
               ship.getVariant().removePermaMod(blockedMod);
            }
         }
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (SUPlugin.HASLUNALIB) {
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableCryoarithmeticEngineCFlaresToggle");
      }

      if (!this.disableExtraEffect) {
         if (Global.getCombatEngine().isPaused()) {
            return;
         }

         if (!ship.isAlive() || ship.isPiece()) {
            return;
         }

         if (ship.isAlive() && ship.getFluxTracker().isOverloaded()) {
            ShipAPI playerShip = Global.getCombatEngine().getPlayerShip();
            CombatEngineAPI engine = Global.getCombatEngine();
            String CRYOENGDataKey = ship.getId() + "_shuqext_reload_data";
            Object targetDataObj = Global.getCombatEngine().getCustomData().get(CRYOENGDataKey);
            if (targetDataObj == null) {
               Global.getCombatEngine()
                  .getCustomData()
                  .put(
                     CRYOENGDataKey,
                     new SUCryoarithmeticEngineUpgrades.QHData(ship.getHullSize(), 3.0F, SUCryoarithmeticEngineUpgrades.CRYOFLARES.get(ship.getHullSize()))
                  );
            } else if (ship.getHullSize() != ((SUCryoarithmeticEngineUpgrades.QHData)targetDataObj).size) {
               Global.getCombatEngine().getCustomData().remove(CRYOENGDataKey);
               Global.getCombatEngine()
                  .getCustomData()
                  .put(
                     CRYOENGDataKey,
                     new SUCryoarithmeticEngineUpgrades.QHData(ship.getHullSize(), 3.0F, SUCryoarithmeticEngineUpgrades.CRYOFLARES.get(ship.getHullSize()))
                  );
            }

            SUCryoarithmeticEngineUpgrades.QHData data = (SUCryoarithmeticEngineUpgrades.QHData)targetDataObj;
            if (data == null) {
               return;
            }

            this.flareInterval.advance(amount);
            if (data.isReloading) {
               data.reloadingInterval.advance(amount);
               boolean hasElapsed = data.reloadingInterval.intervalElapsed();
               int elapsed = Math.round((float)((int)data.reloadingInterval.getElapsed()));
               if (!hasElapsed) {
                  if (ship == playerShip) {
                     String status = "LAUNCHING CRYOFLARES";
                     engine.maintainStatusForPlayerShip(
                        this.ID + "_TOOLTIP",
                        Global.getSettings().getSpriteName("tooltips", "cryoflares_sys"),
                        "Emergency System Status: " + status,
                        "Reloading in " + elapsed + " / 3 seconds",
                        false
                     );
                  }

                  return;
               }

               data.isReloading = false;
            }

            if (!this.flareInterval.intervalElapsed()) {
               return;
            }

            List<MissileAPI> missiles = AIUtils.getNearbyEnemyMissiles(ship, SUCryoarithmeticEngineUpgrades.ENGAGEMENT_RANGE.get(ship.getHullSize()));
            if (missiles == null || missiles.isEmpty()) {
               return;
            }

            float variance = MathUtils.getRandomNumberInRange(-0.3F, 0.3F);
            MissileAPI target = AIUtils.getNearestMissile(ship);
            data.attackAngle = VectorUtils.getAngle(ship.getLocation(), target.getLocation());
            float angle = (float)(data.attackAngle + (Math.random() * 10.0 - 20.0));
            Vector2f location = MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() / 2.0F, data.attackAngle);
            Vector2f location2 = MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() / 2.0F, 180.0F);
            Vector2f location3 = MathUtils.getPointOnCircumference(ship.getLocation(), ship.getCollisionRadius() / 2.0F, 360.0F);
            Global.getCombatEngine().addSmokeParticle(location, ship.getVelocity(), 20.0F, 0.4F, 0.5F, SUCryoarithmeticEngineUpgrades.FLARE_SMOKE);
            Global.getCombatEngine()
               .addNebulaSmokeParticle(
                  location,
                  ship.getVelocity(),
                  SUCryoarithmeticEngineUpgrades.NEBULA_SIZE / 4.0F,
                  15.0F,
                  0.1F,
                  0.3F,
                  2.0F,
                  SUCryoarithmeticEngineUpgrades.FLARE_SMOKE
               );
            Global.getCombatEngine()
               .addNebulaSmokeParticle(
                  location2,
                  ship.getVelocity(),
                  SUCryoarithmeticEngineUpgrades.NEBULA_SIZE / 2.0F,
                  15.0F,
                  0.1F,
                  0.3F,
                  2.0F,
                  SUCryoarithmeticEngineUpgrades.FLARE_SMOKE
               );
            Global.getCombatEngine()
               .addNebulaSmokeParticle(
                  location3,
                  ship.getVelocity(),
                  SUCryoarithmeticEngineUpgrades.NEBULA_SIZE / 2.0F,
                  15.0F,
                  0.1F,
                  0.3F,
                  2.0F,
                  SUCryoarithmeticEngineUpgrades.FLARE_SMOKE
               );
            Global.getSoundPlayer().playSound("launch_flare_1", 1.0F + variance, 1.0F + variance, location, ship.getVelocity());
            MissileAPI newMissile = (MissileAPI)Global.getCombatEngine()
               .spawnProjectile(ship, (WeaponAPI)null, "shu_cryolauncher", location, angle, ship.getVelocity());
            newMissile.setFromMissile(true);
            MissileAPI newMissile2 = (MissileAPI)Global.getCombatEngine()
               .spawnProjectile(ship, (WeaponAPI)null, "shu_cryolauncher", location2, 180.0F, ship.getVelocity());
            newMissile2.setFromMissile(true);
            MissileAPI newMissile3 = (MissileAPI)Global.getCombatEngine()
               .spawnProjectile(ship, (WeaponAPI)null, "shu_cryolauncher", location3, 360.0F, ship.getVelocity());
            newMissile3.setFromMissile(true);
            data.flaresLeft--;
            if (data.flaresLeft <= 0) {
               data.isReloading = true;
               data.flaresLeft = data.maxFlares;
               data.reloadingInterval.setElapsed(0.0F);
            }
         }
      }
   }

   public boolean isApplicableToShip(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         return SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS)
            ? false
            : !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), this.getIncompatibleIds());
      } else {
         return false;
      }
   }

   public String getUnapplicableReason(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         return !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS)
               && !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), this.getIncompatibleIds())
            ? super.getUnapplicableReason(ship)
            : "Only one type of special upgrade hullmod can be installed per ship";
      } else {
         return "Unable to locate ship!";
      }
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         if (SUPlugin.HASLUNALIB) {
            this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableCryoarithmeticEngineCFlaresToggle");
            this.fluxReductionBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CRYOARITHMETICENGINE_FLUX_REDUC_PERCENT");
            this.engineHealthBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CRYOARITHMETICENGINE_ENGINE_HEALTH_BONUS");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Reduces weapon flux cost: %s\n• Increases engine durability: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{Misc.getRoundedValue(this.fluxReductionBonus) + "%", Misc.getRoundedValue(this.engineHealthBonus) + "%"}
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Reduces weapon flux cost: %s\n• Increases engine durability: %s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{Misc.getRoundedValue(25.0F) + "%", Misc.getRoundedValue(100.0F) + "%"}
            );
         }

         if (!this.disableExtraEffect) {
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addSectionHeading("Extra System", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI text2 = tooltip.beginImageWithText(
                  Global.getSettings().getSpriteName("tooltips", "cryoflares_sys"), SUStringCodex.SHU_TOOLTIP_IMG
               );
               text2.addPara(
                  "Cryoflare Launcher",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                  new String[]{"Cryoflare Launcher"}
               );
               text2.addPara(
                  "A set of hacked programs within the Cryoarithmetic Engine will trigger the activation of its emergency system when the ship gets overloaded. %s will be released if it detects incoming missiles during overload. There's a %s seconds interval before the flares can be launched again.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{"Cryoflares", Misc.getRoundedValue(3.0F)}
               );
               tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
            }

            if (!Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addPara(
                     "Press and hold [%s] to view its extra system.",
                     SUStringCodex.SHU_TOOLTIP_PADMAIN,
                     Misc.getGrayColor(),
                     Misc.getStoryBrightColor(),
                     new String[]{"F1"}
                  )
                  .setAlignment(Alignment.MID);
            }
         }

         tooltip.addPara(
            "%s",
            SUStringCodex.SHU_TOOLTIP_PADMAIN,
            Misc.getGrayColor(),
            new String[]{"This is an extension of Quantum Heatsink and it will remove itself when the main hullmod is removed from the parent module."}
         );
      }
   }
}
