import java.awt.*;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

public class SpendingMonitorAlpha implements ActionListener  {

	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//                                              Global Files                                               //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//Core graphical elements
	JFrame window = new JFrame();

	Container mainCont;

	JPanel logoDateP = new JPanel();
	JPanel buttonsP = new JPanel();
	JPanel currentSessionP = new JPanel();
	JPanel archiveP = new JPanel();

	//Other graphical components 

	//logo
	ImageIcon logoimage = new ImageIcon("logo.png");
	JLabel labelimage = new JLabel("",logoimage,JLabel.CENTER);

	//buttons
	JButton startSessionB = new JButton("Start new period");
	JButton continueSessionB = new JButton("Continue exesting session");
	JButton addToTotalB = new JButton("Add");
	JButton clearB = new JButton("Clear");

	//current session elements
	JLabel spendamountL = new JLabel();
	JLabel sinceDateL = new JLabel();
	JFormattedTextField userinput = new JFormattedTextField();

	// Functional objects and variables

	File sessionData = new File("sData.bin");
	final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	Date date = new Date();
	double amount;
	String finDate;


	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 									          Graphics and UI                                              //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void display() {

		/*This method will call all other methods
		 * responsible for graphic
		 */

		//Adding content to container
		ccontainerr();

		//filling all panels with content
		logodpanel();
		buttonpanel();
		csessionpanel();

		//adding panels to container
		addPanels();

