package NextLevel;

public class SpriteTypeFeatures {
    /**
     * amount of health it can take from avatar
     */
    public double dangerousToAvatar;
    /**
     * Can it decrease points of avatar indirectly (e.g. by colliding with other object?
     */
    public boolean dangerousOtherwise;
    /**
     * Can it be destroyed?
     */
    public boolean destroyable;
    /**
     * Can it be collected?
     */
    public boolean collectable;
    /**
     * Can it give victory?
     */
    public boolean givingVictory;
    /**
     * Can it give defeat?
     */
    public boolean givingDefeat;
    /**
     * Amount of points it give
     */
    public int givingPoints;
    /**
     * Can avatar pass through it?
     */
    public boolean passable;
    /**
     * Can it move?
     */
    public boolean moving;
    /**
     * speed of the object
     */
    public double speed;
    /**
     * does collecting/destroying it increase value of other objects?
     */
    public boolean increasingValuesOfOtherObjects;
    /**
     * does collecting/destroying it allow to achieve victory?
     */
    public boolean allowingVictory;


	/**
     * Public constructor with default values
     */
    public SpriteTypeFeatures()
    {
    	this.dangerousToAvatar = 0;
    	this.dangerousOtherwise = false;
    	this.destroyable = false;
    	this.collectable = false;
    	this.givingVictory = false;
    	this.givingDefeat = false;
    	this.givingPoints = 0;
    	this.passable = false;
    	this.moving = false;
    	this.speed = 0;
    	this.increasingValuesOfOtherObjects = false;
    	this.allowingVictory = false;
    }
	/**
     * Public constructor with given values
     */
    public SpriteTypeFeatures(double dangerousToAvatar, boolean dangerousOtherwise, boolean destroyable,
    		boolean collectable, boolean givingVictory, boolean givingDefeat, int givingPoints,
    		boolean passable, boolean moving, double speed, boolean increasingValuesOfOtherObjects,
    		boolean allowingVictory)
    {
    	this.dangerousToAvatar = dangerousToAvatar;
    	this.dangerousOtherwise = dangerousOtherwise;
    	this.destroyable = destroyable;
    	this.collectable = collectable;
    	this.givingVictory = givingVictory;
    	this.givingDefeat = givingDefeat;
    	this.givingPoints = givingPoints;
    	this.passable = passable;
    	this.moving = moving;
    	this.speed = speed;
    	this.increasingValuesOfOtherObjects = increasingValuesOfOtherObjects;
    	this.allowingVictory = allowingVictory;
    }
	/**
     * Returns partial score from given sprite
     * @param weights array of weights used to calculate partial score.
     * @param distanceToSprite distance from current position to given sprite.
     */
    public double getScore(double[] weights, double distanceToSprite)
    {
    	double[] filledWeights = new double[12];
    	if(weights.length!=12) {
    		System.out.println("The weights array was incomplite. Missing values were set to 0.");
    		for (int i = 0; i<weights.length; i++) {
    			filledWeights[i] = weights[i];
    		}
    		for (int i = weights.length; i<12; i++) {
    			filledWeights[i] = 0;
    		}
    	} else {
    		filledWeights = weights;
    	}
    	double spriteScore = filledWeights[0] * this.dangerousToAvatar +
    			filledWeights[1] * ((this.dangerousOtherwise) ? 1 : 0) +
    			filledWeights[2] * ((this.destroyable) ? 1 : 0) +
    			filledWeights[3] * ((this.collectable) ? 1 : 0) +
    			filledWeights[4] * ((this.givingVictory) ? 1 : 0) +
    			filledWeights[5] * ((this.givingDefeat) ? 1 : 0) +
    			filledWeights[6] * this.givingPoints +
    			filledWeights[7] * ((this.passable) ? 1 : 0) +
    			filledWeights[8] * ((this.moving) ? 1 : 0) +
    			filledWeights[9] * this.speed +
    			filledWeights[10] * ((this.increasingValuesOfOtherObjects) ? 1 : 0) +
    			filledWeights[11] * ((this.allowingVictory) ? 1 : 0);
    	return spriteScore/distanceToSprite;
    }
}
