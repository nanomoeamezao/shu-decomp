package data.hullmods;

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

public class SHUGACHAAEExoticShielding extends BaseHullMod {
   private final Color SHIELD_RING_COLOR = new Color(255, 255, 0, 255);
   private final Color SHIELD_INNER_COLOR = new Color(220, 75, 30, 145);
   private static final float SHIELD_BONUS = 30.0F;
   private static final float SHIELD_FLUX_BONUS = 0.2F;
   private static final float SHIELD_RATE = 70.0F;
   private static final float SHIELD_ARC_BONUS = 160.0F;
   private static final float PIERCE_MULT = 0.2F;
   private static final float FLUX_CHECKER = 0.01F;
   private final String ID;
   boolean enableCheatModeForRetards = SUPlugin.ENABLE_CHEAT_FOR_RETARDS;

   public SHUGACHAAEExoticShielding() {
      this.ID = "SHUGACHAAEExoticShielding";
   }

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (stats.getVariant().getSMods().contains("specialsphmod_gacha_exotic_shielding")) {
         stats.getShieldDamageTakenMult().modifyPercent(id, -30.0F);
         stats.getShieldUnfoldRateMult().modifyPercent(id, 70.0F);
         stats.getShieldTurnRateMult().modifyPercent(id, 70.0F);
         stats.getShieldArcBonus().modifyFlat(id, 160.0F);
         stats.getDynamic().getStat("shield_pierced_mult").modifyMult(id, 0.2F);
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
      }

      if (!this.enableCheatModeForRetards
         && (
            !ship.getVariant().getSMods().contains("specialsphmod_gacha_exotic_shielding")
               || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_gacha_exotic_shielding")
         )) {
         ship.getVariant().removeMod("specialsphmod_gacha_exotic_shielding");
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      CombatEngineAPI engine = Global.getCombatEngine();
      ShipAPI playerShip = Global.getCombatEngine().getPlayerShip();
      float FLUX_LEVEL = Math.min(ship.getFluxTracker().getFluxLevel() / 0.9F, 1.0F);
      float SHIELD_FLUX_REDUC = Math.round((0.0F + 0.2F * FLUX_LEVEL) * 100.0F);
      if (ship.isAlive()) {
         if (ship.getVariant().getSMods().contains("specialsphmod_gacha_exotic_shielding") && ship.getShield() != null) {
            if (ship.getShield().isOn()) {
               ship.getMutableStats().getShieldAbsorptionMult().modifyPercent(this.ID, -SHIELD_FLUX_REDUC);
               if (ship.getFluxLevel() >= 0.01F && ship == playerShip) {
                  engine.maintainStatusForPlayerShip(
                     this.ID,
                     Global.getSettings().getSpriteName("tooltips", "exotic_shields"),
                     "Exo-Shield Emitter:",
                     "-" + Math.round((0.0F + 0.2F * FLUX_LEVEL) * 100.0F) + "% Damage taken by shields",
                     false
                  );
               }

               if (ship.getFluxLevel() >= 0.5F) {
                  ship.getShield().setRingColor(this.SHIELD_RING_COLOR);
                  ship.getShield().setInnerColor(this.SHIELD_INNER_COLOR);
               }
            } else {
               ship.getMutableStats().getShieldAbsorptionMult().unmodify(this.ID);
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
         if (ship.getVariant().getSMods().contains("specialsphmod_gacha_exotic_shielding")) {
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
               "• Reduces damage taken by shields: %s\n• Improves shield raise and turn rate: %s\n• Reduced chance of shield being pierced: %s\n• Increases the shield's coverage: %s degrees",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(30.0F) + "%", Misc.getRoundedValue(70.0F) + "%", Misc.getRoundedValue(80.0F) + "%", Misc.getRoundedValue(160.0F)
               }
            );
            if (Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"))) {
               tooltip.addSectionHeading("Passive System", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
               TooltipMakerAPI text = tooltip.beginImageWithText(
                  Global.getSettings().getSpriteName("tooltips", "exotic_shields"), SUStringCodex.SHU_TOOLTIP_IMG
               );
               text.addPara(
                  "Exo-Shield Emitter",
                  SUStringCodex.SHU_TOOLTIP_PADZERO,
                  Global.getSettings().getColor("tooltipTitleAndLightHighlightColor"),
                  new String[]{"Exo-Shield Emitter"}
               );
               text.addPara(
                  "Altair Exotech's experimental shield conduit absorbs the build up flux and converts it as energy for the emitter. The process further reinforces the shield's layering and its strength. The additional defense bonus caps at %s.",
                  0.0F,
                  Misc.getHighlightColor(),
                  new String[]{Misc.getRoundedValue(20.0F) + "%"}
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
               new String[]{
                  "\"Peace of mind, delivered through 40 gigawatts of powerful, reliable energy shields. Next time you refit your fleet, ask yourself: Am I keeping my crew safe? Now available in several colors and shield diameters.\""
               }
            )
            .italicize();
         tooltip.addPara(
            "%s",
            SUStringCodex.SHU_TOOLTIP_PADSIG,
            SUStringCodex.SHU_TOOLTIP_QUOTECOLOR,
            new String[]{"         — Altair Exotech marketing copy, c183 pre-Collapse"}
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
