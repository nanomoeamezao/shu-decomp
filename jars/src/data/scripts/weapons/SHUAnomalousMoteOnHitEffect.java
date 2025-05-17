package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.combat.MoteControlScript;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

public class SHUAnomalousMoteOnHitEffect implements OnHitEffectPlugin {
   private static final Color EMP_COLOR_CORE = new Color(230, 40, 160, 255);
   private static final Color EMP_COLOR_FRINGE = new Color(200, 10, 125, 55);

   public void onHit(
      DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine
   ) {
      boolean withEMP = false;
      if (target instanceof ShipAPI ship) {
         if (!ship.isFighter()) {
            float pierceChance = 1.0F;
            pierceChance *= ship.getMutableStats().getDynamic().getValue("shield_pierced_mult");
            boolean piercedShield = shieldHit && (float)Math.random() < pierceChance;
            if (!shieldHit || piercedShield) {
               float emp = projectile.getEmpAmount();
               float dam = projectile.getDamageAmount();
               engine.spawnEmpArcPierceShields(
                  projectile.getSource(),
                  point,
                  target,
                  target,
                  projectile.getDamageType(),
                  dam,
                  emp,
                  100000.0F,
                  "mote_attractor_impact_emp_arc",
                  20.0F,
                  EMP_COLOR_FRINGE,
                  EMP_COLOR_CORE
               );
               withEMP = true;
            }
         } else {
            float damage = 600.0F;
            Global.getCombatEngine().applyDamage(projectile, ship, point, 600.0F, DamageType.ENERGY, 0.0F, false, false, projectile.getSource(), true);
         }
      } else if (target instanceof MissileAPI) {
         float damage = 600.0F;
         Global.getCombatEngine().applyDamage(projectile, target, point, 600.0F, DamageType.ENERGY, 0.0F, false, false, projectile.getSource(), true);
      }

      String impactSoundId = MoteControlScript.getImpactSoundId(projectile.getSource());
      Global.getSoundPlayer().playSound(impactSoundId, 1.0F, 1.0F, point, new Vector2f());
   }
}
