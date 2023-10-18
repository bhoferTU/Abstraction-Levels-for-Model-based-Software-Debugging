package at.tugraz.ist.debugging.modelbased.minion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;

import at.tugraz.ist.debugging.modelbased.solver.ConstraintStrategy;

class FileBasedMinionCaller extends MinionCaller {
	private static final String STDERR_FILE = "./minion_stderr.txt";
	private static final String STDOUT_FILE = "./minion_stdout.txt";

	@Override
	public void cleanup() {
		super.cleanup();
		cleanup(STDOUT_FILE);
		cleanup(STDERR_FILE);
	}

	@Override
	public MinionSolverResult getSolution(String fileName) {
		Process p = null;
		try {
			ProcessBuilder processBuilder = getProcessBuilder(fileName);

			File stdoutFile = new File(STDOUT_FILE);
			processBuilder.redirectOutput(stdoutFile);
			File stderrFile = new File(STDERR_FILE);
			processBuilder.redirectError(stderrFile);

			p = processBuilder.start();
			p.waitFor();

			return new MinionSolverResult(readFile(stdoutFile),
					readFile(stderrFile));

		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Communication error during MINION solve run", e);
		} catch (InterruptedException e) {
			return null;
		} finally {
			if (p != null)
				p.destroy();
		}
	}

	private String readFile(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));

		StringBuilder s = new StringBuilder();
		String line = null;
		while ((line = br.readLine()) != null) {
			s.append(line);
			s.append(System.lineSeparator());
		}
		br.close();
		return s.toString();
	}
}

/**
 * Calls an external Minion process and hands over the path to a previously
 * created minion file.
 * 
 */
public abstract class MinionCaller {
	public enum MinionCallerCommunicationMode {
		FileBased, PipeBased
	};

	protected static final String[] SETTINGS = { "-findallsols" };

	// protected static final String[] SETTINGS = {"-findallsols", "-varorder",
	// "ldf-random"};

	public static MinionCaller create(
			MinionCallerCommunicationMode communicationType) {
		switch (communicationType) {
		case FileBased:
			return new FileBasedMinionCaller();
		case PipeBased:
			return new PipeBasedMinionCaller();
		default:
			throw new IllegalArgumentException(
					"Cannot create MinionCaller instance due to unknown communication type");
		}
	}

	public void cleanup() {
	}

	public void cleanup(String fileName) {
		File file = new File(fileName);
		if (file.exists())
			file.delete();
	}

	protected ProcessBuilder getProcessBuilder(String fileName)
			throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder();

		if ((ManagementFactory.getOperatingSystemMXBean().getName()
				.equals("Mac OS X"))
				|| (ManagementFactory.getOperatingSystemMXBean().getName()
						.contains("Windows"))) {
			// TODO: find a more robust way
			ConstraintStrategy.externalProcess = "minion.exe";

			if (ManagementFactory.getOperatingSystemMXBean().getName()
					.equals("Mac OS X")) {
				processBuilder.command("./minion", SETTINGS[0], SETTINGS[1],
						SETTINGS[2], fileName);
			} else if (ManagementFactory.getOperatingSystemMXBean().getName()
					.contains("Windows")) {
				// processBuilder.command("minion.exe", SETTINGS[0],
				// SETTINGS[1], SETTINGS[2], fileName);
				processBuilder.command("minion.exe", SETTINGS[0], fileName);
			}

			return processBuilder;
		}
		throw new UnsupportedOperationException(String.format(
				"Invalid operating system: %s", ManagementFactory
						.getOperatingSystemMXBean().getName()));
	}

	public abstract MinionSolverResult getSolution(String fileName);
}

class PipeBasedMinionCaller extends MinionCaller {
	@Override
	public MinionSolverResult getSolution(String fileName) {
		try {
			MinionSolverResult result = new MinionSolverResult();

			ProcessBuilder pb = getProcessBuilder(fileName);

			Process p = pb.start();

			Streamer outStreamer = new Streamer(p.getInputStream());
			Streamer errStreamer = new Streamer(p.getErrorStream());
			Thread outThread = new Thread(outStreamer);
			Thread errThread = new Thread(errStreamer);
			outThread.start();
			errThread.start();
			outThread.join();
			errThread.join();
			p.waitFor();

			result.setSolution(outStreamer.getOuput().toString());
			result.setError(errStreamer.getOuput().toString());

			return result;
		} catch (IOException e) {
			throw new RuntimeException("I/O error in MINION caller", e);
		} catch (InterruptedException e) {
			throw new RuntimeException("I/O error in MINION caller", e);
		}
	}
}

class Streamer implements Runnable {
	private StringBuilder output;
	private BufferedReader reader;

	public Streamer(InputStream inputStream) {
		this.reader = new BufferedReader(new InputStreamReader(inputStream));
	}

	public StringBuilder getOuput() {
		return this.output;
	}

	@Override
	public void run() {
		String line;
		this.output = new StringBuilder();
		try {
			while ((line = this.reader.readLine()) != null) {
				this.output.append(line + "\n");
			}
			this.reader.close();
		} catch (IOException e) {
			System.err.println("ERROR: " + e.getMessage());
		}
	}
}
