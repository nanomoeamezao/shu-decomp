package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI.EmpArcParams;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual.NEParams;
import com.fs.starfarer.api.impl.combat.dweller.DwellerShroud;
import com.fs.starfarer.api.impl.combat.dweller.HumanShipShroudedHullmod;
import com.fs.starfarer.api.impl.combat.dweller.RiftLightningEffect;
import com.fs.starfarer.api.impl.combat.dweller.DwellerShroud.DwellerShroudParams;
import com.fs.starfarer.api.impl.combat.threat.EnergyLashSystemScript.DelayedCombatActionPlugin;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.SUPlugin;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import lunalib.lunaSettings.LunaSettings;
import org.lwjgl.util.vector.Vector2f;

public class SHUGACHAAbyssGazer extends HumanShipShroudedHullmod {
   public static float MAX_RANGE = 4000.0F;
   public static float RECENT_HIT_DUR = 5.0F;
   public static float MAX_TIME_SINCE_RECENT_HIT = 0.1F;
   public static float WEIGHT_PER_RECENT_HIT = 1.0F;
   public static float MISFIRE_WEIGHT = 10.0F;
   public static float MIN_REFIRE_DELAY = 0.22F;
   public static float MAX_REFIRE_DELAY = 0.44F;
   public static float REFIRE_RATE_MULT = 1.0F;
   public static float FLUX_PER_DAMAGE = 1.0F;
   public static float MIN_DAMAGE = 200.0F;
   public static float MAX_DAMAGE = 500.0F;
   public static float EMP_MULT = 3.0F;
   boolean enableCheatModeForRetards = SUPlugin.ENABLE_CHEAT_FOR_RETARDS;
   public static String DATA_KEY = "SHUGACHAAbyssGazer_core_ShroudedThunderheadHullmod_data_key";

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (stats.getVariant().getSMods().contains("specialsphmod_gacha_abyss_gazer")) {
         stats.getCrewLossMult().modifyPercent(id, 50.0F);
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      super.applyEffectsAfterShipCreation(ship, id);
      if (SUPlugin.HASLUNALIB) {
         this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
      }

      if (!this.enableCheatModeForRetards
         && (
            !ship.getVariant().getSMods().contains("specialsphmod_gacha_abyss_gazer")
               || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_gacha_abyss_gazer")
         )) {
         ship.getVariant().removeMod("specialsphmod_gacha_abyss_gazer");
      }

      if (ship.getVariant().getSMods().contains("specialsphmod_gacha_abyss_gazer")) {
         ship.addListener(new SHUGACHAAbyssGazer.ShroudedThunderheadDamageDealtMod(ship));
      }
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (ship.getVariant().getSMods().contains("specialsphmod_gacha_abyss_gazer")) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "banner_r"), 368.0F, 40.0F, 5.0F);
            if (SUPlugin.HASLUNALIB) {
               this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
            }

            if (this.enableCheatModeForRetards) {
               LabelAPI retardrius = tooltip.addPara("%s", 5.0F, Misc.getBrightPlayerColor(), new String[]{"Cheat Mode: ON"});
               retardrius.setAlignment(Alignment.MID);
               retardrius.italicize();
            }

            tooltip.addPara(
               "• %s\n• %s\n• %s", SUStringCodex.SHU_TOOLTIP_EXTRADESC, Misc.getHighlightColor(), new String[]{"???????????", "???????????", "???????????"}
            );
         } else {
            tooltip.addPara(
               "%s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               Misc.getNegativeHighlightColor(),
               new String[]{"No bonus applied. The information will only appear if this hullmod is S-modded."}
            );
         }

         tooltip.addPara(
               "%s",
               SUStringCodex.SHU_TOOLTIP_PADQUOTE,
               SUStringCodex.SHU_TOOLTIP_QUOTECOLOR,
               new String[]{"\"They are watching, they are coming, the gates will open...\""}
            )
            .italicize();
         tooltip.addPara(
            "%s",
            SUStringCodex.SHU_TOOLTIP_PADSIG,
            SUStringCodex.SHU_TOOLTIP_QUOTECOLOR,
            new String[]{"         — Excerpts from Alpha Site Researcher's Journal"}
         );
      }
   }

   public Color getBorderColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_R_BORDER;
   }

   public Color getNameColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_R_NAME;
   }

   public static SHUGACHAAbyssGazer.ShroudedThunderheadHullmodData getData(ShipAPI ship) {
      CombatEngineAPI engine = Global.getCombatEngine();
      String key = DATA_KEY + "_" + ship.getId();
      SHUGACHAAbyssGazer.ShroudedThunderheadHullmodData data = (SHUGACHAAbyssGazer.ShroudedThunderheadHullmodData)engine.getCustomData().get(key);
      if (data == null) {
         data = new SHUGACHAAbyssGazer.ShroudedThunderheadHullmodData();
         engine.getCustomData().put(key, data);
      }

      return data;
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      super.advanceInCombat(ship, amount);
      if (ship.getVariant().getSMods().contains("specialsphmod_gacha_abyss_gazer")) {
         if (!ship.isAlive()) {
            return;
         }

         if (amount <= 0.0F) {
            return;
         }

         SHUGACHAAbyssGazer.ShroudedThunderheadHullmodData data = getData(ship);
         float prob = data.getHitProbability();
         DwellerShroud shroud = DwellerShroud.getShroudFor(ship);
         shroud.getParams().flashProbability = Math.min(0.1F + prob * 1.4F, 1.0F);
         data.untilArc = data.untilArc - amount * REFIRE_RATE_MULT;
         if (data.untilArc <= 0.0F) {
            boolean hasRecentHits = data.hasRecentEnoughHits();
            if (hasRecentHits) {
               SHUGACHAAbyssGazer.RecentHitData hit = data.pickRecentHit();
               if (hit != null && hit.target != null) {
                  data.recentHits.remove(hit);
                  this.spawnLightning(ship, hit);
               }

               data.untilArc = MIN_REFIRE_DELAY + (float)Math.random() * (MAX_REFIRE_DELAY - MIN_REFIRE_DELAY);
            }
         }

         data.recentHits.advance(amount);
      }
   }

   public static float getPowerMult(HullSize size) {
      switch (size) {
         case CAPITAL_SHIP:
            return 1.0F;
         case CRUISER:
            return 0.6666667F;
         case DESTROYER:
            return 0.33333334F;
         case FIGHTER:
         case FRIGATE:
            return 0.0F;
         default:
            return 1.0F;
      }
   }

   public static float getDamage(HullSize size) {
      float mult = getPowerMult(size);
      return MIN_DAMAGE + (MAX_DAMAGE - MIN_DAMAGE) * mult;
   }

   public static float getEMPDamage(HullSize size) {
      return getDamage(size) * EMP_MULT;
   }

   public static float getFluxCost(HullSize size) {
      return getDamage(size) * FLUX_PER_DAMAGE;
   }

   public void spawnLightning(ShipAPI ship, SHUGACHAAbyssGazer.RecentHitData hit) {
      CombatEngineAPI engine = Global.getCombatEngine();
      Vector2f from = ship.getLocation();
      Vector2f point = hit.point;
      float dist = Misc.getDistance(from, point);
      if (!(dist > MAX_RANGE)) {
         float mult = getPowerMult(ship.getHullSize());
         float damage = getDamage(ship.getHullSize());
         float emp = getEMPDamage(ship.getHullSize());
         if (FLUX_PER_DAMAGE > 0.0F) {
            float fluxCost = getFluxCost(ship.getHullSize());
            if (!this.deductFlux(ship, fluxCost)) {
               return;
            }
         }

         DwellerShroud shroud = DwellerShroud.getShroudFor(ship);
         if (shroud != null) {
            float angle = Misc.getAngleInDegrees(ship.getLocation(), point);
            from = Misc.getUnitVectorAtDegreeAngle(angle + 90.0F - 180.0F * (float)Math.random());
            from.scale((0.5F + (float)Math.random() * 0.25F) * shroud.getShroudParams().maxOffset * shroud.getShroudParams().overloadArcOffsetMult);
            Vector2f.add(ship.getLocation(), from, from);
         }

         float arcSpeed = RiftLightningEffect.RIFT_LIGHTNING_SPEED;
         EmpArcParams params = new EmpArcParams();
         params.segmentLengthMult = 8.0F;
         params.zigZagReductionFactor = 0.15F;
         params.fadeOutDist = 50.0F;
         params.minFadeOutMult = 10.0F;
         params.flickerRateMult = 0.3F;
         params.movementDurOverride = Math.max(0.05F, dist / arcSpeed);
         float arcWidth = 40.0F + mult * 40.0F;
         float explosionRadius = 40.0F + mult * 40.0F;
         Color color = RiftLightningEffect.RIFT_LIGHTNING_COLOR;
         EmpArcEntityAPI arc = engine.spawnEmpArcVisual(from, ship, point, null, arcWidth, color, new Color(255, 255, 255, 255), params);
         arc.setCoreWidthOverride(arcWidth / 2.0F);
         arc.setRenderGlowAtStart(false);
         arc.setFadedOutAtStart(true);
         arc.setSingleFlickerMode(true);
         float volume = 0.75F + 0.25F * mult;
         float pitch = 1.0F + 0.25F * (1.0F - mult);
         Global.getSoundPlayer().playSound("rift_lightning_fire", pitch, volume, from, ship.getVelocity());
         if (shroud != null) {
            DwellerShroudParams shroudParams = shroud.getShroudParams();
            params = new EmpArcParams();
            params.segmentLengthMult = 4.0F;
            params.glowSizeMult = 4.0F;
            params.flickerRateMult = 0.5F + (float)Math.random() * 0.5F;
            params.flickerRateMult *= 1.5F;
            Color core = Color.white;
            float thickness = shroudParams.overloadArcThickness;
            float angle = Misc.getAngleInDegrees(from, ship.getLocation());
            angle += 90.0F * ((float)Math.random() - 0.5F);
            Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
            dist = shroudParams.maxOffset * shroud.getShroudParams().overloadArcOffsetMult;
            dist = dist * 0.5F + dist * 0.5F * (float)Math.random();
            dist *= 0.5F;
            dir.scale(dist);
            Vector2f to = Vector2f.add(from, dir, new Vector2f());
            arc = engine.spawnEmpArcVisual(from, ship, to, ship, thickness, color, core, params);
            arc.setCoreWidthOverride(shroudParams.overloadArcCoreThickness);
            arc.setSingleFlickerMode(false);
         }

         float explosionDelay = params.movementDurOverride * 0.8F;
         Global.getCombatEngine()
            .addPlugin(
               new DelayedCombatActionPlugin(
                  explosionDelay,
                  () -> {
                     DamagingExplosionSpec spec1 = new DamagingExplosionSpec(
                        0.1F,
                        explosionRadius,
                        explosionRadius * 0.5F,
                        damage,
                        damage / 2.0F,
                        CollisionClass.PROJECTILE_NO_FF,
                        CollisionClass.GAS_CLOUD,
                        3.0F,
                        3.0F,
                        0.5F,
                        0,
                        new Color(255, 255, 255, 0),
                        new Color(255, 100, 100, 0)
                     );
                     spec1.setMinEMPDamage(emp * 0.5F);
                     spec1.setMaxEMPDamage(emp);
                     spec1.setDamageType(DamageType.ENERGY);
                     spec1.setUseDetailedExplosion(false);
                     spec1.setSoundSetId("rift_lightning_explosion");
                     spec1.setSoundVolume(0.5F + 0.5F * mult);
                     DamagingProjectileAPI explosion = engine.spawnDamagingExplosion(spec1, ship, point);
                     Color color1 = RiftLightningEffect.RIFT_LIGHTNING_COLOR;
                     color1 = new Color(255, 75, 75, 255);
                     NEParams p = RiftCascadeMineExplosion.createStandardRiftParams(color1, 14.0F + 6.0F * mult);
                     p.fadeOut = 0.5F + 0.5F * mult;
                     p.hitGlowSizeMult = 0.6F;
                     p.thickness = 50.0F;
                     RiftCascadeMineExplosion.spawnStandardRift(explosion, p);
                  }
               )
            );
      }
   }

   public static class RecentHitData {
      Object param;
      CombatEntityAPI target;
      Vector2f point;
      DamageAPI damage;
      boolean shieldHit;
   }

   public static class ShroudedThunderheadDamageDealtMod implements DamageDealtModifier {
      public ShipAPI ship;

      public ShroudedThunderheadDamageDealtMod(ShipAPI ship) {
         this.ship = ship;
      }

      public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
         if (param instanceof DamagingProjectileAPI proj) {
            DamagingExplosionSpec spec = proj.getExplosionSpecIfExplosion();
            if (spec != null && spec.getCollisionClassIfByFighter() == CollisionClass.GAS_CLOUD) {
               return null;
            }
         } else if (damage.isDps() && !damage.isForceHardFlux() || damage.getDamage() <= 0.0F) {
            return null;
         }

         if (target != null) {
            SHUGACHAAbyssGazer.ShroudedThunderheadHullmodData data = SHUGACHAAbyssGazer.getData(this.ship);
            SHUGACHAAbyssGazer.RecentHitData hit = new SHUGACHAAbyssGazer.RecentHitData();
            hit.param = param;
            hit.target = target;
            hit.point = new Vector2f(point);
            hit.damage = damage;
            hit.shieldHit = shieldHit;
            data.recentHits.add(hit, SHUGACHAAbyssGazer.RECENT_HIT_DUR);
         }

         return null;
      }
   }

   public static class ShroudedThunderheadHullmodData {
      float untilArc = 0.0F;
      TimeoutTracker<SHUGACHAAbyssGazer.RecentHitData> recentHits = new TimeoutTracker();

      boolean hasRecentEnoughHits() {
         for (SHUGACHAAbyssGazer.RecentHitData curr : this.recentHits.getItems()) {
            float remaining = this.recentHits.getRemaining(curr);
            if (remaining >= SHUGACHAAbyssGazer.RECENT_HIT_DUR - SHUGACHAAbyssGazer.MAX_TIME_SINCE_RECENT_HIT) {
               return true;
            }
         }

         return false;
      }

      float getHitProbability() {
         float recent = this.recentHits.getItems().size() * SHUGACHAAbyssGazer.WEIGHT_PER_RECENT_HIT;
         return recent / (recent + SHUGACHAAbyssGazer.MISFIRE_WEIGHT);
      }

      SHUGACHAAbyssGazer.RecentHitData pickRecentHit() {
         if (!this.hasRecentEnoughHits()) {
            return null;
         } else {
            WeightedRandomPicker<SHUGACHAAbyssGazer.RecentHitData> picker = new WeightedRandomPicker();

            for (SHUGACHAAbyssGazer.RecentHitData curr : this.recentHits.getItems()) {
               float remaining = this.recentHits.getRemaining(curr);
               if (!(remaining < SHUGACHAAbyssGazer.RECENT_HIT_DUR - SHUGACHAAbyssGazer.MAX_TIME_SINCE_RECENT_HIT * 2.0F)) {
                  picker.add(curr, SHUGACHAAbyssGazer.WEIGHT_PER_RECENT_HIT);
               }
            }

            SHUGACHAAbyssGazer.RecentHitData misfire = new SHUGACHAAbyssGazer.RecentHitData();
            picker.add(misfire, SHUGACHAAbyssGazer.MISFIRE_WEIGHT);
            return (SHUGACHAAbyssGazer.RecentHitData)picker.pick();
         }
      }
   }
}
