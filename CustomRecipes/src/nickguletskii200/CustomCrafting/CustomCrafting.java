package nickguletskii200.CustomCrafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Spyer for Bukkit
 * 
 * @author nickguletskii200
 */
public class CustomCrafting extends JavaPlugin {
	Logger log;
	private String pluginName;
	private int loadedRecipes = 0;
	private int totalRecipes = 0;
	// private PermissionHandler Permissions;
	public CustomCrafting() {
		super();
	}
	// Rotate an integer 2D arraylist
	public ArrayList<ArrayList<Integer>> rotateArray(
			ArrayList<ArrayList<Integer>> shapee) {
		ArrayList<ArrayList<Integer>> shapeOut = new ArrayList<ArrayList<Integer>>();
		for (int y = 0; y < shapee.get(0).size(); y++) {
			shapeOut.add(y, new ArrayList<Integer>());
		}
		for (int x = 0; x < shapee.size(); x++) {
			for (int y = 0; y < shapee.get(0).size(); y++) {
				shapeOut.get(y).add(x, shapee.get(x).get(y));
			}
		}
		return shapeOut;
	}
	// Rotate a byte 2D arraylist
	public ArrayList<ArrayList<Byte>> rotateArrayB(
			ArrayList<ArrayList<Byte>> shapee) {
		ArrayList<ArrayList<Byte>> shapeOut = new ArrayList<ArrayList<Byte>>();
		for (int y = 0; y < shapee.get(0).size(); y++) {
			shapeOut.add(y, new ArrayList<Byte>());
		}
		for (int x = 0; x < shapee.size(); x++) {
			for (int y = 0; y < shapee.get(0).size(); y++) {
				shapeOut.get(y).add(x, shapee.get(x).get(y));
			}
		}
		return shapeOut;
	}

