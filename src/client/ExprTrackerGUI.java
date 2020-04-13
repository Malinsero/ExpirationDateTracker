package client;

import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.awt.*;
import javax.swing.*;


public class ExprTrackerGUI {
	
	protected JFrame frame;
	protected JPanel panel;
	protected JLabel notifyLbl;
	protected JTextField searchJTF;
	protected JButton searchBtn;
	protected ButtonGroup cateBoxes;
	protected JRadioButton[] cateBtns;
	protected JCheckBox[] tagBoxes;
	protected JPanel resultsPnl;
	protected JCheckBox[] resultBoxes;
	protected JButton addBtn;
	protected Socket server;
	protected ObjectInputStream fromServer;
	protected ObjectOutputStream toServer;
	
	public ExprTrackerGUI(String[] categories, String[] tags) {
		frame = new JFrame("Expiration Date Tracker Demo");
		
		panel = new JPanel();
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		notifyLbl = new JLabel("");
		notifyLbl.setForeground(Color.BLUE);
		panel.add(notifyLbl);
		JLabel searchLbl = new JLabel("Enter the product name or SKU here. Must be exact.");
		panel.add(searchLbl);
		
		JPanel searchPnl = new JPanel(new FlowLayout());
		searchJTF = new JTextField(20);
		
		searchBtn = new JButton("Search");
		searchBtn.setActionCommand("Search");
		searchBtn.addActionListener(new ButtonClickListener());
		
		
		searchPnl.add(searchJTF);
		searchPnl.add(searchBtn);
		
		panel.add(searchPnl);
		
		JLabel categoryLbl = new JLabel("You can select a single category to show applicable items.");
		panel.add(categoryLbl);
		
		cateBoxes = new ButtonGroup();
		
		cateBtns = new JRadioButton[categories.length];
		
		for (int i = 0; i < categories.length; i++) {
			cateBtns[i] = new JRadioButton(categories[i]);
			cateBtns[i].setActionCommand(categories[i]);
			cateBoxes.add(cateBtns[i]);
			panel.add(cateBtns[i]);
		}
		
		
		JLabel tagLbl = new JLabel("Select as many tags as you like to limit search results");
		panel.add(tagLbl);
		
		tagBoxes = new JCheckBox[tags.length];
		
		for (int i = 0; i < tags.length; i++) {
			tagBoxes[i] = new JCheckBox();
			tagBoxes[i].setActionCommand(tags[i]);
			tagBoxes[i].setText(tags[i]);
			panel.add(tagBoxes[i]);
		}
		
		JLabel resultsLbl = new JLabel("Search Results will appear below.\nSelect a result to add it to the schedule.");
		
		panel.add(resultsLbl);
		
		resultsPnl = new JPanel(new FlowLayout());
		panel.add(resultsPnl);
		
		JPanel btnPanel = new JPanel(new FlowLayout());
		
		addBtn = new JButton("Add");
		addBtn.setActionCommand("Add");
		addBtn.addActionListener(new ButtonClickListener());
		
		JButton exprBtn = new JButton("Check Expiring Items");
		exprBtn.setActionCommand("Remove");
		exprBtn.addActionListener(new ButtonClickListener());
		
		btnPanel.add(addBtn);
		btnPanel.add(exprBtn);
		
		panel.add(btnPanel);
		
		panel.setSize(450, 450);
		
		frame.add(panel);
		frame.setSize(450, 450);
		
		frame.show();
		
		try {
			server = new Socket("localhost", 8004);
			toServer = new ObjectOutputStream(server.getOutputStream());
			fromServer = new ObjectInputStream(server.getInputStream());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String[] cate = new String[] {"roastedcoffee"};
		String[] tags = new String[] {"Dark Roast", "Medium Roast", "Blonde Roast", "Single Origin", "Blend"};
		ExprTrackerGUI view = new ExprTrackerGUI(cate, tags);
		
	}
	
	private class ButtonClickListener implements ActionListener, ExprTrackerConstants {
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			
			if (command.equals("Search")) {
				try {
					toServer.writeInt(QUERY);
					toServer.writeObject(searchJTF.getText());
					ButtonModel categoriesSel = cateBoxes.getSelection();
					
					if (categoriesSel == null) {
						toServer.writeObject("");
					} else {
						toServer.writeObject(categoriesSel.getActionCommand());
					}
					LinkedList<String> checked = new LinkedList<>();
					for (int i = 0; i < tagBoxes.length; i++) {
						if (tagBoxes[i].isSelected()) {
							checked.add(tagBoxes[i].getActionCommand());
						}
					}
					toServer.writeInt(checked.size());
					for (String name : checked) {
						toServer.writeObject(name);
					}
					int results = fromServer.readInt(); // get the number of results
					String[] resultNames = new String[results];
					
					for (int i = 0; i < results; i++) {
						resultNames[i] = (String) fromServer.readObject();
					}
					
					resultsPnl.removeAll();
					resultBoxes = new JCheckBox[results];
					
					for (int i = 0; i < resultBoxes.length; i++) {
						resultBoxes[i] = new JCheckBox();
						resultBoxes[i].setText(resultNames[i]);
						resultBoxes[i].setActionCommand(resultNames[i]);
						resultsPnl.add(resultBoxes[i]);
					}
					
					
					
				} catch (IOException ex) {
					ex.printStackTrace();
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				notifyLbl.setText("Database searched. Results displayed");
			} else if (command.equals("Add")) {
				LinkedList<Item> toAdd = new LinkedList<>();
				if (resultBoxes == null) {
					return;
				}
				
				for (int i = 0; i < resultBoxes.length; i++) {
					if (resultBoxes[i].isSelected()) {
						toAdd.add(new Item(resultBoxes[i].getActionCommand()));
					}
				}
				
				try {
					for (Item itm : toAdd) {
						toServer.writeInt(ADD_ITEM);
						toServer.writeObject(itm);
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				
				notifyLbl.setText("Items added to the expiration tracker.");
				
			} else if (command.equals("Remove")) {
				try {
					toServer.writeInt(REMOVE);
					
					int removedNum = fromServer.readInt(); // get the number of results
					
					String removedNames = "Items Expiring this Month:\n";
					
					for (int i = 0; i < removedNum; i++) {
						removedNames = removedNames + "\n" + (String) fromServer.readObject();
					}
					
					notifyLbl.setText(removedNames);
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		}
	}
}
