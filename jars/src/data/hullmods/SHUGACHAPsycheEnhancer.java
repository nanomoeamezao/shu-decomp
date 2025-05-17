package data.hullmods;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import lunalib.lunaSettings.LunaSettings;
import org.lwjgl.input.Keyboard;

public class SHUGACHAPsycheEnhancer extends BaseHullMod {
   private static final float CR_DEGRADE_BONUS = 50.0F;
   private static final float PEAK_CR_BONUS = 80.0F;
   private static final float MAX_CR = 0.2F;
   private static final float ENMITY_ROF_BONUS = 40.0F;
   private static final float ENMITY_SPEED_BONUS = 20.0F;
   private static final float ENMITY_MANEUVERABILITY_BONUS = 20.0F;
   private final String ID;
   boolean enableCheatModeForRetards = SUPlugin.ENABLE_CHEAT_FOR_RETARDS;

   public SHUGACHAPsycheEnhancer() {
      this.ID = "SHUGACHAPsycheEnhancer";
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (stats.getVariant().getSMods().contains("specialsphmod_gacha_psyche_enhancer") && !stats.getVariant().hasHullMod("automated")) {
         stats.getCRLossPerSecondPercent().modifyPercent(id, 50.0F);
         stats.getPeakCRDuration().modifyPercent(id, 80.0F);
         stats.getMaxCombatReadiness().modifyFlat(id, 0.2F, "Psyche Enhancer");
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
      }

      if (!this.enableCheatModeForRetards
         && (
            !ship.getVariant().getSMods().contains("specialsphmod_gacha_psyche_enhancer")
               || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_gacha_psyche_enhancer")
         )) {
         ship.getVariant().removeMod("specialsphmod_gacha_psyche_enhancer");
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (ship.getVariant().getSMods().contains("specialsphmod_gacha_psyche_enhancer")) {
         MutableShipStatsAPI stats = ship.getMutableStats();
         ShipAPI playerShip = Global.getCombatEngine().getPlayerShip();
         float Timer = Global.getCombatEngine().getTotalElapsedTime(false);
         CombatEngineAPI engine = Global.getCombatEngine();
         float percentageOfHPLeft = ship.getHitpoints() / ship.getMaxHitpoints();
         if (Global.getCurrentState() != GameState.COMBAT || !ship.isAlive()) {
            return;
         }

         if (!ship.getVariant().hasHullMod("automated")) {
            stats.getHitStrengthBonus().modifyPercent(this.ID, 35.0F);
            if (Timer < 960.0F) {
               stats.getBallisticWeaponDamageMult().modifyMult(this.ID, 1.0F + 0.01F * (Timer / 24.0F));
               stats.getEnergyWeaponDamageMult().modifyMult(this.ID, 1.0F + 0.01F * (Timer / 24.0F));
            } else {
               stats.getBallisticWeaponDamageMult().modifyMult(this.ID, 1.4F);
               stats.getEnergyWeaponDamageMult().modifyMult(this.ID, 1.4F);
            }

            if (ship == playerShip) {
               if (Timer < 960.0F) {
                  Global.getCombatEngine()
                     .maintainStatusForPlayerShip(
                        this.ID,
                        Global.getSettings().getSpriteName("tooltips", "battle_frenzy"),
                        "Battle Frenzy:",
                        "+" + Math.round(Timer / 24.0F - 0.5F) + "% Increased Weapon Damage",
                        false
                     );
               } else {
                  Global.getCombatEngine()
                     .maintainStatusForPlayerShip(
                        this.ID,
                        Global.getSettings().getSpriteName("tooltips", "battle_frenzy"),
                        "Battle Frenzy:",
                        "+" + Math.round(39.541668F) + "% Increased Weapon Damage",
                        false
                     );
               }
            }

            if (percentageOfHPLeft > 0.6F) {
               ship.getMutableStats().getMaxSpeed().unmodifyFlat(this.ID);
               ship.getMutableStats().getAcceleration().unmodifyPercent(this.ID);
               ship.getMutableStats().getDeceleration().unmodifyPercent(this.ID);
               ship.getMutableStats().getTurnAcceleration().unmodifyPercent(this.ID);
               ship.getMutableStats().getMaxTurnRate().unmodifyPercent(this.ID);
               ship.getMutableStats().getBallisticRoFMult().unmodifyPercent(this.ID);
               ship.getMutableStats().getEnergyRoFMult().unmodifyPercent(this.ID);
            } else {
               ship.getMutableStats().getMaxSpeed().modifyFlat(this.ID, 20.0F);
               ship.getMutableStats().getAcceleration().modifyPercent(this.ID, 20.0F);
               ship.getMutableStats().getDeceleration().modifyPercent(this.ID, 20.0F);
               ship.getMutableStats().getTurnAcceleration().modifyPercent(this.ID, 40.0F);
               ship.getMutableStats().getMaxTurnRate().modifyPercent(this.ID, 20.0F);
               ship.getMutableStats().getBallisticRoFMult().modifyPercent(this.ID, 40.0F);
               ship.getMutableStats().getEnergyRoFMult().modifyPercent(this.ID, 40.0F);
            }

            if (engine.getPlayerShip() != ship) {
               return;
            }

            if (percentageOfHPLeft <= 0.6F) {
               String crewStatus = "Thrilled";
               engine.maintainStatusForPlayerShip(
                  this.ID + "_TOOLTIP_ONE",
                  Global.getSettings().getSpriteName("tooltips", "battle_frenzy"),
                  "Crew Status: " + crewStatus,
                  "Weapon rate of fire +40%",
                  false
               );
               engine.maintainStatusForPlayerShip(
                  this.ID + "_TOOLTIP_TWO", Global.getSettings().getSpriteName("tooltips", "battle_frenzy"), "", "Max top speed +20", false
               );
            }
         }
      }
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (ship.getVariant().getSMods().contains("specialsphmod_gacha_psyche_enhancer")) {
            if (ship.getVariant().hasHullMod("automated")) {
               tooltip.addPara(
                  "The bonus %s to automated ships.", SUStringCodex.SHU_TOOLTIP_PADMAIN, Misc.getNegativeHighlightColor(), new String[]{"is not applicable"}
               );
            } else {
               tooltip.addImage(Global.getSettings().getSpriteName("tooltips", "banner_sr"), 368.0F, 40.0F, 5.0F);
               if (SUPlugin.HASLUNALIB) {
                  this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
               }

               if (this.enableCheatModeForRetards) {
                  LabelAPI retardrius = tooltip.addPara("%s", 5.0F, Misc.getBrightPlayerColor(), new String[]{"Cheat Mode: ON"});
                  retardrius.setAlignment(Alignment.MID);
                  retardrius.italicize();
               }

               tooltip.addPara(
                  "• Increases peak performance time: %s\n• Increases max combat readiness: %s\n• Reduces the rate at which combat readiness degrades by %s.",
                  SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                  SUStringCodex.SHU_TOOLTIP_GREEN,
                  new String[]{Misc.getRoundedValue(80.0F) + "%", Misc.getRoundedValue(20.0F) + "%", Misc.getRoundedValue(50.0F) + "%"}
               );
               if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
                  tooltip.addSectionHeading("Passive System", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
                  TooltipMakerAPI text = tooltip.beginImageWithText(
                     Global.getSettings().getSpriteName("tooltips", "battle_frenzy"), SUStringCodex.SHU_TOOLTIP_IMG
                  );
                  text.addPara(
                     "Battle Frenzy",
                     SUStringCodex.SHU_TOOLTIP_PADZERO,
                     Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                     new String[]{"Battle Frenzy"}
                  );
                  text.addPara(
                     "The longer the crew stays in combat the more battle hungry and ferocious they can be. Their excitement gets amplified when the ship takes heavy damage, rousing their spirits and the desire to fight more. This increases the damage of non-missile weapons by %s for every %s. The bonus damage is capped at %s.",
                     SUStringCodex.SHU_TOOLTIP_PADZERO,
                     Misc.getHighlightColor(),
                     new String[]{Misc.getRoundedValue(1.0F) + "%", Misc.getRoundedValue(24.0F) + " seconds", Misc.getRoundedValue(25.0F) + "%"}
                  );
                  tooltip.addImageWithText(SUStringCodex.SHU_TOOLTIP_PADMAIN);
                  tooltip.addPara(
                     "The following bonuses will be applied when hull integrity falls below %s.",
                     SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                     Misc.getNegativeHighlightColor(),
                     new String[]{Misc.getRoundedValue(60.0F) + "%"}
                  );
                  tooltip.addPara(
                     "• Increased max top speed: %s\n• Increased weapon rate of fire: %s",
                     SUStringCodex.SHU_TOOLTIP_EXTRADESC,
                     SUStringCodex.SHU_TOOLTIP_GREEN,
                     new String[]{Misc.getRoundedValue(20.0F), Misc.getRoundedValue(40.0F) + "%"}
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
               new String[]{"\"I'm sorry, Captain. The hull superstructure is failing. I'm going to have to go all out.\""}
            )
            .italicize();
         tooltip.addPara(
            "%s",
            SUStringCodex.SHU_TOOLTIP_PADSIG,
            SUStringCodex.SHU_TOOLTIP_QUOTECOLOR,
            new String[]{"         — Ensign Lee of the HSS Dynamic Entry, during a seemingly doomed fight against Tri-Tachyon forces, c190"}
         );
      }
   }

   public Color getBorderColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_SR_BORDER;
   }

   public Color getNameColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_SR_NAME;
   }
}
