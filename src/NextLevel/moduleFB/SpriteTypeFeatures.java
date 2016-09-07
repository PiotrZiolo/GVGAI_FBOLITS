package NextLevel.moduleFB;

import NextLevel.utils.LogHandler;

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
	public boolean givingVictoryMove;
	/**
	 * Can it give defeat?
	 */
	public boolean givingDefeatMove;
	/**
	 * Amount of points it give
	 */
	public double changingPointsMove;
	/**
	 * Can avatar pass through it?
	 */
	public boolean givingVictoryUse;
	/**
	 * Can it give defeat?
	 */
	public boolean givingDefeatUse;
	/**
	 * Amount of points it give
	 */
	public double changingPointsUse;
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
	 * were features updated this turn?
	 */
	public boolean featuresUpdatedThisTurn;

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
		this.givingVictoryMove = false;
		this.givingDefeatMove = false;
		this.changingPointsMove = 0;
		this.givingVictoryUse = false;
		this.givingDefeatUse = false;
		this.changingPointsUse = 0;
		this.passable = false;
		this.moving = false;
		this.speed = 0;
		this.changingValuesOfOtherObjects = 0;
		this.allowingVictory = false;
		this.featuresUpdatedThisTurn = true;
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
		this.givingVictoryMove = false;
		this.givingDefeatMove = false;
		this.changingPointsMove = 0;
		this.givingVictoryUse = false;
		this.givingDefeatUse = false;
		this.changingPointsUse = 0;
		this.passable = false;
		this.moving = false;
		this.speed = 0;
		this.changingValuesOfOtherObjects = 0;
		this.allowingVictory = false;
		this.featuresUpdatedThisTurn = true;
	}

	/**
	 * Public constructor with given values
	 */
	public SpriteTypeFeatures(int category, int type, double dangerousToAvatar, boolean dangerousOtherwise,
			boolean destroyable, boolean collectable, boolean givingVictoryMove, boolean givingDefeatMove,
			double changingPointsMove, boolean givingVictoryUse, boolean givingDefeatUse,
			double changingPointsUse, boolean passable, boolean moving, double speed,
			double increasingValuesOfOtherObjects, boolean allowingVictory)
	{
		this.category = category;
		this.type = type;
		this.dangerousToAvatar = dangerousToAvatar;
		this.dangerousOtherwise = dangerousOtherwise;
		this.destroyable = destroyable;
		this.collectable = collectable;
		this.givingVictoryMove = givingVictoryMove;
		this.givingDefeatMove = givingDefeatMove;
		this.changingPointsMove = changingPointsMove;
		this.givingVictoryUse = givingVictoryUse;
		this.givingDefeatUse = givingDefeatUse;
		this.changingPointsUse = changingPointsUse;
		this.passable = passable;
		this.moving = moving;
		this.speed = speed;
		this.changingValuesOfOtherObjects = 0;
		this.allowingVictory = allowingVictory;
		this.featuresUpdatedThisTurn = true;
	}

	public String print()
	{
		return "[" + this.category + ", " + this.type + ", " + this.dangerousToAvatar + ", "
				+ this.dangerousOtherwise + ", " + this.destroyable + ", " + this.collectable + ", "
				+ this.givingVictoryMove + ", " + this.givingDefeatMove + ", " + this.changingPointsMove + ", "
				+ this.givingVictoryUse + ", " + this.givingDefeatUse + ", " + this.changingPointsUse + ", "
				+ this.passable + ", " + this.moving + ", " + this.speed + ", "
				+ this.changingValuesOfOtherObjects + ", " + this.allowingVictory + "]";
		/*LogHandler.writeLog("[" + this.category + ", " + this.type + ", " + this.dangerousToAvatar + ", "
				+ this.dangerousOtherwise + ", " + this.destroyable + ", " + this.collectable + ", "
				+ this.givingVictory + ", " + this.givingDefeat + ", " + this.changingPoints + ", " + this.passable
				+ ", " + this.moving + ", " + this.speed + ", " + this.changingValuesOfOtherObjects + ", "
				+ this.allowingVictory + "]", "SpriteTypeFeatures.print", 0);
		*/
	}
}
