import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL; // Add this line for URL class


class OnlineTest extends JFrame implements ActionListener {
	JTextArea questionArea;
	JButton[] options = new JButton[4];
	JButton nextButton;
	JLabel timerLabel, scoreLabel, turnLabel;
	int player1Score = 0, player2Score = 0, current = 0;
	Timer timer;
	int timeLeft = 60; // Time in sec
	int selectedOptionIndex = -1; // To track the selected option
	String player1Name, player2Name;
	boolean isPlayer1Turn = true; // Track whose turn it is

	String[][] questions = {
			{"Which of the following is a valid identifier in Java?", "1var", "var_1", "1stVar", "var-1", "1"},
			{"What is the default value of a boolean variable in Java?", "true", "false", "0", "null", "1"},
			{"Which keyword is used to define a constant in Java?", "constant", "final", "static", "immutable", "1"},
			{"What is the size of an int variable in Java?", "16 bit", "32 bit", "64 bit", "8 bit", "1"},
			{"Which of these is not a primitive data type in Java?", "int", "float", "String", "char", "2"},
			{"Which method is used to start a thread in Java?", "start()", "run()", "execute()", "init()", "0"},
			{"What is the maximum value of an int in Java?", "2^31 - 1", "2^32 - 1", "2^15 - 1", "2^63 - 1", "0"},
			{"Which exception is thrown when dividing by zero?", "ArithmeticException", "NullPointerException", "IllegalArgumentException", "IndexOutOfBoundsException", "0"},
			{"Which collection class allows duplicates?", "Set", "List", "Map", "Queue", "1"},
			{"What is the purpose of the 'static' keyword?", "To declare a constant", "To allocate memory", "To denote a method belongs to the class", "To denote a method belongs to the object", "2"},
			{"Which access modifier allows access only within the same package?", "private", "protected", "public", "default", "3"},
			{"What does JVM stand for?", "Java Virtual Machine", "Java Variable Machine", "Java Visual Machine", "Java Version Machine", "0"},
			{"Which keyword is used to inherit a class in Java?", "extends", "implements", "inherits", "super", "0"},
			{"What is the main method signature in Java?", "public static void main(String args[])", "public void main(String[] args)", "public static void main(String args)", "public void main(String args[])", "0"},
			{"What is the output of 10 + 20 + '30' in Java?", "1030", "3030", "1020", "Error", "0"}
	};

	OnlineTest(String title) {
		super(title);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(630, 500);
		setLocation(250, 100);
		setLayout(new CardLayout()); // Use CardLayout for easy switching between screens
		getContentPane().setBackground(new Color(230, 230, 250)); // Light lavender background
		setResizable(false);

		showStartPage(); // Show the start page to input player names

		setVisible(true);
	}

	private void showStartPage() {
		JPanel startPanel = new JPanel();
		startPanel.setLayout(new GridBagLayout());
		startPanel.setBackground(new Color(230, 230, 250));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);

