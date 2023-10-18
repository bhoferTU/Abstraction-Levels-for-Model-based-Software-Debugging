package at.tugraz.ist.debugging.modelbased.smt.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import at.tugraz.ist.debugging.modelbased.smt.SMTConstants;
import at.tugraz.ist.debugging.modelbased.smt.SMTConstants.Satisfiability;
import at.tugraz.ist.debugging.modelbased.smt.datastructures.Model;
import at.tugraz.ist.debugging.modelbased.smt.datastructures.UnsatCore;


/**
 * Parser for the output of the Z3 SMT solver. This parser is able to parse the
 * satisfiability string as well as the model (in case of SAT) and the unsat
 * core (in case of UNSAT).
 */
public class Z3Parser extends SolverOutputParser {
	/**
	 * Parser states for the model parser state machine
	 * 
	 */
	private enum EModelParserState {
		Assignment, Finished, Initial, InModel, Name
	}

	/**
	 * Parser states for the satisfiability parser state machine
	 * 
	 */
	private enum ESatisfiabilityParserState {
		Error, Finished, Initial, SatUnsat
	};

	/**
	 * Parser states for the unsat core parser state machine
	 * 
	 */
	private enum EUnsatCoreParserState {
		Finished, Initial, Variable
	};


	public Z3Parser(InputStream is) {
		super(is);
	}

	/**
	 * Parses the model output string provided by the SMT solver
	 * 
	 * @return found model
	 * @throws IOException
	 */
    @Override
	public Model getModel() throws IOException {

		Model model = new Model();
		EModelParserState state = EModelParserState.Initial;

		StringBuilder charBuffer = new StringBuilder();
		Stack<String> stringStack = new Stack<String>();
		while (state != EModelParserState.Finished) {
			int value = is.read();
			char charVal = (char) value;

			if (value == -1)
				throw new RuntimeException(
						String.format(
								"SMT-Parser: Error while reading model, invalid EOF token in state '%s'",
								state));

			switch (state) {
			case Assignment:
				if (charVal == ')') {
					// add item to map
					model.add(stringStack.pop(),
							SMTConstants.stringToBool(charBuffer.toString()));
					charBuffer.delete(0, charBuffer.length());
					state = EModelParserState.InModel;
				} else
					charBuffer.append(charVal);
				break;
			case Finished:

				break;
			case InModel:
				if (charVal == ')') {
					state = EModelParserState.Finished;
				} else if (charVal == '(') {
					state = EModelParserState.Name;
					if (charBuffer.length() > 0)
						throw new RuntimeException(
								String.format(
										"SMT-Parser: Error while reading model, stack is not empty in state '%s'",
										charVal, state));
				} else if (!Character.isWhitespace(charVal)) {
					throw new RuntimeException(
							String.format(
									"SMT-Parser: Error while reading model, invalid token '%s' (expected '(' or ')') in state '%s'",
									charVal, state));
				}
				break;
			case Initial:
				if (charVal == '(')
					state = EModelParserState.InModel;
				else if (!Character.isWhitespace(charVal))
					throw new RuntimeException(
							String.format(
									"SMT-Parser: Error while reading model, invalid token '%s' (expected '(') in state '%s'",
									charVal, state));

				break;
			case Name:
				if (Character.isWhitespace(charVal)) {
					stringStack.push(charBuffer.toString());
					charBuffer.delete(0, charBuffer.length());
					state = EModelParserState.Assignment;
				} else
					charBuffer.append(charVal);
				break;
			default:
				break;

			}
		}
		return model;

	}

	/**
	 * Parses the unsat core output string provided by the SMT solver
	 * 
	 * @return extracted unsatisfiable core
	 * @throws IOException
	 */
    @Override
	public UnsatCore getUnsatCore() throws IOException {
		UnsatCore core = new UnsatCore();
		EUnsatCoreParserState state = EUnsatCoreParserState.Initial;

		StringBuilder charBuffer = new StringBuilder();
		while (state != EUnsatCoreParserState.Finished) {
			int value = is.read();
			char charVal = (char) value;

			if (value == -1)
				throw new RuntimeException(
						String.format(
								"SMT-Parser: Error while reading model, invalid EOF token in state '%s'",
								state));

			switch (state) {
			case Finished:
				break;
			case Initial:
				if (charVal == '(')
					state = EUnsatCoreParserState.Variable;
				else if (!Character.isWhitespace(charVal))
					throw new RuntimeException(
							String.format(
									"SMT-Parser: Error while reading model, invalid token '%s' (expected '(') in state '%s'",
									charVal, state));
				break;
			case Variable:
				if (charVal == ' ' || charVal == ')') {
					if (charBuffer.length() > 0)
						core.add(charBuffer.toString());
					charBuffer.delete(0, charBuffer.length());
					if (charVal == ')')
						state = EUnsatCoreParserState.Finished;
				} else
					charBuffer.append(charVal);
				break;
			default:
				break;

			}
		}
		return core;
	}

	/**
	 * Parses the satisfiability output string provided by the SMT solver
	 * 
	 * @return satisfiable or unsatsisfiable
	 * @throws IOException
	 */
    @Override
	public Satisfiability isSatisfied() throws IOException {
		StringBuilder charBuffer = new StringBuilder();
		ESatisfiabilityParserState state = ESatisfiabilityParserState.Initial;

		while (true) {
			int value = is.read();
			if (value == -1)
				throw new RuntimeException(
						"SMT-Parser: Expected satisfiability token, but got EOF");
			char charVal = (char) value;

			switch (state) {
			case Error:
				charBuffer.append(charVal);
				if (charVal == ')') {
					state = ESatisfiabilityParserState.Finished;
					throw new RuntimeException(
							"SMT-Parser: Error while reading satisfiability token (only first error is shown): "
									+ charBuffer.toString());
				}
				break;
			case Finished:
				break;
			case Initial:
				charBuffer.append(charVal);
				if (charVal == '(')
					state = ESatisfiabilityParserState.Error;
				else if (charVal == 's' || charVal == 'u')
					state = ESatisfiabilityParserState.SatUnsat;
				else if (!Character.isWhitespace(charVal))
					throw new RuntimeException(
							String.format(
									"SMT-Parser: Expected satisfiability token, but read unknown token '%s'",
									charVal));
				break;
			case SatUnsat:
				if (Character.isWhitespace(charVal)) {
					state = ESatisfiabilityParserState.Finished;
					return SMTConstants.stringToSatisfiablity(charBuffer
							.toString());
				} else
					charBuffer.append(charVal);
				break;
			default:
				throw new RuntimeException();

			}

		}
	}

}