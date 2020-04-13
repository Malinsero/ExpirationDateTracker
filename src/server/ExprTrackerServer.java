package server;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import java.io.*;
import java.net.*;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ExprTrackerServer implements ExprTrackerConstants {
	
	
	public ExprTrackerServer() {
		
		JFrame frame = new JFrame("Expiration Date Tracker Server");
		JPanel panel = new JPanel();
		JLabel label = new JLabel("Server is Running");
		
		panel.add(label);
		
		frame.add(panel);
		frame.setSize(450, 450);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.show();
		
		// get ready to create threads for clients
		new Thread() {
			public void run() {
				ServerSocket serverSocket;
				Thread updater = new Thread() {
					ServerSocket listener;
					ObjectInputStream fromClient;
					ObjectOutputStream toClient;
					
					
					public void run() {
						try {
							listener = new ServerSocket(8003);
							
						
							while (true) {
								Socket client;
								
								client = listener.accept();
								fromClient = new ObjectInputStream(client.getInputStream());
								toClient = new ObjectOutputStream(client.getOutputStream());
								
								DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
								fac.setValidating(false);
							
								DocumentBuilder builder = fac.newDocumentBuilder();
								Document doc = builder.parse("Server.xml");
							
								XPathFactory xpathfactory = XPathFactory.newInstance();
								XPath xpath = xpathfactory.newXPath();
							
								Element root = doc.getDocumentElement();
								
								toClient.write(1); // tell the client we're ready
								
								int method = fromClient.readInt(); // get the message we're doing
								
								switch (method) {
								case ADD_ITEM:
									String name = (String) fromClient.readObject(); // get the name of the object adding
									String exprDate = (String) fromClient.readObject(); // get the expiration date.
									
									Element item = doc.createElement("item");
									Element schedule = (Element) xpath.evaluate("//schedule", root, XPathConstants.NODE); // get the schedule
									Element itmName = doc.createElement("name");
									itmName.appendChild(doc.createTextNode(name));
									item.appendChild(itmName);
									Element exprDateElem = doc.createElement("exprDate");
									exprDateElem.appendChild(doc.createTextNode(exprDate));
									item.appendChild(exprDateElem);
									schedule.appendChild(item);
									
									TransformerFactory tf = TransformerFactory.newInstance();
									Transformer trans = tf.newTransformer();
									
									DOMSource source = new DOMSource(doc);
									StreamResult myRes = new StreamResult(new File("Server.xml"));
									
									trans.transform(source, myRes);
									toClient.writeInt(1); // tell server we're done
									break;
									
								case QUERY:
									String queQuery = (String) fromClient.readObject();
									NodeList queResult = (NodeList) xpath.evaluate(queQuery, root, XPathConstants.NODE);
									toClient.writeInt(queResult.getLength());
									for (int i = 0; i < queResult.getLength(); i++) {
										Node resNode = (Node) queResult.item(i);
										Node resText = (Node) xpath.evaluate("text()", resNode, XPathConstants.NODE);
										toClient.writeObject(resText.getNodeValue());
									}
									break;
									
								case REMOVE:
									
									Date today = new Date();
									Calendar cal = Calendar.getInstance();
									cal.setTime(today);
									String exprMonth = String.format("%04d", cal.get(Calendar.YEAR))+":"+String.format("%02d", cal.get(Calendar.MONTH)+1);
									String ReQuery = "//schedule/item[exprDate=\""+exprMonth+"\"/name";
									NodeList reResult = (NodeList) xpath.evaluate(ReQuery, root, XPathConstants.NODESET);
									
									// send the items that expire this month to the user
									toClient.writeInt(reResult.getLength());
									for (int i = 0; i<reResult.getLength(); i++) {
										Node resNode = (Node) reResult.item(i);
										Node resText = (Node) xpath.evaluate("text()", resNode, XPathConstants.NODE);
										toClient.writeObject(resText.getNodeValue());
									}
									
									// remove those items from the schedule
									while (reResult.getLength() > 0) {
										Node node = reResult.item(0);
										node.getParentNode().removeChild(node);
									}
									break;
								}
								
								client.close();
								
								
							}
						} catch (IOException e) {
							e.printStackTrace();
						} catch (XPathExpressionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (SAXException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ParserConfigurationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (TransformerException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				};
				
				updater.start();
				
				try {
					serverSocket = new ServerSocket(8004);
					
					while (true) {
						Socket client;
						
						System.out.println("waiting for client");
						client = serverSocket.accept();
						String ipAddress = client.getInetAddress().getHostAddress();
						System.out.println("client found and connected at " + ipAddress);
						
						new Thread(new HandleSession(client)).start();
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
				}
			}
		}.start();
	}
	
	public static void main(String[] args) {
		ExprTrackerServer server = new ExprTrackerServer();
		while (true);
	}
	
}

class HandleSession implements Runnable, ExprTrackerConstants {
	private final Socket client;
	private Socket listener;
	private ObjectInputStream fromClient;
	private ObjectOutputStream toClient;
	private ObjectInputStream fromListener;
	private ObjectOutputStream toListener;
	
	public HandleSession(Socket client) {
		this.client = client;
		try {
			
			fromClient = new ObjectInputStream(client.getInputStream());
			toClient = new ObjectOutputStream(client.getOutputStream());
			
			this.listener = new Socket("localhost", 8003);
			fromListener = new ObjectInputStream(listener.getInputStream());
			toListener = new ObjectOutputStream(listener.getOutputStream());
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void run() {
		try {
			toClient.writeObject(new Integer(1)); // notify the client that the server is ready
			fromListener.readInt(); // get the notification from listener that it's ready
			
			boolean live = true;
			while (live) {
				
				Integer method; // a variable that gets which method to use from the client
			
				method = fromClient.readInt();
			
				switch (method) {
					case ADD_ITEM:
						Item itm = (Item) fromClient.readObject();
						addItem(itm);
						toClient.writeInt(1); // tell client we're done
						break;
					case QUERY:
						String search = (String) fromClient.readObject();
						String cate = (String) fromClient.readObject();
						int tagNum = (int) fromClient.readInt();
						String[] tags = new String[tagNum];
						
						for (int i = 0; i < tagNum; i++) {
							tags[i] = (String) fromClient.readObject();
						}
						
						String[] result = query(search, cate, tags);
						
						toClient.writeInt(result.length); // tell client how many results are coming.
						
						for (int i = 0; i < result.length; i++) {
							toClient.writeObject(result[i]);
						}
						
						break;
					case REMOVE:
						String[] expiring = remove();
						
						toClient.writeInt(expiring.length);
						
						for (int i = 0; i < expiring.length; i++) {
							toClient.writeObject(expiring[i]);
						}
						
						break;
						
					case KILL:
						live = false;
						break;
						
						
			
				}
			}
			
		} catch(IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Takes an item and adds it to the expiration date tracker schedule.
	 * 
	 * @param itm An item that will expire to be added to the tracker
	 * @throws IOException 
	 */
	private void addItem(Item itm) throws IOException {
		toListener.writeInt(ADD_ITEM);
		toListener.writeObject(itm.getName()); // tell the listener the object name
		toListener.writeObject(itm.getExprDate()); // tell the listener the object date
		fromListener.readInt(); // get the notification from listener that we're done
	}
	
	/**
	 * Search to see if an item exists in the database. This checks for an item definition,
	 * it does not search for expiring items.
	 * 
	 * @param search The string entered into the search field
	 * @param cate The category selected by the user
	 * @param tags The tags selected by the user
	 * @return An array of items found by the query
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	private String[] query(String search, String cate, String[] tags) throws IOException, ClassNotFoundException {
		toListener.writeInt(QUERY);
		
		String searchStr = "";
		if (search != null && search != "" ) {
			searchStr = searchStr + "(//items//*[name=\""+search+"\"] | //items//*[sku=\""+search+"\"])";
		}
		if (cate != null && cate != "") {
			if (searchStr != null && searchStr != "") {
				searchStr = "//"+cate+"//*[."+searchStr+"]";
			} else {
				searchStr = "//"+cate+"//*";
			}
		}
		if (tags != null && tags.length != 0) {
			for (int i = 0; i < tags.length; i++) {
				if (searchStr != null && searchStr != "") {
					searchStr = "//*//*[tag=\'"+tags[i]+"\"]";
				} else {
					searchStr = "//*//*[tag=\""+tags[i]+"\"]";
				}
			}
		}
		
		if (searchStr == "") {
			return null;
		}
		
		toListener.writeObject(searchStr+"/name");
		
		int pings = fromListener.readInt(); // get the number of results
		String[] myResults = new String[pings];
		
		for (int i = 0; i < pings; i++) {
			myResults[i] = (String) fromListener.readObject();
		}
		
		return myResults;
		
	}
	
	/**
	 * Search for items that are expiring within the next month. When an item is returned,
	 * it is removed from the schedule.
	 * 
	 * @param id The id of the schedule entry in the database
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * 
	 */
	private String[] remove() throws IOException, ClassNotFoundException {
		toListener.writeInt(REMOVE);
		int pings = fromListener.readInt(); // get the number of things expiring.
		String[] myResults = new String[pings];
		
		for (int i = 0; i < pings; i++) {
			myResults[i] = (String) fromListener.readObject();
		}
		
		return myResults;
	}
}
