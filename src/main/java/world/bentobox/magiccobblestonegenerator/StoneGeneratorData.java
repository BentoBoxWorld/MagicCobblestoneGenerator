package world.bentobox.magiccobblestonegenerator;

import com.google.gson.annotations.Expose;
import world.bentobox.bentobox.database.objects.DataObject;

public class StoneGeneratorData implements DataObject {

	/**
	 * UUID Of player (owner of island)
	 */
	@Expose
	private String uniqueId;
	
	/**
	 * Level of the generator
	 */
	@Expose
	private long generatorLevel;
	
	public StoneGeneratorData() {
	}
	
	/**
	 * Create new entry with UUID of player and level
	 * @param uniqueId
	 * @param generatorLevel
	 */
	public StoneGeneratorData(String uniqueId, long generatorLevel) {
		this.uniqueId = uniqueId;
		this.generatorLevel = generatorLevel;
	}
	
	/**
	 * Create new entry with UUID of player with default level of 0
	 * @param uniqueId
	 */
	public StoneGeneratorData(String uniqueId) {
		this(uniqueId, 0);
	}

	@Override
	public String getUniqueId() {
		return uniqueId;
	}

	@Override
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public long getGeneratorLevel() {
		return generatorLevel;
	}

	public void setGeneratorLevel(long generatorLevel) {
		this.generatorLevel = generatorLevel;
	}

	
}
