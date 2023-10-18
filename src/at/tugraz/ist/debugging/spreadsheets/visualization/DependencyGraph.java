package at.tugraz.ist.debugging.spreadsheets.visualization;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;

import at.tugraz.ist.debugging.modelbased.Cell;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetProperties;
import at.tugraz.ist.debugging.spreadsheets.configuration.SpreadsheetPropertiesException;
import at.tugraz.ist.debugging.spreadsheets.datastructures.cells.CellContainer;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;

public class DependencyGraph {

	protected CellContainer cells = null;
	protected int edgeCounter = 0;
	JFrame frame = null;
	EditingModalGraphMouse<Cell, Integer> gm = null;

	public static void main(String[] args) {
		// List<String> corpus =
		// Directory.getFiles("Benchmarks\\ISCAS85\\configuration_files",
		// "properties");
		// List<String> corpus = new ArrayList<String>();
		// corpus.add("Benchmarks\\ISCAS85\\configuration_files\\c432_BOOL_tc1_1_1Fault.properties");

		DependencyGraph dependencyGraph = new DependencyGraph();
		String propertiesFile = "Benchmarks\\INTEGER\\configuration_files\\fromAFW\\AFW_amortization_1Faults_Fault1.properties";
		try {
			SpreadsheetProperties properties = new SpreadsheetProperties(propertiesFile);
			Graph<Cell, Integer> dg = dependencyGraph.createGraph(properties.getExcelSheetPath());
			dg = dependencyGraph.createGraph("C:\\Users\\inica\\Desktop\\Spreadsheets2021\\new Birgit\\MulitFault.xlsx");
			System.out.println("The graph g = " + dg.toString());
			dependencyGraph.drawGraph(dg);
		} catch (SpreadsheetPropertiesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected Graph<Cell, Integer> createGraph(String spreadsheetFile) {
		Graph<Cell, Integer> dg = new DirectedSparseMultigraph<Cell, Integer>();
		edgeCounter = 0;

		cells = CellContainer.create(spreadsheetFile);
		for (Cell c : cells.getCells()) {
			dg.addVertex(c);
			Set<Cell> references = c.getExpression().getReferencedCells(false, true);
			for (Cell c2 : references) {
				dg.addEdge(edgeCounter++, c2, c);
			}
		}

		return dg;
	}

	protected void drawGraph(Graph<Cell, Integer> dg) {
		// KKLayout oder StaticLayout
		Layout<Cell, Integer> layout = new KKLayout<Cell, Integer>(dg);
		// layout.setSize(new Dimension(dg.getVertexCount() * 50,
		// dg.getVertexCount() * 40));
		layout.setSize(new Dimension(1200, 1000));
		VisualizationViewer<Cell, Integer> vv = new VisualizationViewer<Cell, Integer>(layout);
		// vv.setPreferredSize(new Dimension(dg.getVertexCount() * 50 + 50,
		// dg.getVertexCount() * 40 + 50));
		vv.setPreferredSize(new Dimension(1250, 1050));
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Cell>());

		Transformer<Cell, Paint> vertexPaint = new Transformer<Cell, Paint>() {
			public Paint transform(Cell cell) {
				if (cell.isFormulaCell()) {
					if (cells.getOutputCoords().contains(cell.getCoords()))
						return Color.RED;
					else
						return Color.GREEN;
				} else
					return Color.BLUE;

			}
		};

		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);

		Factory<Cell> vertexFactory = new Factory<Cell>() { // My vertex factory
			public Cell create() {
				return new Cell(0, 0, 0);
			}
		};

		Factory<Integer> edgeFactory = new Factory<Integer>() { // My edge
																// factory
			public Integer create() {
				return edgeCounter++;
			}
		};

		gm = new EditingModalGraphMouse<Cell, Integer>(vv.getRenderContext(), vertexFactory, edgeFactory);
		gm.setMode(ModalGraphMouse.Mode.PICKING);
		vv.setGraphMouse(gm);

		JMenuBar menuBar = new JMenuBar();
		JMenu modeMenu = gm.getModeMenu();
		modeMenu.setText("Mouse Mode");
		modeMenu.setIcon(null);
		modeMenu.setPreferredSize(new Dimension(80, 20));
		menuBar.add(modeMenu);

		frame = new JFrame("Simple Graph View");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.setJMenuBar(menuBar);

		frame.pack();
		frame.setVisible(true);
	}

}
