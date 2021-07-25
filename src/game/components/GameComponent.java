package game.components;

public abstract class GameComponent {
	
	/**
	 * Used when instantiating an instance of the Thing.
	 * For example, when instantiating a Thing with an Inventory,
	 * it should create a copy of the inventory rather than using a shared one.
	 * @return
	 */
	public abstract GameComponent instance();
}
