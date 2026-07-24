package wildlife;

import java.util.*;

import game.*;
import game.actions.*;
import utils.*;
import world.*;
import world.liquid.LiquidType;

public class Animal extends Unit {

	
	private double aggression;
	private double bravery;
	private double curiosity;
	private double herdInstinct;
	private double territoriality;
    private static final int TARGETING_COOLDOWN = 10;

    private int dontMigrateUntil;
    private int nextTimeToChooseTarget;
    private int whenToInvade;

    private double thirst;
    private double thirstThreshold;
    private double thirstRate;

    private double hunger;
    private double hungerRate;

    
    private double fatigue;
    private double fatigueRate = 0.0005;
    
    private Unit currentEnemy;
    private Tile starting;
    private Tile wanderTarget;
    private Tile rememberedWater;
    private Tile rememberedFood;
    private Tile lastThreatLocation;
    private Unit currentThreat;
    private Thing currentTarget;
    private ActionType currentGoal = ActionType.WANDER;
    private int goalLockedUntil = 0;
    private static final int VISION_RANGE = 8;
    

    private enum ActionType {
        REST,
        WANDER,
        SEEK_WATER,
        SEEK_FOOD,
        FLEE,
        ATTACK,
        RETURN_HOME
    }

    public Animal(UnitType type, Tile tile, Faction faction) {
        super(type, tile, faction);


        this.starting = tile;

    }
    
    @Override public void updateState() { 
    	super.updateState(); 
    	
    	if(World.ticks >= goalLockedUntil){
    	    goalLockedUntil = World.ticks + 80;
    	}
    	
    	fatigue += fatigueRate;
    }
    
    @Override
    public void planActions(World world) {

        if (!isIdle())
            return;

        // Refresh memory
        scanEnvironment(world);

        // Only choose a new goal every few seconds
        if (World.ticks >= goalLockedUntil) {
            currentGoal = chooseGoal(world);
            goalLockedUntil = World.ticks + 80;
        }

        switch (currentGoal) {

            case REST:
                rest();
                break;

            case WANDER:
                wander(world);
                break;

            case SEEK_WATER:
                seekWater(world);
                break;

            case SEEK_FOOD:
                seekFood(world);
                break;

            case FLEE:
                flee(world);
                break;

            case ATTACK:
                attack(world);
                break;

            case RETURN_HOME:
                if(starting != null)
                    queuePlannedAction(PlannedAction.moveTo(starting));
                break;
        }
    }
    
    private ActionType chooseGoal(World world) {

        EnumMap<ActionType, Double> scores = new EnumMap<>(ActionType.class);
        scores.put(ActionType.REST, getScoreRest());

        scores.put(ActionType.WANDER, getScoreWander());

        scores.put(ActionType.SEEK_WATER, getScoreWater());

        scores.put(ActionType.SEEK_FOOD, getScoreFood());

        scores.put(ActionType.FLEE, getScoreFlee());

        scores.put(ActionType.ATTACK, getScoreAttack());

        scores.put(ActionType.RETURN_HOME, getScoreReturnHome());

        ActionType best = ActionType.WANDER;
        double highest = Double.NEGATIVE_INFINITY;

        for (Map.Entry<ActionType, Double> e : scores.entrySet()) {

            if (e.getValue() > highest) {
                highest = e.getValue();
                best = e.getKey();
            }

        }

        return best;

    }
    
    private void attack(World world){

        if(currentEnemy == null || currentEnemy.isDead()){

            currentEnemy = findClosestEnemy(world);

        }

        if(currentEnemy != null){

            queuePlannedAction(
                PlannedAction.attack(currentEnemy)
            );

        }

    }
    
    
    private Unit findClosestEnemy(World world) {

        Unit best = null;
        double bestDistance = Double.MAX_VALUE;

        for(Tile t : getVisibleTiles(world)) {

            for(Unit u : t.getUnits()) {

                if(u == this)
                    continue;

                if(u.getFaction() == getFaction())
                    continue;

                double d = getTile().distanceTo(u.getTile());

                if(d < bestDistance) {

                    bestDistance = d;
                    best = u;

                }

            }

        }

        return best;

    }
    
