package segmentation;
/*
 * This class is for parameter object. A parameter that is passed to segmentation
 * code and regulates the attributes of the segments to be selected for extraction.
 * Parameters of this kind include -relay={0,1}, -locs={0.41}, -pa={0.34} etc.
 */
public class Parameter {
	String name;
	String value;
	public Parameter() {
		name ="";
		value = "";
	}
	public Parameter(String name, String value){
		this.name = name;
		this.value = value;
	}
	public String getName(){
		return name;
	}
	public String getValue() {
		return value;
	}
	public void setParameter(String name, String value) {
		this.name = name;
		this.value = value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
