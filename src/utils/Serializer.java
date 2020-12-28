package utils;

import java.io.*;

public class Serializer {
	public static TileLoc readTileLoc(ObjectInput in) throws IOException {
		int x = in.readInt();
		int y = in.readInt();
		return new TileLoc(x, y);
	}
	public static void write(ObjectOutput out, TileLoc tileLoc) throws IOException {
		out.writeInt(tileLoc.x());
		out.writeInt(tileLoc.y());
	}
}
