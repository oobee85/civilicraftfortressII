package game.components;

import java.util.*;

import game.*;

public class ComponentTest {
	
	public void run() {
		Component dc = new DamageResistance(DamageResistance.getDefaultResistance());
		Component tc = new TestComponent();
		String name = DamageResistance.class.getSimpleName();
		System.out.println(name);
		System.out.println(dc.getClass().getSimpleName());
		Class c = dc.getClass();
		HashMap<Class, Integer> hashmap;
	}
	

	public static void main(String[] args) {
		new ComponentTest().run();
	}

}
