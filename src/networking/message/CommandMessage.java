package networking.message;

import java.io.*;

import utils.*;

public class CommandMessage implements Externalizable {

	private CommandType command;
	private int thingID;
	private TileLoc target;
	/** Used only by serialization */
	public CommandMessage() {
	}
	
	public CommandMessage(int thingID, CommandType command, TileLoc target) {
		this.thingID = thingID;
		this.command = command;
		this.target = target;
	}
	public CommandType getCommand() {
		return command;
	}
	public int getThingID() {
		return thingID;
	}
	public TileLoc getTargetLocation() {
		return target;
	}
	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		command = CommandType.values()[input.readInt()];
		thingID = input.readInt();
		target = new TileLoc();
		target.readExternal(input);
	}
	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeInt(command.ordinal());
		output.writeInt(thingID);
		target.writeExternal(output);
	}
	
	@Override
	public String toString() {
		return command.name() + " " + thingID + " " + target;
	}
}