	public void onEnable() {
		log = this.getServer().getLogger();
		pluginName = "[" + this.getDescription().getName() + "]";
		RecipeList rl = new RecipeList();
		rl.load();
		Iterator<String> iter = rl.keySet().iterator();
		while (iter.hasNext()) {
			totalRecipes++;
			String recordName = iter.next();
			HashMap<String, Object> hm = rl.get(recordName);
			if (hm.containsKey("Type")) {
				if (hm.get("Type").equals("Shaped")) {
					doShaped(hm, recordName);
				} else if (hm.get("Type").equals("Shapeless")) {
					doShapeless(hm, recordName);
				} else if (hm.get("Type").equals("Furnace")) {
					doFurnace(hm, recordName);
				}
			} else {
				doShaped(hm, recordName);
			}
		}
		log.info(pluginName + " - " + loadedRecipes + " of " + totalRecipes + " recipes were loaded successfully.");
		log.info(pluginName + " - v" + getDescription().getVersion() + " by " + getDescription().getAuthors() + " enabled!");
	}
	//Load a shaped recipe
	@SuppressWarnings("unchecked")
	public void doShaped(HashMap<String, Object> hm, String recordName) {
		Integer resultID;
		Integer resultQuantity;
		try {
		resultID = (Integer) hm.get("Result");
		resultQuantity = (Integer) hm.get("Quantity");
		} catch (Exception e) {
			log.severe(pluginName + " - ERROR result found with non-integer resultID, or Quantity. Please fix recipe named: " + recordName);
			return;
		}
		//Create our variables here.
		ArrayList<ArrayList<Integer>> shape;
		ArrayList<ArrayList<Byte>> data = null;
		boolean dv = true;

		//Try to load shape data.
		try {
			if (!hm.containsKey("DataValues")) {
				shape = (ArrayList<ArrayList<Integer>>) hm.get("Shape");
			} else {
				shape = ids((ArrayList<ArrayList<String>>) hm.get("Shape"));
			}
			dv = hm.containsKey("DataValues");
			if (dv) {
				data = data((ArrayList<ArrayList<String>>) hm.get("Shape"));
				data = rotateArrayB(data);
			}
			shape = rotateArray(shape);
		} catch (Exception e) {
			log.severe(pluginName + " error parsing shape data in recipe named: " + recordName);
			return;
		}

		char[][] chars = new char[shape.size()][shape.get(0).size()];

		HashMap<String, Character> tmpc = new HashMap<String, Character>();
		String key = "abcdefghi";
		for (int x = 0; x < shape.size(); x++) {
			for (int y = 0; y < shape.get(x).size(); y++) {
				if (dv) {
					// chars[x][y] = (char) ((shape.get(x).get(y).toString()
					// .hashCode() + data.get(x).get(y).hashCode()));
					if (shape.get(x).get(y) == 0) {
						chars[x][y] = ' ';
					} else if (tmpc.containsKey(shape.get(x).get(y) + ","
							+ data.get(x).get(y))) {
						chars[x][y] = tmpc.get(shape.get(x).get(y) + ","
								+ data.get(x).get(y));
					} else {
						chars[x][y] = key.charAt(0);
						key = key.substring(1, key.length());
						tmpc.put(
								shape.get(x).get(y) + "," + data.get(x).get(y),
								chars[x][y]);
					}
				} else {
					if (shape.get(x).get(y) == 0) {
						chars[x][y] = ' ';
					} else {
						chars[x][y] = (char) ((int) shape.get(x).get(y));
					}
				}
			}
		}
		ShapedRecipe sr = null;
		//Try to load ResultData and ResultDamage
		try {
			if (hm.containsKey("ResultData") && hm.containsKey("ResultDamage")) {
				sr = new ShapedRecipe(new ItemStack(resultID, resultQuantity,
						((Integer) hm.get("ResultDamage")).shortValue(),
						((Integer) hm.get("ResultData")).byteValue()));
			}
			if (hm.containsKey("ResultDamage")) {
				sr = new ShapedRecipe(new ItemStack(resultID, resultQuantity,
						((Integer) hm.get("ResultDamage")).shortValue()));
			} else if (hm.containsKey("ResultData")) {
				sr = new ShapedRecipe(new ItemStack(resultID, resultQuantity,
						((Integer) hm.get("ResultData")).byteValue()));
			} else {
				sr = new ShapedRecipe(new ItemStack(resultID, resultQuantity));
			}
		} catch (Exception e) {
			log.severe(pluginName + " - error parsing ResultData or Damage in recipe named: " + recordName);
			return;
		}

		String[] strs = new String[chars[0].length];
		for (int y = 0; y < chars[0].length; y++) {
			String str = "";
			for (int x = 0; x < chars.length; x++) {
				if (chars[x][y] != (char) 0) {
					str += chars[x][y];
				} else {
					str += " ";
				}
			}
			strs[y] = str;
		}
		sr = sr.shape(strs);

		for (int x = 0; x < chars.length; x++) {
			for (int y = 0; y < chars[x].length; y++) {
				if (chars[x][y] != (char) 0) {
					if (dv) {
						if (!(chars[x][y] == ' ')) {
							sr.setIngredient(chars[x][y], new MaterialData(
									shape.get(x).get(y), data.get(x).get(y)));
						}

					} else {
						if (!(chars[x][y] == ' ')) {
							sr.setIngredient(chars[x][y], new MaterialData(
									shape.get(x).get(y)));
						}
					}
				}
			}
		}
		this.getServer().addRecipe(sr);
		loadedRecipes++;
	}
	//Load a shapeless recipe
	@SuppressWarnings("unchecked")
	public void doShapeless(HashMap<String, Object> hm, String recordName) {
		Integer resultID;
		Integer resultQuantity;
		try {
		resultID = (Integer) hm.get("Result");
		resultQuantity = (Integer) hm.get("Quantity");
		} catch (Exception e) {
			log.severe(pluginName + " - ERROR result found with non-integer resultID, or Quantity, please fix recipe named: " + recordName);
			return;
		}
		ShapelessRecipe sr = null;
		//Try to load Durability data in - spit an error if there's any sort of error
		try {
			if (hm.containsKey("ResultData") && hm.containsKey("ResultDamage")) {
				sr = new ShapelessRecipe(new ItemStack(resultID, resultQuantity,
						((Integer) hm.get("ResultDamage")).shortValue(),
						((Integer) hm.get("ResultData")).byteValue()));
			} else if (hm.containsKey("ResultDamage")) {
				sr = new ShapelessRecipe(new ItemStack(resultID, resultQuantity,
						((Integer) hm.get("ResultDamage")).shortValue()));
			} else if (hm.containsKey("ResultDamage")) {
				sr = new ShapelessRecipe(new ItemStack(resultID, resultQuantity,
						((Integer) hm.get("ResultData")).byteValue()));
			} else {
				sr = new ShapelessRecipe(new ItemStack(resultID, resultQuantity));
			}
		} catch (Exception e) {
			log.severe(pluginName + " - error parsing ResultData or Damage in recipe named: " + recordName);
			return;
		}
		HashMap<Integer, Integer> mar = null;
		HashMap<Integer, Byte> data = null;
		boolean dv = hm.containsKey("DataValues");
		//Try to load Material data.
		try {
			if (dv) {
				mar = ids((HashMap<Integer, String>) hm.get("Materials"));
				data = data((HashMap<Integer, String>) hm.get("Materials"));
			} else {
				mar = (HashMap<Integer, Integer>) hm.get("Materials");
			}
			for (Integer i : mar.keySet()) {
				if (dv) {
					sr.addIngredient(mar.get(i), new MaterialData(Integer
							.valueOf(i), data.get(i)));
				} else {
					sr.addIngredient(mar.get(i), new MaterialData(Integer
							.valueOf(i)));
				}
			}
		} catch (Exception e) {
			log.severe(pluginName + " - error parsing Materials in recipe named: " + recordName);
			return;
		}
		this.getServer().addRecipe(sr);
		loadedRecipes++;
	}

