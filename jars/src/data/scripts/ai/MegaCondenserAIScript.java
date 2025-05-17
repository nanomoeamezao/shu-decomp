package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionGridAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.impl.combat.MoteControlScript;
import com.fs.starfarer.api.impl.combat.MoteControlScript.SharedMoteAIData;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.lwjgl.util.vector.Vector2f;

public final class MegaCondenserAIScript implements MissileAIPlugin {
   private static final float DISTANCE_FROM_SOURCE = 1500.0F;
   private static final float DISTANCE_FROM_ATTRACTOR = 1000.0F;
   private static final float MAX_FAIRY_CIRCLE = 180.0F;
   private static final float AVERSION_TO_STINKY = 150.0F;
   private static final float AVOID_RANGE = 50.0F;
   private static final float COHESION_RANGE = 80.0F;
   private static final float ATTRACTOR_LOCK_STOP_FLOCKING_ADD = 300.0F;
   protected MissileAPI missile;
   protected IntervalUtil tracker = new IntervalUtil(0.05F, 0.1F);
   protected IntervalUtil updateListTracker = new IntervalUtil(0.05F, 0.1F);
   protected List<MissileAPI> missileList = new ArrayList<>();
   protected List<CombatEntityAPI> tooStinky = new ArrayList<>();
   protected float ratio;
   protected CombatEntityAPI target;
   protected SharedMoteAIData data;
   protected IntervalUtil flutterCheck = new IntervalUtil(2.0F, 4.0F);
   protected FaderUtil currFlutter = null;
   protected float flutterRemaining = 0.0F;
   protected float elapsed = 0.0F;

   public MegaCondenserAIScript(MissileAPI missile) {
      this.missile = missile;
      this.ratio = (float)Math.random();
      this.elapsed = -((float)Math.random()) * 0.5F;
      this.data = MoteControlScript.getSharedData(missile.getSource());
      this.avoidLikePlague();
   }

   public void advance(float amount) {
      if (!this.missile.isFizzling()) {
         if (this.missile.getSource() != null) {
            this.elapsed += amount;
            this.updateListTracker.advance(amount);
            if (this.updateListTracker.intervalElapsed()) {
               this.avoidLikePlague();
            }

            if (this.flutterRemaining <= 0.0F) {
               this.flutterCheck.advance(amount);
               if (this.flutterCheck.intervalElapsed() && ((float)Math.random() > 0.9F || this.data.attractorLock != null && (float)Math.random() > 0.5F)) {
                  this.flutterRemaining = 2.0F + (float)Math.random() * 2.0F;
               }
            }

            if (this.elapsed >= 0.5F) {
               boolean wantToFuck = !this.isRetarded();
               if (this.data.attractorLock != null) {
                  float dist = Misc.getDistance(this.missile.getLocation(), this.data.attractorLock.getLocation());
                  if (dist > this.data.attractorLock.getCollisionRadius() + 300.0F) {
                     wantToFuck = true;
                  }
               }

               if (wantToFuck) {
                  this.doFucking();
               } else {
                  CombatEngineAPI engine = Global.getCombatEngine();
                  Vector2f targetLoc = engine.getAimPointWithLeadForAutofire(this.missile, 1.5F, this.target, 50.0F);
                  engine.headInDirectionWithoutTurning(this.missile, Misc.getAngleInDegrees(this.missile.getLocation(), targetLoc), 10000.0F);
                  if (this.ratio > 0.5F) {
                     this.missile.giveCommand(ShipCommand.TURN_LEFT);
                  } else {
                     this.missile.giveCommand(ShipCommand.TURN_RIGHT);
                  }

                  this.missile.getEngineController().forceShowAccelerating();
               }
            }

            this.tracker.advance(amount);
            if (this.tracker.intervalElapsed() && this.elapsed >= 0.5F) {
               this.needNewVictim();
            }
         }
      }
   }

   protected boolean isRetarded() {
      if (this.target != null && (!(this.target instanceof ShipAPI) || !((ShipAPI)this.target).isPhased())) {
         CombatEngineAPI engine = Global.getCombatEngine();
         if (this.target != null && this.target instanceof ShipAPI && ((ShipAPI)this.target).isHulk()) {
            return false;
         } else {
            List list = null;
            if (this.target instanceof ShipAPI) {
               list = engine.getShips();
            } else {
               list = engine.getMissiles();
            }

            return this.target != null && list.contains(this.target) && this.target.getOwner() != this.missile.getOwner();
         }
      } else {
         return false;
      }
   }

