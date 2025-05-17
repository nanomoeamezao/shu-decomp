package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.SUPlugin;
import data.scripts.util.id.SUStringCodex;
import java.awt.Color;
import lunalib.lunaSettings.LunaSettings;
import org.lwjgl.util.vector.Vector2f;

public class SHUGACHALensingArticle extends BaseHullMod {
   private static final float BEAM_DAMAGE_BONUS = 35.0F;
   boolean enableCheatModeForRetards = SUPlugin.ENABLE_CHEAT_FOR_RETARDS;

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (stats.getVariant().getSMods().contains("specialsphmod_gacha_lensing_article")) {
         stats.getBeamWeaponDamageMult().modifyPercent(id, 35.0F);
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      if (ship.getVariant().getSMods().contains("specialsphmod_gacha_lensing_article") && !ship.getVariant().hasHullMod("high_scatter_amp")) {
         ship.addListener(new SHUGACHALensingArticle.LensingArticleDamageDealtMod(ship));
      }

      if (SUPlugin.HASLUNALIB) {
         this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
      }

      if (!this.enableCheatModeForRetards
         && (
            !ship.getVariant().getSMods().contains("specialsphmod_gacha_lensing_article")
               || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_gacha_lensing_article")
         )) {
         ship.getVariant().removeMod("specialsphmod_gacha_lensing_article");
      }
   }

   public String getDescriptionParam(int index, HullSize hullSize) {
      return null;
   }

   public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
      if (!isForModSpec && ship != null) {
         tooltip.addSectionHeading("Technical Details", Alignment.MID, SUStringCodex.SHU_TOOLTIP_PADMAIN);
         if (ship.getVariant().getSMods().contains("specialsphmod_gacha_lensing_article")) {
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
               "• Increases beam damage: %s\n• Beam damage raises hard flux of targets.",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{Misc.getRoundedValue(35.0F) + "%"}
            );
            tooltip.addPara(
               "• The hard flux effect %s stack with other similar bonus.",
               SUStringCodex.SHU_TOOLTIP_PADZERO,
               Misc.getHighlightColor(),
               new String[]{"does not"}
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
               new String[]{"\"Never bring a gun to a fight when a 200-meter-long overtuned carbon dioxide laser will do the job, better.\""}
            )
            .italicize();
         tooltip.addPara(
            "%s", SUStringCodex.SHU_TOOLTIP_PADSIG, SUStringCodex.SHU_TOOLTIP_QUOTECOLOR, new String[]{"         — Fabrique Orbitale marketing copy, page 7"}
         );
      }
   }

   public Color getBorderColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_R_BORDER;
   }

   public Color getNameColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_R_NAME;
   }

   public static class LensingArticleDamageDealtMod implements DamageDealtModifier {
      protected final ShipAPI ship;

      public LensingArticleDamageDealtMod(ShipAPI ship) {
         this.ship = ship;
      }

      public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
         if (!(param instanceof DamagingProjectileAPI) && param instanceof BeamAPI) {
            damage.setForceHardFlux(true);
         }

         return null;
      }
   }
}
