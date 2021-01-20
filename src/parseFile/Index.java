package parseFile;

public class Index {
	static int tmp_index;
	public int get() {
		return tmp_index;
	}
	public void reset() {
		tmp_index = 0;
	}
	public int change() {
		tmp_index = tmp_index +1;
		return tmp_index;
	}
}
