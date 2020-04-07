
import java.io.Serializable;
import java.util.Calendar;

public class Item implements Serializable {

	private String name;
	private String SKU;
	private String exprDate;
	
	public Item(String name) {
		this.name = name;
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, 7);
		int month = c.get(Calendar.MONTH) + 1;
		int year = c.get(Calendar.YEAR);
		this.exprDate = String.format("%04d", year)+":"+ String.format("%02d", month);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setSKU(String SKU) {
		this.SKU = SKU;
	}
	
	public void setExprDate(String exprDate) {
		this.exprDate = exprDate;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getSKU() {
		return this.SKU;
	}
	
	public String getExprDate() {
		return this.exprDate;
	}
	
}