   protected void needNewVictim() {
      if (this.data.attractorLock != null) {
         this.target = this.data.attractorLock;
      } else {
         CombatEngineAPI engine = Global.getCombatEngine();
         int owner = this.missile.getOwner();
         int maxMotesPerMissile = 2;
         float maxDistFromSourceShip = 1500.0F;
         float maxDistFromAttractor = 1000.0F;
         float minDist = Float.MAX_VALUE;
         CombatEntityAPI closest = null;

         for (MissileAPI other : engine.getMissiles()) {
            if (other.getOwner() != owner && other.getOwner() != 100) {
               float distToTarget = Misc.getDistance(this.missile.getLocation(), other.getLocation());
               if (!(distToTarget > minDist) && (!(distToTarget > 3000.0F) || engine.isAwareOf(owner, other))) {
                  float distFromAttractor = Float.MAX_VALUE;
                  if (this.data.attractorTarget != null) {
                     distFromAttractor = Misc.getDistance(other.getLocation(), this.data.attractorTarget);
                  }

                  float distFromSource = Misc.getDistance(other.getLocation(), this.missile.getSource().getLocation());
                  if ((!(distFromSource > maxDistFromSourceShip) || !(distFromAttractor > maxDistFromAttractor))
                     && this.sicEMLads(other) < maxMotesPerMissile
                     && distToTarget < minDist) {
                     closest = other;
                     minDist = distToTarget;
                  }
               }
            }
         }

         for (ShipAPI otherx : engine.getShips()) {
            if (otherx.getOwner() != owner && otherx.getOwner() != 100 && otherx.isFighter()) {
               float distToTarget = Misc.getDistance(this.missile.getLocation(), otherx.getLocation());
               if (!(distToTarget > minDist) && (!(distToTarget > 3000.0F) || engine.isAwareOf(owner, otherx))) {
                  float distFromAttractorx = Float.MAX_VALUE;
                  if (this.data.attractorTarget != null) {
                     distFromAttractorx = Misc.getDistance(otherx.getLocation(), this.data.attractorTarget);
                  }

                  float distFromSource = Misc.getDistance(otherx.getLocation(), this.missile.getSource().getLocation());
                  if ((!(distFromSource > maxDistFromSourceShip) || !(distFromAttractorx > maxDistFromAttractor))
                     && this.sicEMLads(otherx) < maxMotesPerMissile
                     && distToTarget < minDist) {
                     closest = otherx;
                     minDist = distToTarget;
                  }
               }
            }
         }

         this.target = closest;
      }
   }

   public void avoidLikePlague() {
      this.tooStinky.clear();
      CollisionGridAPI grid = Global.getCombatEngine().getAiGridShips();
      Iterator<Object> iterate = grid.getCheckIterator(this.missile.getLocation(), 300.0F, 300.0F);

      while (iterate.hasNext()) {
         if (iterate.next() instanceof ShipAPI ship && !ship.isFighter()) {
            this.tooStinky.add(ship);
         }
      }

      grid = Global.getCombatEngine().getAiGridAsteroids();
      iterate = grid.getCheckIterator(this.missile.getLocation(), 300.0F, 300.0F);

      while (iterate.hasNext()) {
         if (iterate.next() instanceof CombatEntityAPI asteroid) {
            this.tooStinky.add(asteroid);
         }
      }
   }

   public void doFucking() {
      if (this.missile.getSource() != null) {
         ShipAPI source = this.missile.getSource();
         CombatEngineAPI engine = Global.getCombatEngine();
         float avoidRange = 50.0F;
         float cohesionRange = 80.0F;
         float sourceRejoin = source.getCollisionRadius() + 200.0F;
         float sourceRepel = source.getCollisionRadius() + 50.0F;
         float sourceCohesion = source.getCollisionRadius() + 600.0F;
         float sin = (float)Math.sin(this.data.elapsed * 1.0F);
         float mult = 1.0F + sin * 0.25F;
         avoidRange *= mult;
         Vector2f total = new Vector2f();
         Vector2f fairies = this.fairyRing();
         if (fairies != null) {
            float dist = Misc.getDistance(this.missile.getLocation(), fairies);
            Vector2f dir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(this.missile.getLocation(), fairies));
            float f = dist / 200.0F;
            if (f > 1.0F) {
               f = 1.0F;
            }

            dir.scale(f * 3.0F);
            Vector2f.add(total, dir, total);
            avoidRange *= 3.0F;
         }

         boolean hardAvoiding = false;

         for (CombatEntityAPI other : this.tooStinky) {
            float dist = Misc.getDistance(this.missile.getLocation(), other.getLocation());
            float hardAvoidRange = other.getCollisionRadius() + avoidRange + 50.0F;
            if (dist < hardAvoidRange) {
               Vector2f dir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(other.getLocation(), this.missile.getLocation()));
               float f = 1.0F - dist / hardAvoidRange;
               dir.scale(f * 5.0F);
               Vector2f.add(total, dir, total);
               hardAvoiding = f > 0.5F;
            }
         }

