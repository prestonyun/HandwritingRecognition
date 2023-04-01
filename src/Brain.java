import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

public class Brain {

	private JFrame handwritingFrame;
	private JFrame labelMemoryFrame;
	private DataProcessor drawArea;
	private JPanel controls;
	private JPanel statistics;
	private JPopupMenu pop;
	private static JTextField inputField;
	private JButton acceptInputButton;
	private JButton clearButton;
	private JButton recallButton;
	private JButton storeButton;
	private JTextField tfLine;
	private Circuit circuit;

	public Brain() {
		// Create frames
		handwritingFrame = new JFrame("Handwriting Recognition Unit");
		labelMemoryFrame = new JFrame("Label Memory Unit");

		// Create components
		drawArea = new DataProcessor();
		controls = new JPanel();
		statistics = new JPanel();
		pop = new JPopupMenu();
		inputField = new JTextField();
		acceptInputButton = new JButton("Store");
		clearButton = new JButton("Clear");
		recallButton = new JButton("Recall");
		storeButton = new JButton("Store");
		tfLine = new JTextField();

		// Set layout of main frame
		Container content = handwritingFrame.getContentPane();
		content.setLayout(new BorderLayout());
		content.add(drawArea, BorderLayout.CENTER);
		content.add(controls, BorderLayout.SOUTH);

		// Set layout of label memory frame
		Container popout = labelMemoryFrame.getContentPane();
		popout.setLayout(new BorderLayout());
		popout.add(acceptInputButton, BorderLayout.WEST);
		popout.add(inputField, BorderLayout.EAST);

		// Add listeners to buttons
		clearButton.addActionListener(e -> drawArea.clear());
		recallButton.addActionListener(e -> {
			Circuit probe = new Circuit(drawArea.getDescriptors());
			System.out.println(circuit.recall(probe));
		});
		storeButton.addActionListener(e -> {
			labelMemoryFrame.setVisible(true);
			pop.show(labelMemoryFrame, 50, 50);
		});
		acceptInputButton.addActionListener(e -> {
			circuit = new Circuit(drawArea.getDescriptors(), inputField.getText());
			labelMemoryFrame.dispose();
			labelMemoryFrame.setVisible(false);
			drawArea.clear();
		});

		// Add components to controls panel
		controls.add(clearButton);
		controls.add(recallButton);
		controls.add(storeButton);

		// Add components to pop-up menu
		pop.add(acceptInputButton);
		pop.add(inputField);

		// Add text field to statistics panel
		statistics.add(tfLine);

		// Set visibility and size of frames
		labelMemoryFrame.setVisible(false);
		handwritingFrame.setSize(500, 500);
		handwritingFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		handwritingFrame.setVisible(true);

		System.out.println("width: " + drawArea.getWidth());
		System.out.println("height: " + drawArea.getHeight());
	}

	public static void main(String[] args) {
		new Brain();
	}
}
