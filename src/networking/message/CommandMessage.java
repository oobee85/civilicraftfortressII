package networking.message;

import java.io.*;

import utils.*;

public class CommandMessage implements Externalizable {

	private CommandType command;
	private int thingID;
	private int targetX;
	private int targetY;
	private int targetID;
	private int faction;
	private String type = "";
	private boolean clearQueue;
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
	public static CommandMessage makeMoveToCommand(int thingID, TileLoc target, boolean clearQueue) {
		CommandMessage msg = new CommandMessage();
		msg.command = CommandType.MOVE_TO;
		msg.thingID = thingID;
		msg.targetX = target.x();
		msg.targetY = target.y();
		msg.clearQueue = clearQueue;
		return msg;
	}
	public static CommandMessage makeAttackThingCommand(int thingID, int targetID, boolean clearQueue) {
		CommandMessage msg = new CommandMessage();
		msg.command = CommandType.ATTACK_THING;
		msg.thingID = thingID;
		msg.targetID = targetID;
		msg.clearQueue = clearQueue;
		return msg;
	}
	public static CommandMessage makeBuildTargetCommand(int thingID, int targetID, boolean clearQueue) {
		CommandMessage msg = new CommandMessage();
		msg.command = CommandType.BUILD_THING;
		msg.thingID = thingID;
		msg.targetID = targetID;
		msg.clearQueue = clearQueue;
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
		msg.type = researchType;
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
	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		command = CommandType.values()[input.readInt()];
		thingID = input.readInt();
		targetX = input.readInt();
		targetY = input.readInt();
		targetID = input.readInt();
		faction = input.readInt();
		type = input.readUTF();
		clearQueue = input.readBoolean();
	}
	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeInt(command.ordinal());
		output.writeInt(thingID);
		output.writeInt(targetX);
		output.writeInt(targetY);
		output.writeInt(targetID);
		output.writeInt(faction);
		output.writeUTF(type);
		output.writeBoolean(clearQueue);
	}
	
	@Override
	public String toString() {
		return command.name() + " " + thingID + " " + getTargetLocation();
	}
}
