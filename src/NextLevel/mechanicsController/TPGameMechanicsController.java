package NextLevel.mechanicsController;

import NextLevel.moduleTP.TPGameKnowledge;
import core.game.StateObservationMulti;
import ontology.Types;
import tools.Vector2d;

public class TPGameMechanicsController extends GameMechanicsController
{
	// Real types of fields
	// protected TPGameKnowledge gameKnowledge;
	
	public TPGameMechanicsController(TPGameKnowledge gameKnowledge)
	{
		this.gameKnowledge = gameKnowledge;
	}
	
	public boolean isSpriteOneMoveFromAvatarWithOpponentRotation(Vector2d observationPosition, Vector2d avatarPosition,
			StateObservationMulti currentState, Vector2d avatarOrientation, int spriteType)
	{
		TPGameKnowledge gameKnowledge = (TPGameKnowledge) this.gameKnowledge;
		int playerID = gameKnowledge.getPlayerID();
		int oppID = gameKnowledge.getOppID();
		
		double speedInPixels = currentState.getBlockSize() * currentState.getAvatarSpeed(playerID);
		Vector2d distance = observationPosition.copy().subtract(avatarPosition);

		if ((Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_NIL
				|| Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_DOWN)
				&& Math.abs(distance.x) < currentState.getBlockSize()
				&& Math.abs(distance.y - speedInPixels) < currentState.getBlockSize())
		{
			if (Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_NIL
					&& spriteType == -1 - oppID
					&& Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_UP)
			{
				Types.ACTIONS[] actions = new Types.ACTIONS[2];
				actions[playerID] = Types.ACTIONS.ACTION_NIL;
				actions[oppID] = Types.ACTIONS.ACTION_UP;
				currentState.advance(actions);
			}
			return true;
		}
		if ((Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_NIL
				|| Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_UP)
				&& Math.abs(distance.x) < currentState.getBlockSize()
				&& Math.abs(distance.y + speedInPixels) < currentState.getBlockSize())
		{
			if (Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_NIL
					&& spriteType == -1 - oppID
					&& Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_DOWN)
			{
				Types.ACTIONS[] actions = new Types.ACTIONS[2];
				actions[playerID] = Types.ACTIONS.ACTION_NIL;
				actions[oppID] = Types.ACTIONS.ACTION_DOWN;
				currentState.advance(actions);
			}
			return true;
		}
		if ((Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_NIL
				|| Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_RIGHT)
				&& Math.abs(distance.x - speedInPixels) < currentState.getBlockSize()
				&& Math.abs(distance.y) < currentState.getBlockSize())
		{
			if (Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_NIL
					&& spriteType == -1 - oppID
					&& Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_LEFT)
			{
				Types.ACTIONS[] actions = new Types.ACTIONS[2];
				actions[playerID] = Types.ACTIONS.ACTION_NIL;
				actions[oppID] = Types.ACTIONS.ACTION_LEFT;
				currentState.advance(actions);
			}
			return true;
		}
		if ((Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_NIL
				|| Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_LEFT)
				&& Math.abs(distance.x + speedInPixels) < currentState.getBlockSize()
				&& Math.abs(distance.y) < currentState.getBlockSize())
		{
			if (Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_NIL
					&& spriteType == -1 - oppID
					&& Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_RIGHT)
			{
				Types.ACTIONS[] actions = new Types.ACTIONS[2];
				actions[playerID] = Types.ACTIONS.ACTION_NIL;
				actions[oppID] = Types.ACTIONS.ACTION_RIGHT;
				currentState.advance(actions);
			}
			return true;
		}
		return false;
	}
}
