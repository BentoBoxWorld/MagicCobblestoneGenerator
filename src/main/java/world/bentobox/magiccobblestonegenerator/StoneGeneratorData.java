package world.bentobox.magiccobblestonegenerator;

import com.google.gson.annotations.Expose;
import world.bentobox.bentobox.database.objects.DataObject;

public class StoneGeneratorData implements DataObject {

	@Expose
	private String uniqueId;
	@Expose
	private long generatorLevel;
	
	public StoneGeneratorData() {
	}
	
	public StoneGeneratorData(String uniqueId, long generatorLevel) {
		this.uniqueId = uniqueId;
		this.generatorLevel = generatorLevel;
	}
	
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
