package harmanal;

import java.util.List;

/**
 * Class to encapsulate all keys
 */

public class Key {
	
	public int root;
	public int keyType;

	public Key(int root, int keyType) {
		this.root = root;
		this.keyType = keyType;
	}
	
	public int[] getScale() {
		int[] result = new int[7];
		List<String> scaleIntervals = Chordanal.scaleTable.getKeys(Integer.toString(keyType));
		
		int tone = 0;
		result[tone] = root;
		tone++;
		for (String interval : scaleIntervals) {
			result[tone] = (root + Integer.parseInt(interval)) % 12;
			tone++;
		}
		return result;
	}
	
	public Harmony getScaleHarmony() {
		int[] tones = getScale();
		for (int i = 0; i < tones.length; i++) {
			tones[i] = tones[i] + 60;
		}
		return new Harmony(tones);
	}
	
	public Harmony getTonic() {
		int[] tonic = new int[3];
		tonic[0] = getScale()[0];
		tonic[1] = getScale()[2];
		tonic[2] = getScale()[4];
		return new Harmony(tonic);
	}
	
	public Harmony getSubdominant() {
		int[] subdominant = new int[3];
		subdominant[0] = getScale()[0];
		subdominant[1] = getScale()[3];
		subdominant[2] = getScale()[5];
		return new Harmony(subdominant);
	}
	
	public Harmony getDominant() {
		int[] dominant = new int[3];
		dominant[0] = getScale()[1];
		dominant[1] = getScale()[4];
		dominant[2] = getScale()[6];
		return new Harmony(dominant);
	}
	
	
	
}