	//Shapeless ids
	public HashMap<Integer, Integer> ids(HashMap<Integer, String> arg) {
		HashMap<Integer, Integer> out = new HashMap<Integer, Integer>();
		for (Integer key : arg.keySet()) {
			out.put(key, Integer.valueOf(arg.get(key).split("/")[0]));
		}
		return out;
	}

	//Shapeless data
	public HashMap<Integer, Byte> data(HashMap<Integer, String> hashMap) {
		HashMap<Integer, Byte> out = new HashMap<Integer, Byte>();
		for (Integer key : hashMap.keySet()) {
			out.put(key, (Integer.valueOf(hashMap.get(key).split("/")[1]))
					.byteValue());
		}
		return out;
	}

	//Shaped ids
	public ArrayList<ArrayList<Integer>> ids(ArrayList<ArrayList<String>> ar) {
		ArrayList<ArrayList<Integer>> tb = new ArrayList<ArrayList<Integer>>();
		for (int x = 0; x < ar.size(); x++) {
			tb.add(x, new ArrayList<Integer>());
			for (int y = 0; y < ar.get(x).size(); y++) {
				String tmp = ar.get(x).get(y).split("/")[0];
				tb.get(x).add(y, Integer.valueOf(tmp));
			}
		}
		return tb;
	}

	//Shaped data
	public ArrayList<ArrayList<Byte>> data(ArrayList<ArrayList<String>> ar) {
		ArrayList<ArrayList<Byte>> tb = new ArrayList<ArrayList<Byte>>();
		for (int x = 0; x < ar.size(); x++) {
			tb.add(x, new ArrayList<Byte>());
			for (int y = 0; y < ar.get(x).size(); y++) {
				tb.get(x).add(
						y,
						(Integer.valueOf(ar.get(x).get(y).split("/")[1]))
						.byteValue());
			}
		}
		return tb;
	}

	//Load a furnace recipe
	public void doFurnace(HashMap<String, Object> hm, String recordName) {
		Integer resultID;
		Integer resultQuantity;
		Integer source;
		try {
			resultID = (Integer) hm.get("Result");
			resultQuantity = (Integer) hm.get("Quantity");
			source = (Integer) hm.get("Source");
		} catch (Exception e) {
			log.severe(pluginName + " - ERROR result found with non-integer resultID, Quantity, or Source please fix recipe named: " + recordName);
			return;
		}
		FurnaceRecipe fr;
		ItemStack is;
		try {
			if (hm.containsKey("ResultData")) {
				is = new ItemStack(resultID, resultQuantity, ((Integer) hm
						.get("ResultData")).byteValue());
			} else {
				is = new ItemStack(resultID, resultQuantity);
			}
			if (hm.containsKey("ResultDamage")) {
				is.setDurability((Short) hm.get("ResultDamage"));
			}
			if (hm.containsKey("SourceData")) {
				MaterialData md = new MaterialData(source, ((Integer) hm
						.get("SourceData")).byteValue());
				ItemStack tmp = md.toItemStack();
				tmp.setDurability(((Integer) hm.get("SourceData")).byteValue());
				md = tmp.getData();
				md.setData(((Integer) hm.get("SourceData")).byteValue());
				fr = new FurnaceRecipe(is, md);
				fr.setInput(md);

			} else {
				fr = new FurnaceRecipe(is, new MaterialData(source));
			}
			this.getServer().addRecipe(fr);
			loadedRecipes++;
		} catch (Exception e) {
			log.severe(pluginName + " - error loading furnace recipe named: " + recordName);
		}
	}

	public void onDisable() {
		
	}

}
