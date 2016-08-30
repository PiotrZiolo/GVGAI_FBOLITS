package NextLevel.mechanicsController;

import java.util.TreeSet;

import NextLevel.moduleFB.SpriteTypeFeatures;
import NextLevel.moduleTP.TPGameKnowledge;
import NextLevel.moduleTP.TPSituationOfInterest;
import core.game.Event;
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

	public TreeSet<Event> getEventsDuringSOI(TPSituationOfInterest soi)
	{
		TreeSet<Event> events = (TreeSet<Event>) soi.afterState.getEventsHistory().clone();
		events.removeAll(soi.baseState.getEventsHistory());
		return events;
	}

	public int getNumberOfResourcesGainedDuringSOI(TPSituationOfInterest soi, int playerID)
	{

		return ((StateObservationMulti) soi.afterState).getAvatarResources(playerID).size()
				- ((StateObservationMulti) soi.baseState).getAvatarResources(playerID).size();
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

	public boolean isSpriteWall(SpriteTypeFeatures sprite)
	{
		return (sprite.type == 4 && !sprite.passable && !sprite.destroyable);
	}

	public boolean isSpriteDoingNothing(SpriteTypeFeatures sprite)
	{
		return (!sprite.collectable && !sprite.allowingVictory && !sprite.dangerousOtherwise && !sprite.givingVictory
				&& !sprite.givingDefeat && sprite.changingPoints == 0 && sprite.changingValuesOfOtherObjects == 0
				&& sprite.dangerousToAvatar == 0);
	}
}