		// Create a title label
		JLabel titleLabel = new JLabel("Welcome to the Multiplayer Quiz!", SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
		titleLabel.setForeground(new Color(70, 130, 180));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		startPanel.add(titleLabel, gbc);

		// Player 1 Name Input with placeholder
		JTextField player1Field = createPlaceholderField("Enter Player 1 Name");
		gbc.gridwidth = 1;
		gbc.gridy = 1;
		startPanel.add(player1Field, gbc);

		// Player 2 Name Input with placeholder
		JTextField player2Field = createPlaceholderField("Enter Player 2 Name");
		gbc.gridx = 1;
		startPanel.add(player2Field, gbc);

		// Start Quiz Button
		JButton startButton = new JButton("Start Quiz");
		startButton.setFont(new Font("Arial", Font.BOLD, 16));
		startButton.setBackground(new Color(100, 149, 237));
		startButton.setForeground(Color.WHITE);
		startButton.setOpaque(true);
		startButton.setBorderPainted(false);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		startPanel.add(startButton, gbc);

		// Action listener for the start button
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				player1Name = player1Field.getText().trim();
				player2Name = player2Field.getText().trim();

				// Check if player names are empty
				if (player1Name.isEmpty() || player2Name.isEmpty()) {
					JOptionPane.showMessageDialog(OnlineTest.this, "Please enter names for both players.", "Input Error", JOptionPane.ERROR_MESSAGE);
					return; // Prevent moving to the quiz initialization
				}

				remove(startPanel);
				initializeQuiz(); // Proceed to quiz initialization
				revalidate();
				repaint();
			}
		});


		add(startPanel);
	}

	// Method to create a JTextField with placeholder functionality
	private JTextField createPlaceholderField(String placeholder) {
		JTextField textField = new JTextField(placeholder);
		textField.setForeground(Color.GRAY); // Placeholder color

		// Add focus listener to manage placeholder text
		textField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (textField.getText().equals(placeholder)) {
					textField.setText("");
					textField.setForeground(Color.BLACK); // Normal text color
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (textField.getText().isEmpty()) {
					textField.setForeground(Color.GRAY);
					textField.setText(placeholder); // Restore placeholder
				}
			}
		});

		return textField;
	}

	private void initializeQuiz() {
		setLayout(null); // Use absolute positioning

		// Score Label
		scoreLabel = new JLabel(player1Name + "'s Score: 0 | " + player2Name + "'s Score: 0");
		scoreLabel.setFont(new Font("Arial", Font.BOLD, 16));
		scoreLabel.setBounds(30, 20, 550, 30);
		add(scoreLabel);

		// Timer Label
		timerLabel = new JLabel("Time: 60 sec");
		timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
		timerLabel.setBounds(470, 20, 150, 30);
		add(timerLabel);

		// Turn Label
		turnLabel = new JLabel(player1Name + "'s Turn");
		turnLabel.setFont(new Font("Arial", Font.BOLD, 16));
		turnLabel.setBounds(30, 50, 250, 30);
		add(turnLabel);

		questionArea = new JTextArea();
		questionArea.setLineWrap(true);
		questionArea.setWrapStyleWord(true);
		questionArea.setEditable(false);
		questionArea.setBackground(new Color(240, 240, 255)); // Slightly different background for questions
		questionArea.setFont(new Font("Arial", Font.PLAIN, 16));
		questionArea.setBounds(30, 80, 540, 60); // Fixed size for the question area
		add(questionArea);

		// Initialize option buttons
		for (int i = 0; i < 4; i++) {
			options[i] = new JButton();
			options[i].setFont(new Font("Arial", Font.PLAIN, 16));
			options[i].setBackground(new Color(100, 149, 237)); // Cornflower blue
			options[i].setForeground(Color.WHITE);
			options[i].setOpaque(true); // To ensure button color shows
			options[i].addActionListener(this);
			options[i].setActionCommand(String.valueOf(i)); // Set index as action command
			options[i].setBounds(50, 150 + (i * 50), 500, 40); // Set button bounds
			add(options[i]);
		}

		nextButton = new JButton("Next");
		nextButton.setFont(new Font("Arial", Font.BOLD, 16));
		nextButton.setBackground(new Color(100, 149, 237)); // Cornflower blue
		nextButton.setForeground(Color.WHITE);
		nextButton.addActionListener(this);
		nextButton.setBounds(250, 400, 100, 30);
		add(nextButton);

		shuffleQuestions();
		setQuestion();
		startTimer();
	}

	private void shuffleQuestions() {
		List<Integer> indices = new ArrayList<>();
		for (int i = 0; i < questions.length; i++) {
			indices.add(i);
		}
		Collections.shuffle(indices);
		String[][] shuffledQuestions = new String[questions.length][];
		for (int i = 0; i < questions.length; i++) {
			shuffledQuestions[i] = questions[indices.get(i)];
		}
		questions = shuffledQuestions;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			JButton clickedButton = (JButton) e.getSource();
			String actionCommand = clickedButton.getActionCommand();

			// Check if a question option button was clicked
			if (actionCommand.matches("\\d")) {
				selectedOptionIndex = Integer.parseInt(actionCommand);
				boolean isCorrect = checkAnswer();

				// Update score based on whose turn it is
				if (isPlayer1Turn) {
					if (isCorrect) {
						player1Score++;
					}
				} else {
					if (isCorrect) {
						player2Score++;
					}
				}

				// Update score label
				scoreLabel.setText(player1Name + "'s Score: " + player1Score + " | " + player2Name + "'s Score: " + player2Score);

				// Set button color based on correctness
				clickedButton.setBackground(isCorrect ? Color.GREEN : Color.RED);
				if (!isCorrect) {
					options[Integer.parseInt(questions[current][5])].setBackground(Color.GREEN); // Highlight the correct answer
				}

				// Disable all options after answering
				for (JButton option : options) {
					option.setEnabled(false);
				}

				// Proceed to next question after a short delay
				Timer delay = new Timer(1000, new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						current++;
						isPlayer1Turn = !isPlayer1Turn; // Switch turns
						turnLabel.setText(isPlayer1Turn ? player1Name + "'s Turn" : player2Name + "'s Turn");
						if (current < questions.length) {
							setQuestion();
							resetTimer();
						} else {
							showResult();
						}
					}
				});
				delay.setRepeats(false);
				delay.start();
			}
		}
	}

	private void startTimer() {
		timeLeft = 60;
		timerLabel.setText("Time: " + timeLeft + " sec");
		timer = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timeLeft--;
				timerLabel.setText("Time: " + timeLeft + " sec");
				if (timeLeft <= 0) {
					timer.stop();
					isPlayer1Turn = !isPlayer1Turn; // Switch turns on timeout
					turnLabel.setText(isPlayer1Turn ? player1Name + "'s Turn" : player2Name + "'s Turn");
					current++; // Move to next question
					if (current < questions.length) {
						setQuestion();
					} else {
						showResult();
					}
				}
			}
		});
		timer.start();
	}

	private void resetTimer() {
		if (timer != null) {
			timer.stop();
		}
		startTimer();
	}

	private void setQuestion() {
		// Reset button colors and enable them
		for (JButton option : options) {
			option.setEnabled(true);
			option.setBackground(new Color(100, 149, 237)); // Reset to original color
		}

		// Get the current question
		String[] currentQuestion = questions[current];
		questionArea.setText(currentQuestion[0]);
		for (int i = 0; i < options.length; i++) {
			options[i].setText(currentQuestion[i + 1]);
		}
		timerLabel.setText("Time: 60 sec");
		timeLeft = 60; // Reset the timer for the new question
	}

	private boolean checkAnswer() {
		return selectedOptionIndex == Integer.parseInt(questions[current][5]);
	}

	private void playSound(String soundFile) {
		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getClass().getResource(soundFile));
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showResult() {
		timer.stop();

		// Determine the winner and the corresponding resources
		String winner;
		String imagePath;
		String soundPath;

		if (player1Score > player2Score) {
			winner = player1Name + " wins!";
			imagePath = "winner.jpg";
			soundPath = "winner.wav";
		} else if (player2Score > player1Score) {
			winner = player2Name + " wins!";
			imagePath = "winner.jpg";
			soundPath = "winner.wav";
		} else {
			winner = "It's a Tie!";
			imagePath = "tie.jpg";
			soundPath = "winner.wav";
		}

		// Use a Timer to delay showing the result dialog
		Timer delay = new Timer(1000, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// Create a panel to display the message
				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				panel.setBackground(Color.WHITE);
				panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

				// Add message label
				JLabel messageLabel = new JLabel("<html><h2 style='color: #4A90E2; text-align: center;'>" + winner + "</h2></html>"
						+ "<html><p style='font-size: 16px; text-align: center;'>" + player1Name + ": " + player1Score + "<br>"
						+ player2Name + ": " + player2Score + "</p></html>");
				messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
				panel.add(messageLabel);

				// Load the image
				URL imageUrl = getClass().getResource(imagePath); // Remove leading slash
				if (imageUrl == null) {
					System.err.println("Image not found: " + imagePath);
					// Optionally set a default icon here
					imageUrl = getClass().getResource("default_image.jpg"); // Use a default image
				}

				ImageIcon icon = new ImageIcon(imageUrl);
				JLabel imageLabel = new JLabel(icon);
				imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
				panel.add(imageLabel);

				playSound(soundPath);
				// Show the message dialog with the image
				JOptionPane.showMessageDialog(OnlineTest.this, panel, "Game Over", JOptionPane.INFORMATION_MESSAGE);
				System.exit(0);
			}
		});
		delay.setRepeats(false);
		delay.start();
	}



	public static void main(String[] args) {
		new OnlineTest("Multiplayer Quiz Game");
	}
}
