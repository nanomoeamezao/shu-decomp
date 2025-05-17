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
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lunalib.lunaSettings.LunaSettings;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

public class SUCryoarithmeticEngineUpgrades extends BaseHullMod {
   public static final String DATA_PREFIX = "cryoarithmetic_engine_shu_check_";
   public static final String ITEM = "cryoarithmetic_engine";
   private static final float FLUX_REDUC_PERCENT = 25.0F;
   private static final float ENGINE_HEALTH_BONUS = 100.0F;
   public static final Color FLARE_SMOKE = new Color(100, 100, 245, 175);
   public final IntervalUtil flareInterval = new IntervalUtil(0.05F, 0.05F);
   public final String ID;
   public static final float NEBULA_SIZE = 20.0F * (0.75F + (float)Math.random() * 0.5F);
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_CRYOARITHMETICHMOD_EXTRA_EFFECT;
   float fluxReductionBonus = SUPlugin.CM_CRYOARITHMETICENGINE_FLUX_REDUC_PERCENT;
   float engineHealthBonus = SUPlugin.CM_CRYOARITHMETICENGINE_ENGINE_HEALTH_BONUS;
   float speedBonusFrigate = SUPlugin.CM_CRYOARITHMETICENGINE_SPEED_FRIGATE_BONUS;
   float speedBonusDestroyer = SUPlugin.CM_CRYOARITHMETICENGINE_SPEED_DESTROYER_BONUS;
   float speedBonusCruiser = SUPlugin.CM_CRYOARITHMETICENGINE_SPEED_CRUISER_BONUS;
   float speedBonusCapital = SUPlugin.CM_CRYOARITHMETICENGINE_SPEED_CAPITAL_BONUS;
   boolean toggleGeneralIncompat;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};

   private static final Map IAMSPEED = new HashMap();
   public static final Map<HullSize, Integer> CRYOFLARES = new HashMap<>();
   public static final Map<HullSize, Float> ENGAGEMENT_RANGE = new HashMap<>();

   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_cryoarithmeticengine_upgrades") ? ALL_INCOMPAT_IDS : null;
   }

   public SUCryoarithmeticEngineUpgrades() {
      this.ID = "SUCryoarithmeticEngineUpgrades";
   }

   public CargoStackAPI getRequiredItem() {
      return Global.getSettings().createCargoStack(CargoItemType.SPECIAL, new SpecialItemData("cryoarithmetic_engine", null), null);
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
            TooltipMakerAPI text = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("cryoarithmetic_engine").getIconName(), 20.0F);
            text.addPara("Requires " + aOrAn + " %s to install.", opad, color, new String[]{name});
            tooltip.addImageWithText(5.0F);
         } else if (currentVariant != null && member != null) {
            if (currentVariant.hasHullMod(this.spec.getId())) {
               if (!currentVariant.getHullSpec().getBuiltInMods().contains(this.spec.getId())) {
                  Color color = Misc.getPositiveHighlightColor();
                  TooltipMakerAPI text2 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("cryoarithmetic_engine").getIconName(), 20.0F);
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

               TooltipMakerAPI text3 = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("cryoarithmetic_engine").getIconName(), 20.0F);
               text3.addPara("Requires item: " + req.getDisplayName() + " (" + available + " available)", color, opad);
               tooltip.addImageWithText(5.0F);
            }
         }
      }
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.fluxReductionBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CRYOARITHMETICENGINE_FLUX_REDUC_PERCENT");
         this.engineHealthBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CRYOARITHMETICENGINE_ENGINE_HEALTH_BONUS");
         this.speedBonusFrigate = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CRYOARITHMETICENGINE_SPEED_FRIGATE_BONUS");
         this.speedBonusDestroyer = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CRYOARITHMETICENGINE_SPEED_DESTROYER_BONUS");
         this.speedBonusCruiser = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CRYOARITHMETICENGINE_SPEED_CRUISER_BONUS");
         this.speedBonusCapital = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CRYOARITHMETICENGINE_SPEED_CAPITAL_BONUS");
      }

      if (this.enableCustomSM) {
         stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -this.fluxReductionBonus);
         stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -this.fluxReductionBonus);
         stats.getBeamWeaponFluxCostMult().modifyPercent(id, -this.fluxReductionBonus);
         stats.getMissileWeaponFluxCostMod().modifyPercent(id, -this.fluxReductionBonus);
         stats.getEngineHealthBonus().modifyPercent(id, this.engineHealthBonus);
         HullSize shipSize = stats.getVariant().getHullSpec().getHullSize();
         if (shipSize != null) {
            switch (shipSize) {
               case FRIGATE:
                  stats.getMaxSpeed().modifyFlat(id, this.speedBonusFrigate);
                  stats.getAcceleration().modifyPercent(id, this.speedBonusFrigate);
                  stats.getDeceleration().modifyPercent(id, this.speedBonusFrigate);
                  break;
               case DESTROYER:
                  stats.getMaxSpeed().modifyFlat(id, this.speedBonusDestroyer);
                  stats.getAcceleration().modifyPercent(id, this.speedBonusDestroyer);
                  stats.getDeceleration().modifyPercent(id, this.speedBonusDestroyer);
                  break;
               case CRUISER:
                  stats.getMaxSpeed().modifyFlat(id, this.speedBonusCruiser);
                  stats.getAcceleration().modifyPercent(id, this.speedBonusCruiser);
                  stats.getDeceleration().modifyPercent(id, this.speedBonusCruiser);
                  break;
               case CAPITAL_SHIP:
                  stats.getMaxSpeed().modifyFlat(id, this.speedBonusCapital);
                  stats.getAcceleration().modifyPercent(id, this.speedBonusCapital);
                  stats.getDeceleration().modifyPercent(id, this.speedBonusCapital);
                  break;
               case FIGHTER:
                  stats.getMaxSpeed().modifyFlat(id, 0.0F);
                  stats.getAcceleration().modifyPercent(id, 0.0F);
                  stats.getDeceleration().modifyPercent(id, 0.0F);
            }
         }
      } else if (!this.enableCustomSM) {
         stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -25.0F);
         stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -25.0F);
         stats.getBeamWeaponFluxCostMult().modifyPercent(id, -25.0F);
         stats.getMissileWeaponFluxCostMod().modifyPercent(id, -25.0F);
         stats.getEngineHealthBonus().modifyPercent(id, 100.0F);
         stats.getMaxSpeed().modifyFlat(id, (Float)IAMSPEED.get(hullSize));
         stats.getAcceleration().modifyPercent(id, (Float)IAMSPEED.get(hullSize));
         stats.getDeceleration().modifyPercent(id, (Float)IAMSPEED.get(hullSize));
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

      if (ship.getVariant().getSMods().contains("specialsphmod_cryoarithmeticengine_upgrades")
         || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_cryoarithmeticengine_upgrades")) {
         ship.getVariant().removePermaMod("specialsphmod_cryoarithmeticengine_upgrades");
      }

      if (ship.getVariant().hasHullMod("specialsphmod_cryoarithmeticengine_upgrades")) {
         ship.getVariant().addPermaMod("specialsphmod_cryoarithmeticengine_utilityscript");
         if (currentShipStats != null) {
            SUHullmodUpgradeInstaller.applyHullmodToModulesOfShip(shipVariant, currentShipStats, "specialsphmod_cryoarithmeticengine_extension");
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
            String CRYOENGDataKey = ship.getId() + "_shuqe_reload_data";
            Object targetDataObj = Global.getCombatEngine().getCustomData().get(CRYOENGDataKey);
            if (targetDataObj == null) {
               Global.getCombatEngine()
                  .getCustomData()
                  .put(CRYOENGDataKey, new SUCryoarithmeticEngineUpgrades.QHData(ship.getHullSize(), 3.0F, CRYOFLARES.get(ship.getHullSize())));
            } else if (ship.getHullSize() != ((SUCryoarithmeticEngineUpgrades.QHData)targetDataObj).size) {
               Global.getCombatEngine().getCustomData().remove(CRYOENGDataKey);
               Global.getCombatEngine()
                  .getCustomData()
                  .put(CRYOENGDataKey, new SUCryoarithmeticEngineUpgrades.QHData(ship.getHullSize(), 3.0F, CRYOFLARES.get(ship.getHullSize())));
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
                  String status = "LAUNCHING CRYOFLARES";
                  if (ship == playerShip) {
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

            List<MissileAPI> missiles = AIUtils.getNearbyEnemyMissiles(ship, ENGAGEMENT_RANGE.get(ship.getHullSize()));
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
            Global.getCombatEngine().addSmokeParticle(location, ship.getVelocity(), 20.0F, 0.4F, 0.5F, FLARE_SMOKE);
            Global.getCombatEngine().addNebulaSmokeParticle(location, ship.getVelocity(), NEBULA_SIZE / 4.0F, 15.0F, 0.1F, 0.3F, 2.0F, FLARE_SMOKE);
            Global.getCombatEngine().addNebulaSmokeParticle(location2, ship.getVelocity(), NEBULA_SIZE / 2.0F, 15.0F, 0.1F, 0.3F, 2.0F, FLARE_SMOKE);
            Global.getCombatEngine().addNebulaSmokeParticle(location3, ship.getVelocity(), NEBULA_SIZE / 2.0F, 15.0F, 0.1F, 0.3F, 2.0F, FLARE_SMOKE);
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
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableCryoarithmeticEngineCFlaresToggle");
            this.fluxReductionBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CRYOARITHMETICENGINE_FLUX_REDUC_PERCENT");
            this.engineHealthBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CRYOARITHMETICENGINE_ENGINE_HEALTH_BONUS");
            this.speedBonusFrigate = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CRYOARITHMETICENGINE_SPEED_FRIGATE_BONUS");
            this.speedBonusDestroyer = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CRYOARITHMETICENGINE_SPEED_DESTROYER_BONUS");
            this.speedBonusCruiser = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CRYOARITHMETICENGINE_SPEED_CRUISER_BONUS");
            this.speedBonusCapital = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_CRYOARITHMETICENGINE_SPEED_CAPITAL_BONUS");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Reduces weapon flux cost: %s\n• Increases engine durability: %s\n• Improves top speed: %s/%s/%s/%s (by hull size)",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(this.fluxReductionBonus) + "%",
                  Misc.getRoundedValue(this.engineHealthBonus) + "%",
                  Misc.getRoundedValue(this.speedBonusFrigate),
                  Misc.getRoundedValue(this.speedBonusDestroyer),
                  Misc.getRoundedValue(this.speedBonusCruiser),
                  Misc.getRoundedValue(this.speedBonusCapital)
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Reduces weapon flux cost: %s\n• Increases engine durability: %s\n• Improves top speed: %s/%s/%s/%s (by hull size)",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(25.0F) + "%",
                  Misc.getRoundedValue(100.0F) + "%",
                  Misc.getRoundedValue(20.0F),
                  Misc.getRoundedValue(15.0F),
                  Misc.getRoundedValue(10.0F),
                  Misc.getRoundedValue(5.0F)
               }
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

   static {
      IAMSPEED.put(HullSize.FIGHTER, 0.0F);
      IAMSPEED.put(HullSize.FRIGATE, 20.0F);
      IAMSPEED.put(HullSize.DESTROYER, 15.0F);
      IAMSPEED.put(HullSize.CRUISER, 10.0F);
      IAMSPEED.put(HullSize.CAPITAL_SHIP, 5.0F);
      CRYOFLARES.put(HullSize.FIGHTER, 4);
      CRYOFLARES.put(HullSize.FRIGATE, 4);
      CRYOFLARES.put(HullSize.DESTROYER, 4);
      CRYOFLARES.put(HullSize.CRUISER, 4);
      CRYOFLARES.put(HullSize.CAPITAL_SHIP, 4);
      ENGAGEMENT_RANGE.put(HullSize.FIGHTER, 300.0F);
      ENGAGEMENT_RANGE.put(HullSize.FRIGATE, 300.0F);
      ENGAGEMENT_RANGE.put(HullSize.DESTROYER, 400.0F);
      ENGAGEMENT_RANGE.put(HullSize.CRUISER, 500.0F);
      ENGAGEMENT_RANGE.put(HullSize.CAPITAL_SHIP, 600.0F);
   }

   public static class QHData {
      public HullSize size;
      public float attackAngle;
      public int flaresLeft;
      public float reloadTime;
      public float maxReload;
      public int maxFlares;
      public boolean isReloading;
      public final IntervalUtil reloadingInterval;

      public QHData(HullSize size, float reloadTime, int flares) {
         this.size = size;
         this.reloadTime = 0.0F;
         this.maxReload = reloadTime;
         this.maxFlares = flares;
         this.flaresLeft = flares;
         this.isReloading = false;
         this.reloadingInterval = new IntervalUtil(reloadTime, reloadTime);
      }
   }
}
