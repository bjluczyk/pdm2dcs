package pdm2dcs;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

/* The PDM2DCS java tool puts a very simple gui front-end on a command-line
 * converter tool originally created under a cygwin environment.  The tool
 * takes as input a waveform output file captured by the ESP waveform collector
 * and converts it to a series of DCS files compatible with the pcDCEdit tool.
 * 
 * The general usage of the java tool is as follows:
 * 
 * 1. Run the pdm2dcs.jar file.
 * 2. Select the "Browse..." button to display a file chooser.
 * 3. Select the waveform output file that you wish to convert.
 * 4. Optionally type in an output file prefix.  If an output file prefix is
 *    not specified, then the prefix of the input file is used.
 * 5. Select either individual or continuous for the output.
 *    a. Choose individual if the waveform output file contains a number of NIBP
 *       measurements.  This will create one DCS file for each measurement.
 *    b. Choose continuous if all data from the waveform output file should be
 *       stored in a single DCS file.  This is generally used when performing
 *       static and dynamic calibration of the invasive pressure line.
 * 6. Select the "Convert" button to run the conversion.  The DCS ouput files
 *    will be created in the same directory as the waveform output file selected
 *    for conversion.  Output text from the conversion is displayed within the
 *    scrolling text area at the bottom of the window.
 *    
 * Note that the command-line pdm2dcs.exe tool must be in your PATH.
 */

public class PDM2DCS extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField inputFileTextField;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private JTextField outputPrefixTextField;
	private String inputFilePath;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PDM2DCS frame = new PDM2DCS();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public PDM2DCS() {
		setTitle("PDM2DCS Converter");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 435, 465);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		// Create a label for the input file text field
		JLabel inputFileLabel = new JLabel("Input File:");
		inputFileLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
		inputFileLabel.setBounds(12, 10, 120, 25);
		contentPane.add(inputFileLabel);
		
		// Create the input file text field
		inputFileTextField = new JTextField();
		inputFileTextField.setFont(new Font("Tahoma", Font.BOLD, 14));
		inputFileTextField.setBounds(12, 48, 280, 30);
		contentPane.add(inputFileTextField);
		inputFileTextField.setColumns(10);
		
		// Create a Browse... button to display a file chooser
		JButton browseButton = new JButton("Browse...");
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Display a file chooser dialog
				JFileChooser fileChooser = new JFileChooser();
				int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION)
				{
					// Get the selected file.  Set the text field contents
					// with the file name and save off the path.
					File inputFile = fileChooser.getSelectedFile();
					inputFileTextField.setText(inputFile.getName());
					inputFilePath = inputFile.getAbsolutePath();
				}
			}
		});
		browseButton.setFont(new Font("Tahoma", Font.BOLD, 14));
		browseButton.setBounds(304, 48, 100, 30);
		contentPane.add(browseButton);
		
		// Create radio buttons to control the conversion mode
		JRadioButton rdbtnIndividual = new JRadioButton("Individual");
		rdbtnIndividual.setFont(new Font("Tahoma", Font.PLAIN, 14));
		buttonGroup.add(rdbtnIndividual);
		rdbtnIndividual.setBounds(12, 100, 127, 30);
		rdbtnIndividual.setSelected(true);
		contentPane.add(rdbtnIndividual);
		
		JRadioButton rdbtnContinuous = new JRadioButton("Continuous");
		rdbtnContinuous.setFont(new Font("Tahoma", Font.PLAIN, 14));
		buttonGroup.add(rdbtnContinuous);
		rdbtnContinuous.setBounds(12, 135, 127, 30);
		contentPane.add(rdbtnContinuous);
		
		// Create a label for the output prefix text field
		JLabel outputPrefixLabel = new JLabel("Output Prefix:");
		outputPrefixLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
		outputPrefixLabel.setBounds(12, 180, 120, 30);
		contentPane.add(outputPrefixLabel);
		
		// Create the output prefix text field
		outputPrefixTextField = new JTextField();
		outputPrefixTextField.setFont(new Font("Tahoma", Font.BOLD, 14));
		outputPrefixTextField.setBounds(144, 180, 260, 30);
		contentPane.add(outputPrefixTextField);
		outputPrefixTextField.setColumns(10);
		
		// Create a scrolling text area to hold the conversion output
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 309, 392, 98);
		contentPane.add(scrollPane);
		
		JTextArea textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		
		// Create a button to run the conversion.
		// This is where the magic happens.
		JButton convertButton = new JButton("Convert");
		convertButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (inputFileTextField.getText() != null)
				{
					// Convert the input file to a set of DCS files
					File inputFile = new File(inputFilePath);
					convert(inputFile);
				}
			}
			
			// The 'convert' method converts an input file saved by the ESP waveform
			// collector to a series of DCS files compatible with the DCEdit tool.
			public void convert(File inputFile)
			{
				// Build up the command string for bash to execute
				String cmdString = new String();
				cmdString = "\"pdm2dcs";	// The executable to invoke - must be in your path
				
				// Add the input filename
				cmdString += " -i " + inputFile.getName();
				// Add output prefix if the text field is not empty
				if (outputPrefixTextField.getText().length() != 0)
				{
					cmdString += " -o " + outputPrefixTextField.getText();
				}
				// Enable continuous mode if selected
				if (rdbtnContinuous.isSelected())
				{
					cmdString += " -c";
				}
				cmdString += "\"";		// Ending quote
				System.out.println(cmdString);
				
				// Now put together the full command array.  We want to fire off
				// a Windows command window that will invoke a cygwin bash shell
				// and run the conversion tool with the appropriate arguments.
				String commandArray[] = { "cmd", "/c", "bash", "-c", cmdString };
				
				// Run the conversion
				try {
					// Create a process to run the conversion
					ProcessBuilder builder = new ProcessBuilder(commandArray);
					
					// Switch to the directory containing the input file
					builder.directory(new File(inputFile.getParent()));
					
					// Run the conversion
					Process p = builder.start();
					
					// Read the process output and print it to the text area
					Scanner scanner = new Scanner(p.getInputStream());
					scanner.useDelimiter("\r\n");
					while (scanner.hasNext())
					{
						textArea.append(scanner.next());
					}
					scanner.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// Wait a second for the process to run
				try {
					Thread.sleep(1*1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
				}
				
				System.out.println("Converted input file!!");
			}
		});
		convertButton.setFont(new Font("Tahoma", Font.BOLD, 14));
		convertButton.setBounds(154, 250, 100, 30);
		contentPane.add(convertButton);
	}
}
