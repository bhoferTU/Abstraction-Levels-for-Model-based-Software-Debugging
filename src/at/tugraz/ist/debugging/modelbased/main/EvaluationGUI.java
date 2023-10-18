package at.tugraz.ist.debugging.modelbased.main;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.Format;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import at.tugraz.ist.debugging.modelbased.Diagnosis;
import at.tugraz.ist.debugging.modelbased.EDebuggingAlgorithm;
import at.tugraz.ist.debugging.modelbased.EModelGranularity;
import at.tugraz.ist.debugging.modelbased.ESolver;
import at.tugraz.ist.debugging.modelbased.ESolverAccessOption;
import at.tugraz.ist.debugging.modelbased.ModelBasedResult;
import at.tugraz.ist.debugging.modelbased.SolverConfiguration;
import at.tugraz.ist.debugging.modelbased.SolverConfigurator;
import at.tugraz.ist.debugging.modelbased.Strategy;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategy;
import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategyConfiguration;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetPropertiesException;
import at.tugraz.ist.debugging.spreadsheets.datastructures.Coords;
import at.tugraz.ist.util.RuntimeProcessExecuter;
import at.tugraz.ist.util.debugging.Writer;
import at.tugraz.ist.util.time.TimeSpan;
import at.tugraz.ist.util.time.TimeSpan.Precision;

public class EvaluationGUI extends JDialog {

	private static boolean allowDouble = false;

	private static String endFile = null;// "D:\\Studium\\IJCAI_13\\debugger_afw\\test\\propertyFiles\\AFW_dice_rolling_3Faults_Fault1.properties";

	/**
	 * automatically generated UID
	 */
	private static final long serialVersionUID = 8817135713304148706L;

	private static String startFile = null;// "D:\\Studium\\IJCAI_13\\debugger_afw\\test\\propertyFiles\\AFW_weather_1Faults_Fault1.properties";

	public static boolean checkDiagnosis(List<Diagnosis> computedDiagnoses, List<Coords> realDiagnosis) {
		for(Diagnosis diag: computedDiagnoses){
			boolean contained = true;
			for(Coords coordinate:realDiagnosis){
				if(!diag.contains(coordinate)){
					contained = false;
					break;
				}
			}
			if(contained)
				return true;
		}
		
		return false;
	}

	/**
	 * Launch the application - without parameters
	 */
	public static void main(String[] args) {
		try {
			EvaluationGUI dialog = new EvaluationGUI();
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Determines whether cones should be used in order to reduce the number of
	 * cells which need to be considered
	 */
	private JCheckBox checkBoxUseCones;

	/**
	 * Determines whether algorithms should determine after finding at least
	 * diagnoses with a certain cardinality k *
	 */
	private JCheckBox checkBoxUseEarlyTermination;

	/**
	 * Determines whether string cells are considered
	 */
	private JCheckBox checkBoxUseStrings;
    
    /**
	 * Determines whether string cells are considered
	 */
	private JCheckBox checkBoxVerifySolution;
    
    /**
	 * combo box to select solver
	 */
	private JComboBox<ESolver> comboBoxSolver;
    
    /**
	 * combo box to select algorithm
	 */
	private JComboBox<EDebuggingAlgorithm> comboBoxAlgorithm;
    
    /**
	 * combo box to select solver access option
	 */
	private JComboBox<ESolverAccessOption> comboBoxOption;

    /**
	 * combo box to select model granularity
	 */
	private JComboBox<EModelGranularity> comboBoxGranularity;
    
	/**
	 * content panel of gui
	 */
	private final JPanel contentPanel = new JPanel();

	/**
	 * used files chooser to define config file
	 */
	private JFileChooser fc;

	/**
	 * Allows the user to enter the amount of runs
	 */
	JFormattedTextField formattedTextFieldRuns;
	JFormattedTextField formattedTextFieldTime;
	/**
	 * text field for file name of config file
	 */
	private JTextField textField;
    
	/**
	 * Create the dialog.
	 */
	public EvaluationGUI() {
        fc = new JFileChooser();
		fc.setDialogTitle("choose config file");
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setCurrentDirectory(new File("."));
		setTitle("Excel-Debugger Evaluation");
		setAlwaysOnTop(true);
		setBounds(100, 100, 450, 330);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(null);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		textField = new JTextField();
		textField.setBounds(101, 11, 282, 20);
		textField.setText("test\\propertyFiles");
		contentPanel.add(textField);
		textField.setColumns(10);

		JLabel lblNewLabel = new JLabel("Property-Files:");
		lblNewLabel.setBounds(10, 14, 81, 14);
		contentPanel.add(lblNewLabel);

		JButton button = new JButton("...");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				int result = fc.showOpenDialog(contentPanel);
				if (result == JFileChooser.APPROVE_OPTION) {
					String file = fc.getSelectedFile().getPath();
					textField.setText(file);
				}
			}
		});
		button.setBounds(393, 10, 31, 23);
		contentPanel.add(button);