         for (MissileAPI otherMissile : this.data.motes) {
            if (otherMissile != this.missile) {
               float dist = Misc.getDistance(this.missile.getLocation(), otherMissile.getLocation());
               float w = otherMissile.getMaxHitpoints();
               w = 1.0F;
               if (dist < avoidRange && otherMissile != this.missile && !hardAvoiding) {
                  Vector2f dir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(otherMissile.getLocation(), this.missile.getLocation()));
                  float f = 1.0F - dist / avoidRange;
                  dir.scale(f * w);
                  Vector2f.add(total, dir, total);
               }

               if (dist < cohesionRange) {
                  Vector2f dir = new Vector2f(otherMissile.getVelocity());
                  Misc.normalise(dir);
                  float f = 1.0F - dist / cohesionRange;
                  dir.scale(f * w);
                  Vector2f.add(total, dir, total);
               }
            }
         }

         if (this.missile.getSource() != null) {
            float distx = Misc.getDistance(this.missile.getLocation(), source.getLocation());
            if (distx > sourceRejoin) {
               Vector2f dir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(this.missile.getLocation(), source.getLocation()));
               float f = distx / (sourceRejoin + 180.0F) - 1.0F;
               dir.scale(f * 0.5F);
               Vector2f.add(total, dir, total);
            }

            if (distx < sourceRepel) {
               Vector2f dir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(source.getLocation(), this.missile.getLocation()));
               float f = 1.0F - distx / sourceRepel;
               dir.scale(f * 5.0F);
               Vector2f.add(total, dir, total);
            }

            if (distx < sourceCohesion && source.getVelocity().length() > 20.0F) {
               Vector2f dir = new Vector2f(source.getVelocity());
               Misc.normalise(dir);
               float f = 1.0F - distx / sourceCohesion;
               dir.scale(f * 1.0F);
               Vector2f.add(total, dir, total);
            }

            if (total.length() <= 0.05F) {
               float offset = this.ratio > 0.5F ? 90.0F : -90.0F;
               Vector2f dir = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(this.missile.getLocation(), source.getLocation()) + offset);
               float f = 1.0F;
               dir.scale(f * 1.0F);
               Vector2f.add(total, dir, total);
            }
         }

         if (total.length() > 0.0F) {
            float dir = Misc.getAngleInDegrees(total);
            engine.headInDirectionWithoutTurning(this.missile, dir, 10000.0F);
            if (this.ratio > 0.5F) {
               this.missile.giveCommand(ShipCommand.TURN_LEFT);
            } else {
               this.missile.giveCommand(ShipCommand.TURN_RIGHT);
            }

            this.missile.getEngineController().forceShowAccelerating();
         }
      }
   }

   protected int sicEMLads(CombatEntityAPI other) {
      int count = 0;

      for (MissileAPI mote : this.data.motes) {
         if (mote != this.missile && mote.getUnwrappedMissileAI() instanceof MegaCondenserAIScript) {
            MegaCondenserAIScript ai = (MegaCondenserAIScript)mote.getUnwrappedMissileAI();
            if (ai.getTarget() == other) {
               count++;
            }
         }
      }

      return count;
   }

   public Vector2f fairyRing() {
      Vector2f attractor = null;
      if (this.data.attractorTarget != null) {
         attractor = this.data.attractorTarget;
         if (this.data.attractorLock != null) {
            attractor = this.data.attractorLock.getLocation();
         }
      }

      return attractor;
   }

   public CombatEntityAPI getTarget() {
      return this.target;
   }

   public void setTarget(CombatEntityAPI target) {
      this.target = target;
   }

   public void render() {
   }
}
