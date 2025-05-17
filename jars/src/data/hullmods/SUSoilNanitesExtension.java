package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import lunalib.lunaSettings.LunaSettings;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

public class SUSoilNanitesExtension extends BaseHullMod {
   private static final float REPAIR_BONUS = 60.0F;
   private static final float ARMOR_VENT = 0.5F;
   private static final float ARMOR_VENT_VALUE = 50.0F;
   private static final float NANITE_PARTICLE_OPACITY = 0.85F;
   private static final float NANITE_PARTICLE_RADIUS = 130.0F;
   private static final float NANITE_PARTICLE_SIZE = 5.0F;
   private static final int MAX_NANITE_PARTICLES_PER_FRAME = 10;
   private static final Color NANITE_REPAIR_COLOR = new Color(205, 205, 235, 45);
   private final String ID;
   boolean enableCustomSM = SUPlugin.ENABLE_CUSTOM_STATS_MODE;
   boolean disableExtraEffect = SUPlugin.DISABLE_SOILNANITEHMOD_EXTRA_EFFECT;
   boolean disableAutomatedRepairUnitIncompat = SUPlugin.DISABLE_AUTOMATEDREPAIRUNIT_INCOMPATIBILITY;
   float repairBonus = SUPlugin.CM_SOILNANITE_COMBAT_REPAIR_BONUS;
   float armorFrigate = SUPlugin.CM_SOILNANITE_ARMOR_FRIGATE_BONUS;
   float armorDestroyer = SUPlugin.CM_SOILNANITE_ARMOR_DESTROYER_BONUS;
   float armorCruiser = SUPlugin.CM_SOILNANITE_ARMOR_CRUISER_BONUS;
   float armorCapital = SUPlugin.CM_SOILNANITE_ARMOR_CAPITAL_BONUS;
   boolean toggleGeneralIncompat;
   private static final String[] ALL_INCOMPAT_IDS = new String[]{};

   private static final Map armourzone = new HashMap();

   private String[] getIncompatibleIds() {
      return super.spec.getId().equals("specialsphmod_soilnanites_extension") ? ALL_INCOMPAT_IDS : null;
   }

   public SUSoilNanitesExtension() {
      this.ID = "SUSoilNanitesExtension";
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCustomSM = LunaSettings.getBoolean("mayu_specialupgrades", "shu_customStatsModeToggle");
         this.repairBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SOILNANITE_COMBAT_REPAIR_BONUS");
         this.armorFrigate = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SOILNANITE_ARMOR_FRIGATE_BONUS");
         this.armorDestroyer = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SOILNANITE_ARMOR_DESTROYER_BONUS");
         this.armorCruiser = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SOILNANITE_ARMOR_CRUISER_BONUS");
         this.armorCapital = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SOILNANITE_ARMOR_CAPITAL_BONUS");
      }