		//Setting size and frame settings
		framesettings();

	}

	public void framesettings() {
		/* This method is responsible for frame settings such as size, locations 
		 * and is actually showing the frame
		 */
		window.setTitle("Spending Monitor");
		window.setSize(200,400);
		window.setLocation(400, 200);
		window.setResizable(false);
		window.pack();
		window.setVisible(true);
	}

	public void ccontainerr() {

		/*This method adds container
		 * To the main JFrame
		 * and set the outer layout
		 * (boxlayout Y - axis)
		 */
		mainCont = window.getContentPane();
		BoxLayout mainlayout = new BoxLayout(mainCont, BoxLayout.Y_AXIS);
		mainCont.setLayout(mainlayout);		
	}

	public void addPanels() {

		//now we will add all panels to container
		mainCont.add(logoDateP);
		mainCont.add(buttonsP);
		mainCont.add(currentSessionP);
		mainCont.add(archiveP);

	}
	public void logodpanel() {

		/*All graphical settings for first panel with 
		 * logo and date
		 */
		logoDateP.add(labelimage);

		//Setting up date
		//final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

		//Displaying date
		JLabel datel = new JLabel(dateFormat.format(date));
		datel.setBounds(50, 50, 75, 75);
		logoDateP.add(datel);

	}
	public void buttonpanel() {
		//Creating layout
		//BoxLayout button_layout = new BoxLayout(buttonsP, BoxLayout.Y_AXIS);
		buttonsP.setLayout(new GridLayout(2,1));

		//Registering actionlistener for buttons
		startSessionB.addActionListener(this);
		continueSessionB.addActionListener(this);

		//adding buttons to panel
		buttonsP.add(startSessionB);
		buttonsP.add(continueSessionB);
	}
	public void csessionpanel() {
		//Creating Layout
		GridLayout csLayout = new GridLayout(4,2,1,1);
		currentSessionP.setLayout(csLayout);

		//creating border and adding it to current session panel
		Border buttonborder = BorderFactory.createTitledBorder("Current Period");
		currentSessionP.setBorder(buttonborder);

		//Creating some elements that don't have to be accessible at class level
		JLabel spendL = new JLabel("You have spend");
		JLabel sinceL = new JLabel("Since");
		JLabel addL = new JLabel("Add: ");

		//Registering actionlistener for buttons
		addToTotalB.addActionListener(this);
		clearB.addActionListener(this);
		
		//Putting elements on panel
		currentSessionP.add(spendL);
		currentSessionP.add(spendamountL);
		currentSessionP.add(sinceL);
		currentSessionP.add(sinceDateL);
		currentSessionP.add(addL);
		currentSessionP.add(userinput);
		currentSessionP.add(addToTotalB);
		currentSessionP.add(clearB);

		//hiding panel on start screen
		currentSessionP.setVisible(false);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// 									This is where functionality starts                                     //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////



	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == startSessionB) {

			newsession();

		}
		else if(e.getSource() == continueSessionB) {

			continuesession();

		}
		else if(e.getSource() == addToTotalB) {
			addSpending();
		}
		else if(e.getSource() == clearB) {
			userinput.setText("0.00");
		}
	}
	public void newsession() {

		//check whether there are files from previous session or not.

		//If file exists do this
		if (checkforfile()) {
			int isSure = JOptionPane.showConfirmDialog(null, "There is existing data from previous session. "
					+ "Starting new session will erase this data. This process is irreversible."
					+ "Are you sure you want to continue?", "Caution", JOptionPane.YES_NO_OPTION);

			//if user say yes and want to proceed
			if (isSure == JOptionPane.YES_OPTION) {
				deletedata();
				startNew();
			}
		}
		//If file not found do this
		else if (!checkforfile()) {
			startNew();
		}
	}
	public void startNew() {
		//Create file and fill with starting data
		createnewfile();
		setanddisplay();
	}
	public void continuesession() {
		//check whether there are files from previous session or not.

		//do this if file exist
		if (checkforfile()) {

			//read file and display in a panel
			setanddisplay();
		}
		//do this if file doesn't exist
		else if (!checkforfile()) {
			int whatToDo = JOptionPane.showConfirmDialog(null, "Data from previous session is not found"
					+ "Would you like to start a new session?", "Caution", JOptionPane.YES_NO_OPTION);
			if (whatToDo == JOptionPane.YES_OPTION) {
				startNew();
			}		
		}
	}
	public void createnewfile () {

		//create file itself and fill it with data
		try{
			final Formatter filecreation = new Formatter("sData.bin");
			filecreation.close();

			DataOutputStream datafill = new DataOutputStream(new FileOutputStream("sData.bin"));
			datafill.writeChars(dateFormat.format(date));
			datafill.writeDouble(0.00);
			datafill.close();
			//Debug
			System.out.println("File created");

		}
		catch(Exception a) {
			JOptionPane.showMessageDialog(null, "File has not been created","Error", JOptionPane.WARNING_MESSAGE);
		}

	}
	public boolean checkforfile() {

		if (sessionData.exists()) {
			return true;
		}
		else {
			return false;
		}
	}
	public void deletedata() {
		try {
			sessionData.delete();
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Cannot Delete file. Please look for sData.bin file in program "
					+ "folder and delete it manually","Error", JOptionPane.WARNING_MESSAGE);
		}
	}
	public void setanddisplay() {

		boolean canDisplay = false;
		String finDate = "";

		//read date and amount
		try{
			DataInputStream dataread = new DataInputStream(new FileInputStream("sData.bin"));

			for (int c = 0; c < 10; c++) {
				finDate += dataread.readChar();
			}
			amount = dataread.readDouble();
			dataread.close();
			//if everything worked we can display it
			canDisplay = true;
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(null, "data is corrupted or missing!",
					"Error", JOptionPane.WARNING_MESSAGE);
		}
		if (canDisplay) {
			sinceDateL.setText(finDate);
			spendamountL.setText(Double.toString(amount));
			userinput.setText("0.00");
			currentSessionP.setVisible(true);
			window.pack();
		}
	}	
	public void addSpending() {

		String userSpending = userinput.getText();

		//if user input proper data
		if (isCurrency(userSpending)) {
			overwriteandrefresh (userSpending);
		}

		//if user input invalid data
		if (!isCurrency(userSpending)) {
			userinput.setText("0.00");
		}

	}
	public boolean isCurrency(String inpu) {

		try {
			double cash = Double.parseDouble(inpu);

			//if is less that one cent
			if (cash < 0.01) {
				return false;
			}
			else {
				return true;
			}
		}
		catch (NumberFormatException nc) {
			return false;
		}
	}
	public void overwriteandrefresh (String spending) {
		try {
			//Convert string that user entered to double
			double newAmount = Double.parseDouble(spending);
			newAmount += amount;
			// create a new RandomAccessFile with filename test
			RandomAccessFile sDataRAF = new RandomAccessFile("sData.bin", "rws");
			
			//overwrite amount
			sDataRAF.seek(20);
			sDataRAF.writeDouble(newAmount);
			
			//read new amount
			sDataRAF.seek(20);
			amount = sDataRAF.readDouble();
			
			//debug
			System.out.println(amount);
			
			//refresh
			spendamountL.setText(Double.toString(amount));
			sDataRAF.close();
			//optional set input textfield to default after adding value
			//userinput.setText("0.00");
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Could not save new amount",
					"Error", JOptionPane.WARNING_MESSAGE);
		}
	}
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	//											Starting program                                             //
	//////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) {
		//executing program
		SpendingMonitorAlpha application = new SpendingMonitorAlpha();
		application.display();
	}
}
