import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

public class brain {
	
	JFrame jframe = new JFrame("Handwriting Recognition Unit");
	JFrame popup = new JFrame("Label Memory Unit");
	
	dataProcessor drawArea = new dataProcessor();
	
	JPanel controls = new JPanel();
	JPanel statistics = new JPanel();
	
	JPopupMenu pop = new JPopupMenu();
	static JTextField input = new JTextField();
	
	JButton acceptInput = new JButton("Store");
	JButton clearBtn = new JButton("Clear");
	JButton arrayBtn = new JButton("Recall");
	JButton storeBtn = new JButton("Store");
	
	TextField tfLine = new TextField();
	
	circuit m;
	
	
	public static void main(String[] args) {
		
		brain brain = new brain();
		brain.display();
		
	}
	
	public void display()
	{
		Container content = jframe.getContentPane();
		Container popout = popup.getContentPane();
		
		content.setLayout(new BorderLayout());
		content.add(drawArea, BorderLayout.CENTER);
		content.add(controls, BorderLayout.SOUTH);
		popout.add(acceptInput, BorderLayout.WEST);
		popout.add(input, BorderLayout.EAST);
		
		
		clearBtn.addActionListener(actionListener);
		arrayBtn.addActionListener(actionListener);
		storeBtn.addActionListener(actionListener);
		acceptInput.addActionListener(actionListener);
		
		
		controls.add(clearBtn);
		controls.add(arrayBtn);
		controls.add(storeBtn);
		pop.add(acceptInput);
		pop.add(input);
		
		statistics.add(tfLine);
		
		popup.dispatchEvent(new WindowEvent(popup, WindowEvent.WINDOW_CLOSING));
		
		jframe.setSize(500, 500);
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setVisible(true);
		
		System.out.println("width: " + drawArea.getWidth());
		System.out.println("height: " + drawArea.getHeight());
	}
	
	ActionListener actionListener = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() == clearBtn)
				drawArea.clear();
			else if (e.getSource() == arrayBtn)
			{
				circuit probe = new circuit(drawArea.getDescriptors());
				System.out.println(m.recall(probe));
			}
			else if (e.getSource() == storeBtn)
			{
				popup.setVisible(true);
				pop.show(popup, 50, 50);

			}
			if (e.getSource() == acceptInput)
			{
				m = new circuit(drawArea.getDescriptors(), input.getText());
				popup.dispose();
				popup.setVisible(false);
				drawArea.clear();
			}
		}
	};
	
};
