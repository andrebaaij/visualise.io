package models;

public class Entity {
	public String name,
		description,
		thumbnail,
		instance;
	
	public Integer id;
	
	public Entity() {
		
	}
	
	public Entity(Integer id) {
		this.id = id;
	}
}