    private List<Tile> getVisibleTiles(World world) {

        List<Tile> visible = new ArrayList<>();

        for(Tile t : Utils.getTilesInRadius(getTile(), world, VISION_RANGE)) {

            visible.add(t);

        }

        return visible;

    }
    
    private void scanEnvironment(World world) {

        for(Tile t : getVisibleTiles(world)) {

            if(t.liquidType == LiquidType.WATER) {
                rememberedWater = t;
            }

//            if(t.getPlant() != null) {
//                rememberedFood = t;
//            }

            for(Unit u : t.getUnits()) {
                if(u.getFaction() != getFaction()) {
                    currentThreat = u;
                    lastThreatLocation = t;

                }

            }

        }

    }
    
    private void wander(World world){
    	
        if(wanderTarget == null || wanderTarget == getTile() || wanderTarget.isBlocked(this)){ 
        	
        	List<Tile> temp = Utils.getTilesInRadius(getTile(), world, VISION_RANGE);
        	wanderTarget = (Tile) temp.get((int) (temp.size()*Math.random()));
        }

        queuePlannedAction(
            PlannedAction.moveTo(wanderTarget)
        );
    }
    
    private void seekWater(World world){

        if(getTile().liquidType == LiquidType.WATER){
            thirst = Math.max(0, thirst - 0.5);
            return;
        }
        if(rememberedWater != null){
            queuePlannedAction(
                PlannedAction.moveTo(rememberedWater)
            );
            return;
        }
        wander(world);
    }
    
    private void seekFood(World world){

        if(rememberedFood != null){
            queuePlannedAction(
                PlannedAction.moveTo(rememberedFood)
            );
            return;
        }
        wander(world);

    }
    
    
    private void flee(World world){
        if(currentThreat == null){
            wander(world);
            return;
        }
        
        Tile best = getTile();
        double bestDangerScore = -999;
        for(Tile t : getTile().getNeighbors()){

            if(t.isBlocked(this))
                continue;

            double dangerScore = t.distanceTo(currentThreat.getTile()) - applyResistance(t.computeTileDanger());

            if(dangerScore > bestDangerScore){
            	bestDangerScore = dangerScore;
                best = t;

            }

        }

        queuePlannedAction(
            PlannedAction.moveTo(best)
        );

    }
    
    
    
    private double getScoreReturnHome(){

        if(starting == null)
            return 0;
        double dist = getTile().distanceTo(starting);
        if(thirst > 0.4)
            return 0;
        if(hunger > 0.4)
            return 0;
        
        return (dist / 15.0);
    }
    
    private double getScoreRest(){
        return fatigue;
    }
    private double getScoreWander() {

        return curiosity
                * (1.0 - hunger)
                * (1.0 - thirst)
                * (1.0 - fatigue);

    }
    private double getScoreWater() {

        if (rememberedWater == null)
            return thirst * 0.5;

        return thirst * 2.0;

    }
    private double getScoreFood() {

        if (rememberedFood == null)
            return hunger * 0.5;

        return hunger * 2.0;

    }
    private double getScoreFlee() {

        if (currentThreat == null)
            return 0;

        double health = getHealth() / getMaxHealth();

        return (1.0 - bravery)
                * (1.0 - health);

    }
    private double getScoreAttack() {

        if (currentThreat == null)
            return 0;

        double health = getHealth() / getMaxHealth();

        return aggression
                * health;

    }
    
    // sleep function?
    private void rest(){
        fatigue -= 0.01;
        fatigue = Math.max(0,fatigue);

    }
    public boolean wantsToAttack() { 
    	return !getType().getTargetingInfo().isEmpty(); 
    }
    public void chooseWhatToAttack(LinkedList<Unit> units, LinkedList<Building> buildings) { 
    	for (TargetingInfo targetType : getType().getTargetingInfo()) { 
    		Thing target = targetType.getValidTargetFor(this, units, buildings); 
    		if (target != null) { clearPlannedActions(); 
    			queuePlannedAction(PlannedAction.attack(target)); 
    		return; 
    		} 
    	} 
    }
    
    
}