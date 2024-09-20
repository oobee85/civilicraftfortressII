package networking.message;

import java.io.*;

import game.actions.PlannedAction;
import utils.*;

public class CommandMessage implements Externalizable {

	private CommandType command;
	private int thingID;
	private int targetX;
	private int targetY;
	private int targetID = -1;
	private int faction;
	private String type = "";
	private boolean clearQueue;
	private int amount;
	private PlannedAction action;
	/** Used only by serialization */
	public CommandMessage() {
	}
	
	public static CommandMessage makeSetRallyPointCommand(int thingID, TileLoc target) {
		CommandMessage msg = new CommandMessage();
		msg.command = CommandType.SET_RALLY_POINT;
		msg.thingID = thingID;
		msg.targetX = target.x();
		msg.targetY = target.y();
		return msg;
	}
	public static CommandMessage makePlanBuildingCommand(int thingID, TileLoc target, boolean clearQueue, String buildingType) {
		CommandMessage msg = new CommandMessage();
		msg.command = CommandType.PLAN_BUILDING;
		msg.thingID = thingID;
		msg.targetX = target.x();
		msg.targetY = target.y();
		msg.clearQueue = clearQueue;
		msg.type = buildingType;
		return msg;
	}
	public static CommandMessage makeStopCommand(int thingID) {
		CommandMessage msg = new CommandMessage();
		msg.command = CommandType.STOP;
		msg.thingID = thingID;
		return msg;
	}
	public static CommandMessage makeResearchCommand(int factionID, String researchType) {
		CommandMessage msg = new CommandMessage();
		msg.command = CommandType.RESEARCH;
		msg.faction = factionID;
		msg.type = researchType;
		return msg;
	}
	public static CommandMessage makeCraftItemCommand(int factionID, String itemType, int amount) {
		CommandMessage msg = new CommandMessage();
		msg.command = CommandType.CRAFT_ITEM;
		msg.faction = factionID;
		msg.type = itemType;
		msg.amount = amount;
		return msg;
	}
	public static CommandMessage makeProduceUnitCommand(int thingID, String unitType) {
		CommandMessage msg = new CommandMessage();
		msg.command = CommandType.PRODUCE_UNIT;
		msg.thingID = thingID;
		msg.type = unitType;
		return msg;
	}
	public static CommandMessage makePlannedActionCommand(int thingID, PlannedAction action, boolean clearQueue) {
		CommandMessage msg = new CommandMessage();
		msg.thingID = thingID;
		msg.action = action;
		msg.command = CommandType.PLANNED_ACTION;
		msg.clearQueue = clearQueue;
		return msg;
	}
	

	public CommandType getCommand() {
		return command;
	}
	public int getThingID() {
		return thingID;
	}
	public TileLoc getTargetLocation() {
		return new TileLoc(targetX, targetY);
	}
	public int getTargetID() {
		return targetID;
	}
	public boolean getClearQueue() {
		return clearQueue;
	}
	public int getFaction() {
		return faction;
	}
	public String getType() {
		return type;
	}
	public int getAmount() {
		return amount;
	}
	public PlannedAction getPlannedAction() {
		return action;
	}
	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		command = CommandType.values()[input.readInt()];
		thingID = input.readInt();
		targetX = input.readInt();
		targetY = input.readInt();
		targetID = input.readInt();
		faction = input.readInt();
		amount = input.readInt();
		type = input.readUTF();
		clearQueue = input.readBoolean();
		action = (PlannedAction) input.readObject();
	}
	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeInt(command.ordinal());
		output.writeInt(thingID);
		output.writeInt(targetX);
		output.writeInt(targetY);
		output.writeInt(targetID);
		output.writeInt(faction);
		output.writeInt(amount);
		output.writeUTF(type);
		output.writeBoolean(clearQueue);
		output.writeObject(action);
	}
	
	@Override
	public String toString() {
		return command.name() + " " + thingID + " " + getTargetLocation() + " " + targetID;
	}
}
