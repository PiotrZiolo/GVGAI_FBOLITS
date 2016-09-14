package FBOLITS.moduleFB.MechanicsController;

import FBOLITS.mechanicsController.GameMechanicsController;
import FBOLITS.moduleFB.FBGameKnowledge;
import FBOLITS.moduleFB.SpriteTypeFeatures;
import core.game.Observation;
import core.game.StateObservation;

public class FBGameMechanicsController extends GameMechanicsController
{
	// Real types of fields
	// protected TPGameKnowledge gameKnowledge;
	boolean treatFromAvatarAsSprite = false;

	public FBGameMechanicsController(FBGameKnowledge gameKnowledge)
	{
		this.gameKnowledge = gameKnowledge;
	}

	public boolean isSpriteWall(SpriteTypeFeatures sprite)
	{
		return (sprite.category == 4 && !sprite.passable && !sprite.destroyable);
	}

	public boolean isSpriteDoingNothing(SpriteTypeFeatures sprite)
	{
		if (sprite == null)
			return true;

		return (!sprite.collectable && !sprite.allowingVictory && !sprite.dangerousOtherwise
				&& !sprite.givingVictoryMove && !sprite.givingDefeatMove && sprite.changingPointsMove == 0
				&& !sprite.givingVictoryUse && !sprite.givingDefeatUse && sprite.changingPointsUse == 0
				&& sprite.changingValuesOfOtherObjects == 0 && sprite.dangerousToAvatar == 0);
	}

	public Observation getSpriteTypeRepresentative(int spriteType, StateObservation stateObs)
	{
		// TODO The method from GameMechanicsController can be optimized. 
		// Category for the type can be obtained from SpriteTypeFeatures and then only this category has to be searched.
		return super.getSpriteTypeRepresentative(spriteType, stateObs);
	}
}
