package nickguletskii200.CustomCrafting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.yaml.snakeyaml.Yaml;

public class RecipeList extends HashMap<String, HashMap<String, Object>> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5408447074107302872L;

	@SuppressWarnings("unchecked")
	public void load() {
		File file = new File("plugins" + File.separator + "CustomCrafting"
				+ File.separator + "recipelist.yml");
		StringBuffer contents = new StringBuffer();
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));
			String text = null;
			while ((text = reader.readLine()) != null) {
				contents.append(text).append(
						System.getProperty("line.separator"));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Yaml yaml = new Yaml();
		Object object = yaml.load(contents.toString());
		HashMap<Object, HashMap<String, Object>> sysSS = (HashMap<Object, HashMap<String, Object>>) object;
		this.clear();
		for (Object key : sysSS.keySet()) {
			this.put(key.toString(), sysSS.get(key));
		}

	}
}
