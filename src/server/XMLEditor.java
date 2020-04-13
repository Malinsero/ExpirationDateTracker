package server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;

import nu.xom.Builder;
import nu.xom.DocType;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;

/**
 * Class that handles the direct access of the XML structure in the context of the
 * expiration tracker database.
 * 
 * @author John Horning (johnhorning@gmail.com)
 * @version April 7, 2020
 *
 */
public class XMLEditor {

	private String storeAddress;
	private Document _doc = null;
	private Element _root = null;
	private Element _items = null; // items that exist in the database.
	private Element _schedule = null; // The items that are set to expire.
	private int currentId = 0; // tracks the id that will be given to the next added item.
	
	/**
	 * Creates an XML editor that stores its data in a file with the specified name.
	 * If the file does not exist, it will be created.
	 * 
	 * @param fileName The name of the file to be used.
	 */
	public XMLEditor(String fileName) {
		storeAddress = fileName;
		
		File file = new File(fileName);
		if (!file.exists()) { // If the file does not exist:
			
			try {
				file.createNewFile(); // create it
				
			} catch (IOException ex) {
				debug(ex.getMessage());
				System.exit(1);
			}
			
			// And initialize the components of the XML file.
			_root = new Element("root");
			_doc = new Document(_root);
			_items = new Element("items");
			_schedule = new Element("schedule");
			_root.appendChild(_items);
			_root.appendChild(_schedule);
			
			
		} else { // If the file did exist, then read its contents instead.
		
			try {
			
				// Build the document based on the contents of the file.
				Builder builder = new Builder();
				_doc = builder.build(file);
			} catch (IOException ex) {
				debug(ex.getMessage());
				System.exit(1);
			
			} catch (ValidityException ex) {
				debug(ex.getMessage());
				System.exit(1);
			
			} catch (ParsingException ex) {
				debug(ex.getMessage());
				System.exit(1);
			
			}
			
			_root = _doc.getRootElement();
			_items = _root.getFirstChildElement("items");
			_schedule = _root.getFirstChildElement("schedule");
		}
		
		// If the file was empty or something, and the subsections are not included:
		if (_items == null) {
			_items = new Element("items"); // Make the subelements.
			_root.appendChild(_items);
		}
		
		if (_schedule == null) {
			_schedule = new Element("schedule");
			_root.appendChild(_schedule);
		}
	}
	
	/**
	 * Creates an XMLEditor for with a default file name of "data.xml"
	 */
	public XMLEditor() {
	    this("data.xml");
	}
	
	/**
	 * Returns all the valid item names in this XML document.
	 * 
	 * @return Iterable<String> of the names of the items that exist in the doc.
	 */
	public Iterable<String> getItemNames() {
		LinkedList<String> names = new LinkedList<>();
		
		Elements categories = _items.getChildElements();
		for (int i = 0; i < categories.size(); i++) {
		    Elements items = categories.get(i).getChildElements();
		    for (int k = 0; k < items.size(); i++) {
		        names.add(items.get(k).getLocalName());
		    }
		}
		
		return (Iterable<String>) names;
	}
	
	/**
	 * Parses the XML schedule and returns a list of item names that are scheduled to expire
	 * within the specified number of weeks.
	 * 
	 * @param weeks How many weeks out you want to return expiring items.
	 * @return An array of integers representing the IDs of the expiring items.
	 */
	public Integer[] getExpiring(int weeks) {
	    CalendarDate limit = new CalendarDate(new Date()); // get the current day.
	    limit.addDays(weeks * 7); // Add the number of weeks to it. This is our cutoff.
	    
	    // Create a collection to store our expiring items.
	    // To avoid counting one item twice, we will use a HashSet.
	    // We will return the elements at the end.
	    HashSet<Integer> expiring = new HashSet<>();
	    
	    Elements items = _schedule.getChildElements(); // get the expiring items.
	    for (int i = 0; i < items.size(); i++) { // for each item scheduled to expire:
	        Element item = items.get(i);
	        // Get the item's expiration date.
	        CalendarDate expr = new CalendarDate(item.getFirstChildElement("date").getValue());
	        
	        if (expr.compareTo(limit) == -1) { // If our item expires before our limit:
	            // Get the id of the expiring item and add it to the table.
	            expiring.add(new Integer(item.getFirstChildElement("Id").getValue()));
	        }
	    }
	    
	    return (Integer[]) expiring.toArray();
	}
	
	/**
	 * Saves the current version of the XML document.
	 * Mainly for use before shutdown.
	 * 
	 */
	public void saveDocument() {
	    try {
	        File data = new File(storeAddress);
	        FileOutputStream out = new FileOutputStream(data);
	        Serializer seri = new Serializer(out, "UTF-8");
	        seri.setIndent(4);
	        seri.setMaxLength(64);;
	        seri.write(_doc);
	    } catch (IOException ex) {
	        debug(ex.getMessage());
	    }
	}
	
	
	
	private void debug(String message) {
		System.out.println("[XML] " + message);
	}
	
	public static void main(String[] args) {
	    Element root = new Element("root");
	    Element coffee = new Element("coffee");
	    Element shelf = new Element("shelfLife");
	    shelf.appendChild("00:07:00");
	    coffee.appendChild(shelf);
	    root.appendChild(coffee);
	    
	    Element breakfast = new Element("roast");
	    Element name = new Element("name");
	    name.appendChild("Breakfast Blend");
	    breakfast.appendChild(name);
	    coffee.appendChild(breakfast);
	    
	    Element french = new Element("roast");
	    name = new Element("name");
	    name.appendChild("French");
	    french.appendChild(name);
	    coffee.appendChild(french);
	    
	    Element espresso = new Element("roast");
	    name = new Element("name");
	    name.appendChild("Espresso");
	    espresso.appendChild(name);
	    coffee.appendChild(espresso);
	    
	    
	    Document doc = new Document(root);
	    
	    try {
	        File data = new File("data.xml");
	        data.createNewFile();
	        FileOutputStream out = new FileOutputStream(data);
	        Serializer seri = new Serializer(out, "UTF-8");
	        seri.setIndent(4);
	        seri.setMaxLength(64);
	        seri.write(doc);
	        
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    }
	    
	    
	}
}
