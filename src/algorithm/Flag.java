package algorithm;

public class Flag {
	boolean noRelayExtract;
	float locs;
	float pa;
	public Flag(float locs, float pa, boolean relay) {
		this.locs = locs;
		this.pa = pa;
		noRelayExtract = relay;
	}
	
}