        comboBoxAlgorithm = new JComboBox<>();
        comboBoxAlgorithm.setBounds(101, 79, 282, 20);
        contentPanel.add(comboBoxAlgorithm);
        
        comboBoxOption = new JComboBox<>();
        comboBoxOption.setBounds(101, 112, 282, 20);
        contentPanel.add(comboBoxOption);
        
        comboBoxGranularity = new JComboBox<>();
        comboBoxGranularity.setBounds(101, 227, 282, 20);
        contentPanel.add(comboBoxGranularity);
        
        SolverConfigurator.initializeStrategies();
        
        comboBoxSolver = new JComboBox<>();
        comboBoxSolver.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                comboBoxAlgorithm.removeAllItems();
                comboBoxOption.removeAllItems();
                comboBoxGranularity.removeAllItems();
                ESolver solver = (ESolver)comboBoxSolver.getSelectedItem();
                for(SolverConfiguration conf : SolverConfigurator.strategies)
                {
                    if(conf.getSolver() == solver)
                    {
                        for(EDebuggingAlgorithm algorithm : conf.getAlgorithms())
                            comboBoxAlgorithm.addItem(algorithm);
                        for(ESolverAccessOption option : conf.getOptions())
                            comboBoxOption.addItem(option);
                        for(EModelGranularity granularity : conf.getGranulatities())
                            comboBoxGranularity.addItem(granularity);
                    }
                }
            }
        });
        for(SolverConfiguration config : SolverConfigurator.strategies)
        {
            comboBoxSolver.addItem(config.getSolver());
        }
        comboBoxSolver.setEditable(false);
        comboBoxSolver.setBounds(101, 46, 282, 20);
        contentPanel.add(comboBoxSolver);

		JLabel lblSolver = new JLabel("Solver:");
		lblSolver.setBounds(10, 49, 81, 14);
		contentPanel.add(lblSolver);
        
        JLabel lblAlgorithm = new JLabel("Algorithm:");
		lblAlgorithm.setBounds(10, 82, 81, 14);
		contentPanel.add(lblAlgorithm);

        JLabel lblOption = new JLabel("Option:");
		lblOption.setBounds(10, 115, 81, 14);
		contentPanel.add(lblOption);

		checkBoxUseCones = new JCheckBox("Use cones");
		checkBoxUseCones.setBounds(101, 148, 133, 14);
		contentPanel.add(checkBoxUseCones);

		checkBoxUseStrings = new JCheckBox("Use no strings");
		checkBoxUseStrings.setBounds(101, 173, 133, 14);
		checkBoxUseStrings.setSelected(false);
		contentPanel.add(checkBoxUseStrings);

		checkBoxUseEarlyTermination = new JCheckBox("Early termination");
		checkBoxUseEarlyTermination.setBounds(101, 198, 133, 14);
		checkBoxUseEarlyTermination.setSelected(true);
		contentPanel.add(checkBoxUseEarlyTermination);

		// only allow integers
		NumberFormat f = NumberFormat.getNumberInstance();
		f.setMaximumIntegerDigits(3);
		f.setParseIntegerOnly(true);

		formattedTextFieldRuns = new JFormattedTextField(f);
		formattedTextFieldRuns.setBounds(322, 145, 61, 20);
		formattedTextFieldRuns.setText("1");
		contentPanel.add(formattedTextFieldRuns);

		JLabel lblRuns = new JLabel("Runs");
		lblRuns.setBounds(260, 148, 46, 14);
		contentPanel.add(lblRuns);

		JLabel lblMaxtime = new JLabel("maxTime:");
		lblMaxtime.setBounds(260, 173, 61, 14);
		contentPanel.add(lblMaxtime);

		formattedTextFieldTime = new JFormattedTextField((Format) null);
		formattedTextFieldTime.setText("300");
		formattedTextFieldTime.setBounds(322, 170, 46, 20);
		contentPanel.add(formattedTextFieldTime);

		JLabel lblS = new JLabel("s");
		lblS.setBounds(371, 173, 12, 14);
		contentPanel.add(lblS);
        
        checkBoxVerifySolution = new JCheckBox("Verify Solution");
		checkBoxVerifySolution.setBounds(255, 198, 133, 14);
		checkBoxVerifySolution.setSelected(false);
		contentPanel.add(checkBoxVerifySolution);
        
        JLabel lblGranularity = new JLabel("Granularity:");
		lblGranularity.setBounds(10, 230, 81, 14);
		contentPanel.add(lblGranularity);
        
        JButton btnStart = new JButton("Start");
		btnStart.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				execute(textField.getText());
			}
		});
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		btnStart.setBounds(333, 260, 89, 23);
		contentPanel.add(btnStart);
	}

	@SuppressWarnings("deprecation")
	private void execute(String propertyDirectory) {

        ESolver solver = (ESolver) comboBoxSolver.getSelectedItem();
        EDebuggingAlgorithm algorithm = (EDebuggingAlgorithm) comboBoxAlgorithm.
                getSelectedItem();
        ESolverAccessOption option = (ESolverAccessOption) comboBoxOption.
                getSelectedItem();
        EModelGranularity granularity = (EModelGranularity) comboBoxGranularity.
                getSelectedItem();
        
        Strategy strategy = new Strategy(solver, algorithm, option, granularity);
        
        int runs = Integer.parseInt(formattedTextFieldRuns.getText());
		int maxMins = Integer.parseInt(formattedTextFieldTime.getText());
        boolean verifySolution = checkBoxVerifySolution.isSelected();
        if(granularity == EModelGranularity.Value)
            verifySolution = false;
        
		// initialize configuration
		try {
            ConstraintStrategyConfiguration.setStrategy(strategy);
            ConstraintStrategyConfiguration.setUseCones(checkBoxUseCones.isSelected());
            ConstraintStrategyConfiguration.setEarlyTermination(checkBoxUseEarlyTermination.isSelected());
            ConstraintStrategyConfiguration.setRuns(runs);
            ConstraintStrategyConfiguration.setUseStrings(checkBoxUseStrings.isSelected());
            ConstraintStrategyConfiguration.setVerifySolution(verifySolution);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(
					this,
					"Please provide a valid configuration!\nReason:\n\n"
							+ e.getMessage());
			return;
		}

        if(propertyDirectory == null || propertyDirectory.isEmpty())
            propertyDirectory = System.getProperty("user.dir");
        
		for (File file : new File(propertyDirectory)
				.listFiles(new java.io.FileFilter() {
					@Override
					public boolean accept(File pathname) {
						String fileName = pathname.getPath();
						if (fileName.endsWith(".properties") == false)
							return false;
						if (startFile != null
								&& fileName.compareTo(startFile) < 0)
							return false;
						if (endFile != null && fileName.compareTo(endFile) > 0)
							return false;
						if (allowDouble == false
								&& fileName.indexOf("_double") >= 0)
							return false;
						return true;
					}
				})) {
			try {
				String fileName = file.getPath();
				SpreadsheetProperties properties = new SpreadsheetProperties(
						fileName);
				TimeSpan totaltime = new TimeSpan(Precision.MILLISECONDS);
				for (int i = 1; i <= runs; ++i) {
					Executor executor = new Executor(propertyDirectory, properties);
					executor.start();
					if (checkBoxUseEarlyTermination.isSelected() == false)
						executor.join();
					else
						executor.join(maxMins * 1000);
					ModelBasedResult result = executor.getResult();
					if (executor.isAlive()) {
						System.out.println(fileName + " ... timeout!");

						executor.suspend();
						executor.stop();
						if (ConstraintStrategy.externalProcess != null) {
							RuntimeProcessExecuter
									.killProcess(ConstraintStrategy.externalProcess);
						}

						break;
					}
					if (i < runs) {
						totaltime.add(executor.getResult().getRuntime());
					} else {
						if (result == null) {
							System.out.println(fileName + " failed: "
									+ executor.getErrorMessage());
						} else if (checkDiagnosis(result.getAllDiagnoses(), properties.getFaultyCells())) {
							totaltime.add(executor.getResult().getRuntimeSolving());
							totaltime.divide(ConstraintStrategyConfiguration.getRuns());
							result.setRuntime(totaltime);
                            
                            BufferedWriter writer = null;
                            boolean writeHeader = false;
                            String csvFileName = strategy.getName()
                                + (checkBoxUseCones.isSelected() ? "_cone_" : "_nocone_") 
                                + runs + "runs.csv";

                            if(!new File(csvFileName).exists())
                                writeHeader = true;
                            writer = new BufferedWriter(new FileWriter(csvFileName, true));
                            Writer.setActive(false);
                            
                            if(writeHeader)
                                writer.write(ModelBasedResult.getColumnHeader() + System.lineSeparator());
                            writer.write(result.toString() + System.lineSeparator());
                            writer.flush();
                            writer.close();
							System.out.print(result.toString()
									+ System.lineSeparator());
						} else {
							System.out.print(properties.getExcelSheetName()
									+ ";" + strategy.getName()
									+ ";wrongDiagnosis:;;;;;"
									+ result.getDiagnosisAsString() + "\n");
							System.out.println("  WRONG DIAGNOSIS!!");
						}

					}
				}
			} catch (InterruptedException e) {
				continue;
			} catch (SpreadsheetPropertiesException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println("\ndone");

	}
}
