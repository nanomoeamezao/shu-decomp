package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
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
import java.util.HashMap;
import java.util.Map;
import lunalib.lunaSettings.LunaSettings;

public class SHUGACHAMarkOfTheSlayer extends BaseHullMod {
   private static final float MASS_BONUS = 1.9F;
   private static final float ZERO_FLUX_RELEASE = 2.0F;
   private static final float EXPLODE_MULT = 0.5F;
   private static final float ARMOR_MULT = 0.8F;
   private static final float RANGE_THRESHOLD = 550.0F;
   private static final float RANGE_MULT = 0.25F;
   private static final float RECOIL_MALUS = 1.3F;
   private static final Map<String, Boolean> MassIncreased = new HashMap<>();
   boolean enableCheatModeForRetards = SUPlugin.ENABLE_CHEAT_FOR_RETARDS;
   private static final Map YOUCANTHURTMEJACK = new HashMap();

   public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
      if (stats.getVariant().getSMods().contains("specialsphmod_gacha_mark_of_the_slayer")) {
         stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 2.0F);
         stats.getArmorBonus().modifyFlat(id, (Float)YOUCANTHURTMEJACK.get(hullSize));
         stats.getWeaponRangeThreshold().modifyFlat(id, 550.0F);
         stats.getWeaponRangeMultPastThreshold().modifyMult(id, 0.25F);
         stats.getArmorDamageTakenMult().modifyMult(id, 0.8F);
         stats.getHullDamageTakenMult().modifyMult(id, 0.8F);
         stats.getDynamic().getStat("explosion_damage_mult").modifyMult(id, 0.5F);
         stats.getMaxRecoilMult().modifyMult(id, 1.3F);
         stats.getRecoilPerShotMult().modifyMult(id, 1.3F);
         stats.getRecoilDecayMult().modifyMult(id, 1.3F);
      }
   }

   public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
      if (SUPlugin.HASLUNALIB) {
         this.enableCheatModeForRetards = LunaSettings.getBoolean("mayu_specialupgrades", "shu_gachasmodCheatToggle");
      }

      if (!this.enableCheatModeForRetards
         && (
            !ship.getVariant().getSMods().contains("specialsphmod_gacha_mark_of_the_slayer")
               || ship.getVariant().getHullSpec().isBuiltInMod("specialsphmod_gacha_mark_of_the_slayer")
         )) {
         ship.getVariant().removeMod("specialsphmod_gacha_mark_of_the_slayer");
      }
   }

   public void advanceInCombat(ShipAPI ship, float amount) {
      if (ship.getVariant().getSMods().contains("specialsphmod_gacha_mark_of_the_slayer")) {
         if (Global.getCombatEngine() == null) {
            return;
         }

         if (!ship.isAlive()) {
            return;
         }

         if (ship.getParentStation() == null) {
            if (!MassIncreased.containsKey(ship.getId()) || !MassIncreased.get(ship.getId())) {
               ship.setMass(ship.getMass() * 1.9F);
               MassIncreased.put(ship.getId(), true);
            }

            if (!ship.getChildModulesCopy().isEmpty()) {
               for (ShipAPI s : ship.getChildModulesCopy()) {
                  if (!MassIncreased.containsKey(s.getId()) || !MassIncreased.get(s.getId())) {
                     s.setMass(s.getMass() * 1.9F);
                     MassIncreased.put(s.getId(), true);
                  }
               }
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
         if (ship.getVariant().getSMods().contains("specialsphmod_gacha_mark_of_the_slayer")) {
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
               "• Zero-flux movement speed bonus at any flux level.\n• Increases ship's mass: %s\n• Reduces damage taken from ship explosions: %s\n• Increases ship's armor: %s/%s/%s/%s (by hull size)",
               SUStringCodex.SHU_TOOLTIP_EXTRADESC,
               SUStringCodex.SHU_TOOLTIP_GREEN,
               new String[]{
                  Misc.getRoundedValue(90.0F) + "%",
                  Misc.getRoundedValue(50.0F) + "%",
                  Misc.getRoundedValue(100.0F),
                  Misc.getRoundedValue(200.0F),
                  Misc.getRoundedValue(300.0F),
                  Misc.getRoundedValue(400.0F)
               }
            );
            tooltip.addPara(
               "• Increases weapon recoil rate: %s\n• Limits the range of weaponries to %s or less.",
               SUStringCodex.SHU_TOOLTIP_PADZERO,
               SUStringCodex.SHU_TOOLTIP_RED,
               new String[]{Misc.getRoundedValue(30.0F) + "%", Misc.getRoundedValue(600.0F) + "su"}
            );
         } else {
            tooltip.addPara(
               "%s",
               SUStringCodex.SHU_TOOLTIP_PADMAIN,
               Misc.getNegativeHighlightColor(),
               new String[]{"No bonus applied. The information will only appear if this hullmod is S-modded."}
            );
         }

         tooltip.addPara("%s", SUStringCodex.SHU_TOOLTIP_PADQUOTE, SUStringCodex.SHU_TOOLTIP_QUOTECOLOR, new String[]{"\". . .\""}).italicize();
         tooltip.addPara(
            "%s",
            SUStringCodex.SHU_TOOLTIP_PADSIG,
            SUStringCodex.SHU_TOOLTIP_QUOTECOLOR,
            new String[]{"         — Unnamed marine aboard LCS Moloch's Bane, prior to a raid on a compromised Tri-Tachyon research lab, c205"}
         );
      }
   }

   public Color getBorderColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_R_BORDER;
   }

   public Color getNameColor() {
      return SUStringCodex.SHU_HULLMOD_GACHA_R_NAME;
   }

   static {
      YOUCANTHURTMEJACK.put(HullSize.FIGHTER, 0.0F);
      YOUCANTHURTMEJACK.put(HullSize.FRIGATE, 100.0F);
      YOUCANTHURTMEJACK.put(HullSize.DESTROYER, 200.0F);
      YOUCANTHURTMEJACK.put(HullSize.CRUISER, 300.0F);
      YOUCANTHURTMEJACK.put(HullSize.CAPITAL_SHIP, 400.0F);
   }
}