      if (this.enableCustomSM) {
         stats.getCombatEngineRepairTimeMult().modifyMult(id, 1.0F - this.repairBonus * 0.01F);
         stats.getCombatWeaponRepairTimeMult().modifyMult(id, 1.0F - this.repairBonus * 0.01F);
         HullSize shipSize = stats.getVariant().getHullSpec().getHullSize();
         if (shipSize != null) {
            switch (shipSize) {
               case FRIGATE:
                  stats.getArmorBonus().modifyFlat(id, this.armorFrigate);
                  break;
               case DESTROYER:
                  stats.getArmorBonus().modifyFlat(id, this.armorDestroyer);
                  break;
               case CRUISER:
                  stats.getArmorBonus().modifyFlat(id, this.armorCruiser);
                  break;
               case CAPITAL_SHIP:
                  stats.getArmorBonus().modifyFlat(id, this.armorCapital);
                  break;
               case FIGHTER:
                  stats.getArmorBonus().modifyFlat(id, 0.0F);
            }
         }
      } else if (!this.enableCustomSM) {
         stats.getArmorBonus().modifyFlat(id, (Float)armourzone.get(hullSize));
         stats.getCombatEngineRepairTimeMult().modifyMult(id, 0.40000004F);
         stats.getCombatWeaponRepairTimeMult().modifyMult(id, 0.40000004F);
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

      if (SUPlugin.HASLUNALIB) {
         this.disableAutomatedRepairUnitIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableAutomatedRepairUnitIncompatibilityToggle");
      }

      if (!this.disableAutomatedRepairUnitIncompat && ship.getVariant().getHullMods().contains("autorepair")) {
         ship.getVariant().removeMod("autorepair");
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (SUPlugin.HASLUNALIB) {
         this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableSoilNaniteNRepairToggle");
      }

      if (!Global.getCombatEngine().isPaused()) {
         if (ship.isAlive() && !ship.isHulk()) {
            if (!this.disableExtraEffect) {
               MutableShipStatsAPI stats = ship.getMutableStats();
               float percentageOfHPLeft = ship.getHitpoints() / ship.getMaxHitpoints();
               int hullPoints = Math.round((float)((int)ship.getHitpoints()));
               float maxHullPoints = ship.getMaxHitpoints() / 2.0F;
               int halfHullPoints = Math.round((float)((int)maxHullPoints));
               boolean isFucked = ship.getFluxTracker().isOverloaded();
               boolean isSussybaka = ship.getFluxTracker().isVenting();
               ShipAPI playerShip = Global.getCombatEngine().getPlayerShip();
               super.advanceInCombat(ship, amount);
               if (!isFucked && !isSussybaka && percentageOfHPLeft < 0.5F) {
                  stats.getHullCombatRepairRatePercentPerSecond().modifyFlat(this.ID, 0.4F);
                  stats.getMaxCombatHullRepairFraction().modifyFlat(this.ID, 0.5F);
                  Vector2f initialOffset = MathUtils.getRandomPointInCircle(null, 15.0F);
                  Vector2f specificOffset = MathUtils.getRandomPointInCircle(initialOffset, 10.0F);
                  ship.addAfterimage(NANITE_REPAIR_COLOR, specificOffset.x, specificOffset.y, 0.0F, 0.0F, 0.05F, 0.1F, 0.05F, 0.1F, true, false, true);
                  int numParticlesThisFrame = Math.round(percentageOfHPLeft * 10.0F);

                  for (int x = 0; x < numParticlesThisFrame; x++) {
                     Vector2f particlePos = MathUtils.getRandomPointOnCircumference(ship.getLocation(), 130.0F);
                     Vector2f particleVel = Vector2f.sub(ship.getLocation(), particlePos, null);
                     Global.getCombatEngine().addSmokeParticle(particlePos, particleVel, 5.0F, 0.85F, 1.0F, NANITE_REPAIR_COLOR);
                  }

                  if (ship == playerShip) {
                     Global.getCombatEngine()
                        .maintainStatusForPlayerShip(
                           "FORMERLY_CHUCKS_MODULE",
                           Global.getSettings().getSpriteName("tooltips", "nanite_repair"),
                           "N-Repair System: Active",
                           "Repairing Hull: " + hullPoints + " / " + halfHullPoints,
                           false
                        );
                  }
               }

               if (ship.getFluxTracker().isVenting()) {
                  stats.getArmorDamageTakenMult().modifyMult(this.ID, 0.5F);
                  stats.getShieldDamageTakenMult().modifyMult(this.ID, 0.5F);
                  stats.getHullDamageTakenMult().modifyMult(this.ID, 0.5F);
                  if (ship == playerShip) {
                     Global.getCombatEngine()
                        .maintainStatusForPlayerShip(
                           "FEED_AND_SEED_MODULE",
                           Global.getSettings().getSpriteName("tooltips", "nanite_repair"),
                           "Nanite Cluster",
                           "-" + Math.round(50.0F) + "% damage taken while venting",
                           false
                        );
                  }
               } else {
                  stats.getArmorDamageTakenMult().modifyMult(this.ID, 1.0F);
                  stats.getShieldDamageTakenMult().modifyMult(this.ID, 1.0F);
                  stats.getHullDamageTakenMult().modifyMult(this.ID, 1.0F);
               }
            }
         }
      }
   }

   public boolean isApplicableToShip(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (SUPlugin.HASLUNALIB) {
            this.disableAutomatedRepairUnitIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableAutomatedRepairUnitIncompatibilityToggle");
         }

         if (!this.disableAutomatedRepairUnitIncompat && ship.getVariant().hasHullMod("autorepair")) {
            return false;
         } else {
            return SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS)
               ? false
               : !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), this.getIncompatibleIds());
         }
      } else {
         return false;
      }
   }

   public String getUnapplicableReason(ShipAPI ship) {
      if (ship != null && ship.getVariant() != null) {
         if (SUPlugin.HASLUNALIB) {
            this.disableAutomatedRepairUnitIncompat = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableAutomatedRepairUnitIncompatibilityToggle");
         }

         if (!this.disableAutomatedRepairUnitIncompat && ship.getVariant().hasHullMod("autorepair")) {
            return "Incompatible with Automated Repair Unit";
         } else {
            return !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), ALL_INCOMPAT_IDS)
                  && !SUHullmodUpgradeInstaller.listContainsAny(ship.getVariant().getHullMods(), this.getIncompatibleIds())
               ? super.getUnapplicableReason(ship)
               : "Only one type of special upgrade hullmod can be installed per ship";
         }
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
            this.disableExtraEffect = LunaSettings.getBoolean("mayu_specialupgrades", "shu_disableSoilNaniteNRepairToggle");
            this.repairBonus = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SOILNANITE_COMBAT_REPAIR_BONUS");
            this.armorFrigate = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SOILNANITE_ARMOR_FRIGATE_BONUS");
            this.armorDestroyer = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SOILNANITE_ARMOR_DESTROYER_BONUS");
            this.armorCruiser = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SOILNANITE_ARMOR_CRUISER_BONUS");
            this.armorCapital = LunaSettings.getFloat("mayu_specialupgrades", "LUNA_CM_SOILNANITE_ARMOR_CAPITAL_BONUS");
         }

         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (this.enableCustomSM) {
            tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "custom_mode"), 370.0F, 20.0F, 2.0F);
            tooltip.addPara(
               "• Increases engine & weapon repair rate in combat: %s\n• Increases ship's armor: %s/%s/%s/%s (by hull size)",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(this.repairBonus) + "%",
                  Misc.getRoundedValue(this.armorFrigate),
                  Misc.getRoundedValue(this.armorDestroyer),
                  Misc.getRoundedValue(this.armorCruiser),
                  Misc.getRoundedValue(this.armorCapital)
               }
            );
         } else if (!this.enableCustomSM) {
            tooltip.addPara(
               "• Increases engine & weapon repair rate in combat: %s\n• Increases ship's armor: %s/%s/%s/%s (by hull size)",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(60.0F) + "%",
                  Misc.getRoundedValue(50.0F),
                  Misc.getRoundedValue(100.0F),
                  Misc.getRoundedValue(150.0F),
                  Misc.getRoundedValue(200.0F)
               }
            );
         }

         if (!this.disableExtraEffect) {
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addSectionHeading("Passive System", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI text2 = tooltip.beginImageWithText(
                  Global.getSettings().getSpriteName("tooltips", "nanite_repair"), SUStringCodex.SHU_TOOLTIP_IMG
               );
               text2.addPara(
                  "N-Repair", SUStringCodex.SHU_TOOLTIP_PADZERO, Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"), new String[]{"N-Repair"}
               );
               text2.addPara(
                  "Nanites will be deployed to conduct hull repairs when hull percentage falls below %s. Nano-repair recovers %s of ship's hull point every second until the hull integrity is back to %s.",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Misc.getHighlightColor(),
                  new String[]{Misc.getRoundedValue(50.0F) + "%", "0.4%", Misc.getRoundedValue(50.0F) + "%"}
               );
               tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
               tooltip.addPara(
                  "• Nanite Coating reduces the incoming damage by %s during venting.\n• The Nano-repair function will cease if the ship is overloaded.",
                  SUStringCodex.SHU_TOOLTIP_PADMAIN,
                  SUStringCodex.SHU_TOOLTIP_GREEN,
                  new String[]{Misc.getRoundedValue(50.0F) + "%"}
               );
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

         tooltip.addPara(
            "%s",
            SUStringCodex.SHU_TOOLTIP_PADMAIN,
            Misc.getGrayColor(),
            new String[]{"This is an extension of Nanite Swarm Cluster and it will remove itself when the main hullmod is removed from the parent module."}
         );
      }
   }

   static {
      armourzone.put(HullSize.FIGHTER, 0.0F);
      armourzone.put(HullSize.FRIGATE, 50.0F);
      armourzone.put(HullSize.DESTROYER, 100.0F);
      armourzone.put(HullSize.CRUISER, 150.0F);
      armourzone.put(HullSize.CAPITAL_SHIP, 200.0F);
   }
}
