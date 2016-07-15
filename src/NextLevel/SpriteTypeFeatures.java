package NextLevel;

public class SpriteTypeFeatures
{
	/**
	 * Category of the sprite
	 */
	public int category;
	/**
	 * Type of the sprite
	 */
	public int type;
	
	/**
	 * Amount of health it can take from avatar
	 */
	public double dangerousToAvatar;
	/**
	 * Can it decrease points of avatar indirectly (e.g. by colliding with other
	 * object?
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
	public double changingPoints;
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
	public double changingValuesOfOtherObjects;
	/**
	 * does collecting/destroying it allow to achieve victory?
	 */
	public boolean allowingVictory;

	/**
	 * Public constructor with default values
	 */
	public SpriteTypeFeatures()
	{
		this.category = 4;
		this.type = 0;
		this.dangerousToAvatar = 0;
		this.dangerousOtherwise = false;
		this.destroyable = false;
		this.collectable = false;
		this.givingVictory = false;
		this.givingDefeat = false;
		this.changingPoints = 0;
		this.passable = false;
		this.moving = false;
		this.speed = 0;
		this.changingValuesOfOtherObjects = 0;
		this.allowingVictory = false;
	}
	
	/**
	 * Public constructor with default values
	 */
	public SpriteTypeFeatures(int category, int type)
	{
		this.category = category;
		this.type = type;
		this.dangerousToAvatar = 0;
		this.dangerousOtherwise = false;
		this.destroyable = false;
		this.collectable = false;
		this.givingVictory = false;
		this.givingDefeat = false;
		this.changingPoints = 0;
		this.passable = false;
		this.moving = false;
		this.speed = 0;
		this.changingValuesOfOtherObjects = 0;
		this.allowingVictory = false;
	}

	/**
	 * Public constructor with given values
	 */
	public SpriteTypeFeatures(int category, int type, double dangerousToAvatar, boolean dangerousOtherwise, boolean destroyable,
			boolean collectable, boolean givingVictory, boolean givingDefeat, double changingPoints, boolean passable,
			boolean moving, double speed, double increasingValuesOfOtherObjects, boolean allowingVictory)
	{
		this.category = category;
		this.type = type;
		this.dangerousToAvatar = dangerousToAvatar;
		this.dangerousOtherwise = dangerousOtherwise;
		this.destroyable = destroyable;
		this.collectable = collectable;
		this.givingVictory = givingVictory;
		this.givingDefeat = givingDefeat;
		this.changingPoints = changingPoints;
		this.passable = passable;
		this.moving = moving;
		this.speed = speed;
		this.changingValuesOfOtherObjects = 0;
		this.allowingVictory = allowingVictory;
	}
}
