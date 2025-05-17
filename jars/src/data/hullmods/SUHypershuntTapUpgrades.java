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
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.HullModItemManager;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.everyframe.SUHullmodDisplayBlockScript;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import java.util.List;
import lunalib.lunaSettings.LunaSettings;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

public class SUHypershuntTapUpgrades extends BaseHullMod {
   public static final String DATA_PREFIX = "hypershunt_shu_check_";
   public static final String DATA_KEY = "shu_fluxdischarge_data_key";
   public static final String ITEM = "coronal_portal";
   private static final float VENT_BONUS = 60.0F;
   private static final float HARD_FLUX_DISSIPATION_PERCENT = 75.0F;
   private static final float FLUX_DISSIPATION_MULT = 1.1F;
   private static final float OVERLOAD_DURATION_MULT = 1.2F;
   private static final float FLUX_LEVEL = 0.6F;
   private static final float FLUX_SHOCKWAVE_MIN_DAMAGE = 100.0F;
   private static final float SHOCKWAVE_EMP_DAMAGE_AMOUNT = 500.0F;
   private static final float SHOCKWAVE_PUSH_RADIUS = 600.0F;
   private static final float FIGHTER_MULT = 300.0F;
   private static final float FRIGATE_MULT = 200.0F;
   private static final float DESTROYER_MULT = 150.0F;
   private static final float CRUISER_MULT = 110.0F;
   private static final float CAPITAL_MULT = 100.0F;
   private static final float ASTEROID_MULT = 400.0F;
   private static final float FLUX_INTERVAL_MIN = 1.4F;
   private static final float FLUX_INTERVAL_MAX = 1.8F;
   private static final Color EMP_COLOR = new Color(205, 205, 205, 0);
   private final IntervalUtil interval = new IntervalUtil(1.5F, 1.7F);
   private final IntervalUtil intervalShockwave = new IntervalUtil(0.2F, 0.24999F);
   private Vector2f LocPulse;
   private final String ID;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableOverloadDuration = SUPlugin.DISABLE_OVERLOAD_DURATION_HYPERSHUNT;
   boolean disableExtraEffect = SUPlugin.DISABLE_CORONALTAPHMOD_EXTRA_EFFECT;
   boolean disableResistantFluxConduitsIncompat = SUPlugin.DISABLE_FLUXBREAKERS_INCOMPATIBILITY;
   float ventBonus = SUPlugin.CM_HYPERSHUNT_VENT_BONUS;
   float hardFluxDissipation = SUPlugin.CM_HYPERSHUNT_HARD_FLUX_DISSIPATION_PERCENT;
   float fluxDissipation = SUPlugin.CM_HYPERSHUNT_FLUX_DISSIPATION_MULT;
   boolean toggleGeneralIncompat;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};


   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_hypershunt_upgrades") ? ALL_INCOMPAT_IDS : null;
   }

   public SUHypershuntTapUpgrades() {
      this.ID = "SUHypershuntTapUpgrades";
   }

   public CargoStackAPI getRequiredItem() {
      return Global.getSettings().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData("coronal_portal", null), null);
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
            TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("coronal_portal").getIconName(), 20.0F);
            text.addPara("Requires " + aOrAn + " %s to install.", opad, color, new String[]{name});
            tooltip.addImageWithText(5.0F);
         } else if (currentVariant != null && member != null) {
            if (currentVariant.hasHullMod(this.spec.getId())) {
               if (!currentVariant.getHullSpec().getBuiltInMods().contains(this.spec.getId())) {
                  Color color = Misc.getPositiveHighlightColor();
                  TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("coronal_portal").getIconName(), 20.0F);
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

               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("coronal_portal").getIconName(), 20.0F);
               text3.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", color, opad);
               tooltip.addImageWithText(5.0F);
            }
         }
      }
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.disableOverloadDuration = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableOverloadDurationHypershuntToggle");
         this.ventBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_HYPERSHUNT_VENT_BONUS");
         this.hardFluxDissipation = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_HYPERSHUNT_HARD_FLUX_DISSIPATION_PERCENT");
         this.fluxDissipation = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_HYPERSHUNT_FLUX_DISSIPATION_MULT");
      }

      if (this.enableCustomSM) {
         stats.getVentRateMult().modifyFlat(id, 1.0F - this.ventBonus / 100.0F);
         stats.getHardFluxDissipationFraction().modifyFlat(id, 1.0F - this.hardFluxDissipation / 100.0F);
         stats.getFluxDissipation().modifyMult(id, 1.0F + 0.01F * this.fluxDissipation);
      } else if (!this.enableCustomSM) {
         stats.getVentRateMult().modifyFlat(id, 0.59999996F);
         stats.getHardFluxDissipationFraction().modifyFlat(id, 0.75F);
         stats.getFluxDissipation().modifyMult(id, 1.1F);
      }

      if (!this.disableOverloadDuration) {
         stats.getOverloadTimeMod().modifyMult(id, 1.2F);
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
         this.disableResistantFluxConduitsIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableResistantFluxConduitsIncompatibilityToggle");
      }

      if (!this.disableResistantFluxConduitsIncompat && ship.getVariant().getHullMods().contains("fluxbreakers")) {
         ship.getVariant().removeMod("fluxbreakers");
         SUHullmodDisplayBlockScript.showBlocked(ship);
      }

      if (ship.getVariant().getSMods().contains("specialsphmod_hypershunt_upgrades")
         || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_hypershunt_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_hypershunt_upgrades");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_hypershunt_upgrades")) {
         ship.getVariant().addPermaMod("specialsphmod_hypershunt_utilityscript");
         if (currentShipStats != null) {
            SUHullmodUpgradeInstaller.applyHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_hypershunt_extension");
         }
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      CombatEngineAPI engine = Global.getCombatEngine();
      List<CombatEntityAPI> entities = CombatUtils.getEntitiesWithinRange(ship.getLocation(), 600.0F);
      float variance = MathUtils.getRandomNumberInRange(-0.23F, 0.3F);
      int size = entities.size();
      Vector2f loc = new Vector2f(ship.getLocation());
      float rotation = (float)Math.random() * 360.0F;
      if (SUPlugin.HASLUNALIB) {
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableHypeshuntFluxDischargeToggle");
      }

      if (!this.disableExtraEffect) {
         String key = "shu_fluxdischarge_data_key_" + ship.getId();
         SUHypershuntTapUpgrades.FluxSurgePulseData data = (SUHypershuntTapUpgrades.FluxSurgePulseData)engine.getCustomData().get(key);
         if (data == null) {
            data = new SUHypershuntTapUpgrades.FluxSurgePulseData();
            engine.getCustomData().put(key, data);
         }

         if (!data.runOnce) {
            data.runOnce = true;
            data.timer.randomize();
         }

         if (!ship.isAlive()) {
            return;
         }

         if (ship.getFluxLevel() >= 0.6F) {
            engine.maintainStatusForPlayerShip(
               this.ID + "_TOOLTIP_ONE", Global.getSettings().getSpriteName("tooltips", "flux_pulse"), "Flux Discharge: Ready!", "Vent to release", false
            );
         }

         this.interval.advance(engine.getElapsedInLastFrame());
         if (ship.getFluxTracker().isVenting()) {
            ship.getVentCoreColor().brighter();
            if (ship.getFluxLevel() >= 0.5F && this.interval.intervalElapsed()) {
               for (CombatEntityAPI EnemyShip : AIUtils.getNearbyEnemies(ship, 600.0F)) {
                  for (int x = 0; x < 1; x++) {
                     Global.getCombatEngine()
                        .spawnEmpArc(
                           ship, ship.getLocation(), EnemyShip, EnemyShip, DamageType.ENERGY, 350.0F, 175.0F, 60000.0F, "", 60.0F, EMP_COLOR, EMP_COLOR
                        );
                  }
               }

               loc.x = (float)(loc.x - 8.0 * FastTrig.cos(ship.getFacing() * Math.PI / 180.0));
               loc.y = (float)(loc.y - 8.0 * FastTrig.sin(ship.getFacing() * Math.PI / 180.0));
               this.LocPulse = loc;
               Global.getSoundPlayer().playSound("sphum_fluxshunt_boom", 1.0F + variance, 1.0F + variance, ship.getLocation(), ship.getVelocity());
               RippleDistortion ripple = new RippleDistortion(ship.getLocation(), ship.getVelocity());
               ripple.setSize(650.0F);
               ripple.setIntensity(ship.getCollisionRadius());
               ripple.setFrameRate(60.0F);
               ripple.fadeInSize(0.7F);
               ripple.fadeOutIntensity(0.4F);
               DistortionShader.addDistortion(ripple);
               MagicRender.battlespace(
                  Global.getSettings().getSprite("misc", "fluxshunt_shockwave"),
                  this.LocPulse,
                  new Vector2f(),
                  new Vector2f(500.0F, 500.0F),
                  new Vector2f(3000.0F, 3000.0F),
                  rotation,
                  0.0F,
                  ship.getVentCoreColor(),
                  true,
                  0.0F,
                  0.0F,
                  0.0F,
                  0.0F,
                  0.0F,
                  0.35F,
                  0.0F,
                  0.2F,
                  CombatEngineLayers.BELOW_SHIPS_LAYER
               );

               for (int i = 0; i < size; i++) {
                  CombatEntityAPI targetShips = entities.get(i);
                  if (targetShips != ship) {
                     float damage_modifier = 1.0F - MathUtils.getDistance(ship, targetShips) / 600.0F;
                     float fluxpulse_modifier = 400.0F * damage_modifier;
                     float shockwave_damage_modifier = 0.0F;
                     float emp_damage_modifier = 500.0F * damage_modifier;
                     if (targetShips instanceof ShipAPI enemy) {
                        if (null != enemy.getHullSize()) {
                           switch (enemy.getHullSize()) {
                              case FIGHTER:
                                 fluxpulse_modifier = 300.0F * damage_modifier;
                                 shockwave_damage_modifier = 100.0F;
                                 break;
                              case FRIGATE:
                                 fluxpulse_modifier = 200.0F * damage_modifier;
                                 shockwave_damage_modifier = 100.0F;
                                 break;
                              case DESTROYER:
                                 fluxpulse_modifier = 150.0F * damage_modifier;
                                 shockwave_damage_modifier = 100.0F;
                                 break;
                              case CRUISER:
                                 fluxpulse_modifier = 110.0F * damage_modifier;
                                 shockwave_damage_modifier = 100.0F;
                                 break;
                              case CAPITAL_SHIP:
                                 fluxpulse_modifier = 100.0F * damage_modifier;
                                 shockwave_damage_modifier = 100.0F;
                           }
                        }

                        if (enemy.getOwner() == ship.getOwner()) {
                           shockwave_damage_modifier *= 0.0F;
                           emp_damage_modifier *= 0.0F;
                           fluxpulse_modifier *= 0.0F;
                        }

                        if (enemy.getShield() != null && enemy.getShield().isOn() && enemy.getShield().isWithinArc(ship.getLocation())) {
                           enemy.getFluxTracker().increaseFlux(shockwave_damage_modifier * 2.0F, true);
                        } else {
                           ShipAPI empTarget = enemy;

                           for (int x = 0; x < 5; x++) {
                              engine.spawnEmpArc(
                                 ship,
                                 MathUtils.getRandomPointInCircle(enemy.getLocation(), enemy.getCollisionRadius()),
                                 empTarget,
                                 empTarget,
                                 DamageType.ENERGY,
                                 shockwave_damage_modifier,
                                 emp_damage_modifier / 2.0F,
                                 600.0F,
                                 null,
                                 2.0F,
                                 EMP_COLOR,
                                 EMP_COLOR
                              );
                           }
                        }
                     }

                     Vector2f dir = VectorUtils.getDirectionalVector(ship.getLocation(), targetShips.getLocation());
                     dir.scale(fluxpulse_modifier);
                     Vector2f.add(targetShips.getVelocity(), dir, targetShips.getVelocity());
                  }
               }
            }
         }
      }

      if (Global.getSettings().getModManager().isModEnabled("Sunrider") && ship.getVariant().getHullSpec().getBaseHullId().contains("Sunridership")) {
         this.intervalShockwave.advance(engine.getElapsedInLastFrame());
         if (ship.getSystem().isChargedown() && this.intervalShockwave.intervalElapsed()) {
            for (CombatEntityAPI EnemyShip : AIUtils.getNearbyEnemies(ship, 600.0F)) {
               for (int x = 0; x < 1; x++) {
                  Global.getCombatEngine()
                     .spawnEmpArc(ship, ship.getLocation(), EnemyShip, EnemyShip, DamageType.ENERGY, 350.0F, 175.0F, 60000.0F, "", 60.0F, EMP_COLOR, EMP_COLOR);
               }
            }

            loc.x = (float)(loc.x - 8.0 * FastTrig.cos(ship.getFacing() * Math.PI / 180.0));
            loc.y = (float)(loc.y - 8.0 * FastTrig.sin(ship.getFacing() * Math.PI / 180.0));
            this.LocPulse = loc;
            Global.getSoundPlayer().playSound("sphum_fluxshunt_boom", 1.0F + variance, 1.0F + variance, ship.getLocation(), ship.getVelocity());
            RippleDistortion ripple = new RippleDistortion(ship.getLocation(), ship.getVelocity());
            ripple.setSize(650.0F);
            ripple.setIntensity(ship.getCollisionRadius());
            ripple.setFrameRate(60.0F);
            ripple.fadeInSize(0.7F);
            ripple.fadeOutIntensity(0.4F);
            DistortionShader.addDistortion(ripple);
            MagicRender.battlespace(
               Global.getSettings().getSprite("misc", "fluxshunt_shockwave"),
               this.LocPulse,
               new Vector2f(),
               new Vector2f(700.0F, 700.0F),
               new Vector2f(3000.0F, 3000.0F),
               rotation,
               0.0F,
               ship.getVentCoreColor(),
               true,
               0.0F,
               0.0F,
               0.0F,
               0.0F,
               0.0F,
               0.35F,
               0.0F,
               0.2F,
               CombatEngineLayers.BELOW_SHIPS_LAYER
            );

            for (int ix = 0; ix < size; ix++) {
               CombatEntityAPI targetShips = entities.get(ix);
               if (targetShips != ship) {
                  float damage_modifier = 1.0F - MathUtils.getDistance(ship, targetShips) / 600.0F;
                  float fluxpulse_modifier = 400.0F * damage_modifier;
                  float shockwave_damage_modifier = 0.0F;
                  float emp_damage_modifier = 500.0F * damage_modifier;
                  if (targetShips instanceof ShipAPI enemy) {
                     if (null != enemy.getHullSize()) {
                        switch (enemy.getHullSize()) {
                           case FIGHTER:
                              fluxpulse_modifier = 300.0F * damage_modifier;
                              shockwave_damage_modifier = 100.0F;
                              break;
                           case FRIGATE:
                              fluxpulse_modifier = 200.0F * damage_modifier;
                              shockwave_damage_modifier = 100.0F;
                              break;
                           case DESTROYER:
                              fluxpulse_modifier = 150.0F * damage_modifier;
                              shockwave_damage_modifier = 100.0F;
                              break;
                           case CRUISER:
                              fluxpulse_modifier = 110.0F * damage_modifier;
                              shockwave_damage_modifier = 100.0F;
                              break;
                           case CAPITAL_SHIP:
                              fluxpulse_modifier = 100.0F * damage_modifier;
                              shockwave_damage_modifier = 100.0F;
                        }
                     }

                     if (enemy.getOwner() == ship.getOwner()) {
                        shockwave_damage_modifier *= 0.0F;
                        emp_damage_modifier *= 0.0F;
                        fluxpulse_modifier *= 0.0F;
                     }

                     if (enemy.getShield() != null && enemy.getShield().isOn() && enemy.getShield().isWithinArc(ship.getLocation())) {
                        enemy.getFluxTracker().increaseFlux(shockwave_damage_modifier * 2.0F, true);
                     } else {
                        ShipAPI empTarget = enemy;

                        for (int x = 0; x < 5; x++) {
                           engine.spawnEmpArc(
                              ship,
                              MathUtils.getRandomPointInCircle(enemy.getLocation(), enemy.getCollisionRadius()),
                              empTarget,
                              empTarget,
                              DamageType.ENERGY,
                              shockwave_damage_modifier,
                              emp_damage_modifier / 2.0F,
                              600.0F,
                              null,
                              2.0F,
                              EMP_COLOR,
                              EMP_COLOR
                           );
                        }
                     }
                  }

                  Vector2f dir = VectorUtils.getDirectionalVector(ship.getLocation(), targetShips.getLocation());
                  dir.scale(fluxpulse_modifier);
                  Vector2f.add(targetShips.getVelocity(), dir, targetShips.getVelocity());
               }
            }
         }
      }
   }

   public boolean isApplicableToShip(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (ship.getVariant().getHullSize() != HullSize.CAPITAL_SHIP) {
            return false;
         } else {
            if (SUPlugin.HASLUNALIB) {
               this.disableResistantFluxConduitsIncompat = LunaSettings.getBoolean(
                  "mayu_specialupgrades", "shu_disableResistantFluxConduitsIncompatibilityToggle"
               );
            }

            if (!this.disableResistantFluxConduitsIncompat && ship.getVariant().hasHullMod("fluxbreakers")) {
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
         if (ship.getVariant().getHullSize() != HullSize.CAPITAL_SHIP) {
            return "Can only be installed on capital ships";
         } else {
            if (SUPlugin.HASLUNALIB) {
               this.disableResistantFluxConduitsIncompat = LunaSettings.getBoolean(
                  "mayu_specialupgrades", "shu_disableResistantFluxConduitsIncompatibilityToggle"
               );
            }

            if (!this.disableResistantFluxConduitsIncompat && ship.getVariant().hasHullMod("fluxbreakers")) {
               return "Incompatible with Resistant Flux Conduits";
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
            this.disableOverloadDuration = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableOverloadDurationHypershuntToggle");
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableHypeshuntFluxDischargeToggle");
            this.disableResistantFluxConduitsIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableResistantFluxConduitsIncompatibilityToggle");
            this.ventBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_HYPERSHUNT_VENT_BONUS");
            this.hardFluxDissipation = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_HYPERSHUNT_HARD_FLUX_DISSIPATION_PERCENT");
            this.fluxDissipation = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_HYPERSHUNT_FLUX_DISSIPATION_MULT");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Increases flux dissipation: %s\n• Dissipates hard flux while shields are up: %s\n• Improves active vent rate: %s",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(SUPlugin.CM_HYPERSHUNT_FLUX_DISSIPATION_MULT) + "%",
                  Misc.getRoundedValue(SUPlugin.CM_HYPERSHUNT_HARD_FLUX_DISSIPATION_PERCENT) + "%",
                  Misc.getRoundedValue(SUPlugin.CM_HYPERSHUNT_VENT_BONUS) + "%"
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Increases flux dissipation: %s\n• Dissipates hard flux while shields are up: %s\n• Improves active vent rate: %s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{Misc.getRoundedValue(10.0F) + "%", Misc.getRoundedValue(25.0F) + "%", Misc.getRoundedValue(40.0F) + "%"}
            );
         }

         if (!this.disableOverloadDuration) {
            tooltip.addPara(
               "• Overload duration is increased: %s",
               SUStringCodex.SHU_TOOLTIP_NEG,
               SUStringCodex.SHU_TOOLTIP_RED,
               new String[]{Misc.getRoundedValue(20.0F) + "%"}
            );
         }

         if (!this.disableExtraEffect) {
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addSectionHeading("Extra System", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI extrasystxt = tooltip.beginImageWithText(
                  Global.getSettings().getSpriteName("tooltips", "flux_pulse"), SUStringCodex.SHU_TOOLTIP_IMG
               );
               extrasystxt.addPara(
                  "Flux Discharge",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                  new String[]{"Flux Discharge"}
               );
               extrasystxt.addPara(
                  "When the ship's flux level is above %s, the built-up flux can be released as a deadly discharge by venting.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{Misc.getRoundedValue(50.0F) + "%"}
               );
               tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
               tooltip.addPara(
                  "• Unleashes flux shockwaves that push nearby enemies away.\n• The concentrated flux discharge deals %s energy damage to ships and fighters.",
                  SUStringCodex.SHU_TOOLTIP_PADMAIN,
                  SUStringCodex.SHU_HULLMOD_NEGATIVE_TEXT_COLOR,
                  new String[]{Misc.getRoundedValue(300.0F)}
               );
               if (Global.getSettings().getModManager().isModEnabled("Sunrider") && ship.getVariant().getHullSpec().getBaseHullId().contains("Sunridership")) {
                  tooltip.addSectionHeading("Sunrider Interaction", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
                  TooltipMakerAPI extrasyssunrider = tooltip.beginImageWithText(
                     Global.getSettings().getSpriteName("tooltips", "sunrider_collaboration"), SUStringCodex.SHU_TOOLTIP_IMG
                  );
                  extrasyssunrider.addPara(
                     "Ceran Engineering Synergy",
                     SUStringCodex.SHU_TOOLTIP_PADZERO,
                     Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                     new String[]{"Ceran Engineering Synergy"}
                  );
                  extrasyssunrider.addPara(
                     "The installed Coronal Hypershunt tap creates unusual synergy with the ship's Short Range Warp system. The innate Flux Discharge ability %s upon post-warp regardless of the ship's flux level.",
                     SUStringCodex.SHU_TOOLTIP_PADZERO,
                     Misc.getHighlightColor(),
                     new String[]{"activates"}
                  );
                  tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
               }
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
            if (!this.disableResistantFluxConduitsIncompat) {
               text.addPara(
                  "Not compatible with %s, %s",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getNegativeHighlightColor(),
                  new String[]{"Other Special Upgrade Hullmods", "Resistant Flux Conduits"}
               );
            } else if (this.disableResistantFluxConduitsIncompat) {
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

   public static class FluxSurgePulseData {
      final IntervalUtil timer = new IntervalUtil(1.4F, 1.8F);
      boolean runOnce = false;
   }
}
